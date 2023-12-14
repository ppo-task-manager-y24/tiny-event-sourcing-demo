package ru.quipy.status.eda.projections

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.status.StatusRepository
import ru.quipy.status.dto.StatusViewModel
import java.util.*

interface StatusService {
    fun createStatus(statusId: UUID, statusName: String, color: Int): StatusViewModel

    fun getStatus(statusId: UUID): StatusViewModel

    fun getStatuses(): List<StatusViewModel>

    fun isStatusExist(statusName: String, color: Int): Boolean

    fun deleteStatus(statusId: UUID)
}

@Service
class StatusViewService(
        private val statusRepository: StatusRepository
): StatusService
{
    override fun createStatus(statusId: UUID, statusName: String, color: Int): StatusViewModel {
        val entity = StatusViewEntity(
                statusId = statusId,
                statusName = statusName,
                color = color,
                isDeleted = false)

        statusRepository.save(entity)

        return StatusViewModel(entity.statusId, entity.statusName, entity.color)
    }

    override fun isStatusExist(statusName: String, color: Int): Boolean {
        return statusRepository.findByNameAndColor(statusName, color) != null
    }

    override fun getStatus(statusId: UUID): StatusViewModel {
        val status = statusRepository.findByIdOrNull(statusId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")

        return StatusViewModel(status.statusId, status.statusName, status.color)
    }

    override fun getStatuses(): List<StatusViewModel> {
        val statuses = statusRepository.findAll()

        val models = listOf<StatusViewModel>()

        statuses.forEach {
            models.plus(StatusViewModel(it.statusId, it.statusName, it.color))
        }

        return models
    }

    override fun deleteStatus(statusId: UUID) {
        val status = statusRepository.findByIdOrNull(statusId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")

        status.isDeleted = true
        statusRepository.save(status)
    }
}