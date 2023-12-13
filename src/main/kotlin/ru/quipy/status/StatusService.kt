package ru.quipy.status

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.core.EventSourcingService
import ru.quipy.status.dto.StatusCreate
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import ru.quipy.status.eda.logic.StatusAggregateState
import ru.quipy.status.eda.logic.create
import ru.quipy.status.eda.logic.delete
import java.util.*

interface StatusService {
    fun createStatus(projectId: UUID, data: StatusCreate): StatusCreatedEvent

    fun getStatus(statusId: UUID): StatusAggregateState?

    fun getStatuses(projectId: UUID): List<StatusAggregateState>

    fun deleteStatus(projectId: UUID, statusId: UUID): StatusDeletedEvent
}

@Service
class StatusServiceImpl(
        private val statusRepository: StatusRepository,
        private val statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>
) : StatusService
{
    override fun createStatus(projectId: UUID, data: StatusCreate): StatusCreatedEvent {
        val status: StatusEntity? = statusRepository.findByName(data.statusName)

        if (status != null) {
            if (status.projectId == projectId) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "status already exist.")
            }
        }

        val statusEntity = statusRepository.save(StatusEntity(
                            projectId, data.statusName, data.color)
        )

        return statusEsService.create {
            it.create(
                    statusEntity.projectId,
                    statusEntity.statusId,
                    statusEntity.statusName,
                    statusEntity.color
            )
        }
    }

    override fun getStatus(statusId: UUID): StatusAggregateState? {
        return statusEsService.getState(statusId)
    }

    override fun getStatuses(projectId: UUID): List<StatusAggregateState> {
        val statuses: List<StatusEntity> = statusRepository.findByProject(projectId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "project doesn't exist.")
        val states = listOf<StatusAggregateState>()

        statuses.forEach {
            states.plus(statusEsService.getState(it.statusId))
        }

        return states
    }

    override fun deleteStatus(projectId: UUID, statusId: UUID): StatusDeletedEvent {
        val status = statusRepository.findById(statusId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "status doesn't exist.")

        statusRepository.delete(status)

        return statusEsService.update(statusId) {
            it.delete(projectId)
        }
    }
}