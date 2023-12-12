package ru.quipy.status

import org.springframework.web.bind.annotation.*
import ru.quipy.core.EventSourcingService
import ru.quipy.status.dto.StatusCreate
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import ru.quipy.status.eda.logic.StatusAggregateState
import ru.quipy.status.eda.logic.create
import ru.quipy.status.eda.logic.delete
import java.util.*

@RestController
@RequestMapping("/status")
class StatusController(
        private val statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>
) {
    @PostMapping("/")
    fun createStatus(@RequestBody req: StatusCreate): StatusCreatedEvent {
        return statusEsService.create { it.create(req) }
    }

    @GetMapping("/{projectId}/{statusId}")
    fun getStatus(@PathVariable statusId: UUID) : StatusAggregateState? = statusEsService.getState(statusId)

    //@GetMapping("/{projectId}/statuses")
    //fun getStatusList(@PathVariable)

    @PostMapping("/{projectId}/{statusId}")
    fun deleteStatus(@PathVariable statusId: UUID): StatusDeletedEvent {
        return statusEsService.update(statusId) { it.delete() }
    }
}