package ru.quipy.status.eda.projections.status_view

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.status.dto.StatusViewModel
import java.util.*

interface StatusService {
    fun createStatus(projectId: UUID, statusId: UUID, statusName: String, color: Int): StatusViewModel

    fun getStatus(projectId: UUID, statusId: UUID): StatusViewModel

    fun getStatuses(projectId: UUID): List<StatusViewModel>

    fun isStatusExist(projectId: UUID, statusName: String, color: Int): Boolean

    fun deleteStatus(projectId: UUID, statusId: UUID)

    fun getOne(projectId: UUID): StatusViewEntity?
}

@Service
class StatusViewService(
    private val statusRepository: StatusViewRepository
): StatusService
{

    override fun getOne(projectId: UUID): StatusViewEntity? {
        return statusRepository.findByIdOrNull(projectId)
    }

    override fun createStatus(projectId: UUID, statusId: UUID, statusName: String, color: Int): StatusViewModel {
        val entity = getOne(projectId)

        val statusEntity = StatusEntity(
            projectId = projectId,
            statusId = statusId,
            statusName = statusName,
            color = color,
            isDeleted = false,
            usedTaskIds = setOf()
        )

        if (entity == null) {

            val entity = StatusViewEntity(projectId, projectId, mapOf(Pair(statusId, statusEntity)))
            statusRepository.save(entity)
            return StatusViewModel(statusId, projectId, statusName, color)
        }

        val statuses = entity.statuses.toMutableMap()
        statuses[statusId] = statusEntity


        statusRepository.save(
            StatusViewEntity(projectId, projectId, statuses)
        )
        return StatusViewModel(statusId, projectId, statusName, color)
    }

    override fun isStatusExist(projectId: UUID, statusName: String, color: Int): Boolean {
        val entity = getOne(projectId)

        val any = entity?.statuses?.values?.any {
            it.projectId == projectId && it.statusName == statusName && it.color == color
        } ?: false

        return any
    }

    override fun getStatus(projectId: UUID, statusId: UUID): StatusViewModel {

        val statuses = getOne(projectId) ?:
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist")

        val status = statuses.statuses[statusId] ?:
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist")

        if (status.isDeleted) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")
        }

        return StatusViewModel(status.statusId, projectId, status.statusName, status.color)
    }

    override fun getStatuses(projectId: UUID): List<StatusViewModel> {
        val statuses = getOne(projectId)?.statuses?.values?.toList()

        val models = listOf<StatusViewModel>()

        if (statuses == null) {
            return models
        }

        statuses.forEach {
            if (!it.isDeleted) {
                models.plus(StatusViewModel(it.statusId, projectId, it.statusName, it.color))
            }
        }

        return models
    }

    override fun deleteStatus(projectId: UUID, statusId: UUID) {
        val status = getOne(projectId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")

        if (status.projectId != projectId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")
        }

        val targetModel = status.statuses[statusId]

        if (targetModel == null) {
            return
        }

        targetModel.isDeleted = true

        val statuses = status.statuses.toMutableMap()
        statuses[statusId] = targetModel

        statusRepository.save(StatusViewEntity(status.projectId, status.projectId, statuses))
    }
}