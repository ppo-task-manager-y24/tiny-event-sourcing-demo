package ru.quipy.status.eda.projections.status_view

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import ru.quipy.projections.project_view.toEntity
import ru.quipy.status.dto.StatusViewModel
import java.util.*

interface StatusService {
    fun createStatus(projectId: UUID, statusId: UUID, statusName: String, color: Int): StatusViewModel

    fun getStatus(projectId: UUID, statusId: UUID): StatusViewModel

    fun getStatuses(projectId: UUID): List<StatusViewModel>

    fun isStatusExist(projectId: UUID, statusName: String, color: Int): Boolean

    fun deleteStatus(projectId: UUID, statusId: UUID)
}

@Service
class StatusViewService(
    private val statusRepository: StatusViewRepository
): StatusService
{

    override fun createStatus(projectId: UUID, statusId: UUID, statusName: String, color: Int): StatusViewModel {
        val entity = StatusViewEntity(
            id = statusId,
            projectId = projectId,
            statusId = statusId,
            statusName = statusName,
            color = color,
            isDeleted = false
        )

        statusRepository.save(entity)

        return StatusViewModel(entity.statusId, entity.projectId, entity.statusName, entity.color)
    }

    override fun isStatusExist(projectId: UUID, statusName: String, color: Int): Boolean {
        return statusRepository.findAll().any {
            it.projectId == projectId && it.statusName == statusName && it.color == color
        }
    }

    override fun getStatus(projectId: UUID, statusId: UUID): StatusViewModel {
//        val statuses = statusRepository.findAll().filter {
//            projectId == it.projectId
//        }

//        val status = statuses.firstOrNull { it.statusId == statusId }
//            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")

        val status = statusRepository.findByIdOrNull(statusId) ?:
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist")

        if (status.isDeleted) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")
        }

        return StatusViewModel(status.statusId, projectId, status.statusName, status.color)
    }

    override fun getStatuses(projectId: UUID): List<StatusViewModel> {
        val statuses = statusRepository.findAll().filter {
            projectId == it.projectId
        }

        val models = listOf<StatusViewModel>()

        statuses.forEach {
            models.plus(StatusViewModel(it.statusId, projectId, it.statusName, it.color))
        }

        return models
    }

    override fun deleteStatus(projectId: UUID, statusId: UUID) {
        val status = statusRepository
            .findAll().filter {
                it.projectId == projectId
            }.firstOrNull {
                it.statusId == statusId
            }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")

        status.isDeleted = true
        statusRepository.save(status)
    }
}