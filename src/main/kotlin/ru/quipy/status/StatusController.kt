package ru.quipy.status

import org.springframework.web.bind.annotation.*
import ru.quipy.status.dto.StatusCreate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import ru.quipy.status.eda.logic.StatusAggregateState
import java.util.*

@RestController
@RequestMapping("/status")
class StatusController(
        private val statusEsService: StatusService
) {
    @PostMapping
    fun createStatus(@RequestBody req: StatusCreate): StatusCreatedEvent {
        return statusEsService.createStatus(req)
    }

    @GetMapping("/{statusId}")
    fun getStatus(@PathVariable statusId: UUID) : StatusAggregateState? {
        return statusEsService.getStatus(statusId)
    }

    @GetMapping("/{projectId}")
    fun getStatusesList(@PathVariable projectId: UUID): List<StatusAggregateState>? {
        return statusEsService.getStatuses(projectId)
    }

    @PostMapping("/{statusId}")
    fun deleteStatus(@PathVariable statusId: UUID): StatusDeletedEvent {
        return statusEsService.deleteStatus(statusId)
    }
}