package ru.quipy.status

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ru.quipy.core.EventSourcingService
import ru.quipy.status.dto.StatusCreate
import ru.quipy.status.dto.StatusViewModel
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import ru.quipy.status.eda.logic.StatusAggregateState
import ru.quipy.status.eda.logic.create
import ru.quipy.status.eda.logic.delete
import ru.quipy.status.eda.projections.StatusViewService
import java.util.*

@RestController
@RequestMapping("/status")
class StatusController(
        private val statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>,
        private val statusViewService: StatusViewService
) {
    @PostMapping
    fun createStatus(@RequestBody req: StatusCreate): StatusCreatedEvent? {
        if (statusViewService.isStatusExist(req.statusName, req.color)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "status already exist.")
        }

        return statusEsService.create {
            it.create(
                    req.statusName,
                    req.color)
        }
    }

    @GetMapping("/{statusId}")
    fun getStatus(@PathVariable statusId: UUID): StatusViewModel {
        return statusViewService.getStatus(statusId)
    }

    @GetMapping
    fun getStatuses(): List<StatusViewModel> {
        return statusViewService.getStatuses()
    }

    @DeleteMapping("/{statusId}")
    fun deleteStatus(@PathVariable statusId: UUID): StatusDeletedEvent {
        return statusEsService.update(statusId) {
            it.delete()
        }
    }
}