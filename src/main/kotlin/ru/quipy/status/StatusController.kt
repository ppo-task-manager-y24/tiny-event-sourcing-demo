package ru.quipy.status

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ru.quipy.core.EventSourcingService
import ru.quipy.status.dto.StatusAdd
import ru.quipy.status.dto.StatusViewModel
import ru.quipy.status.eda.api.StatusAddedEvent
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import ru.quipy.status.eda.logic.StatusAggregateState
import ru.quipy.status.eda.logic.addStatus
import ru.quipy.status.eda.logic.create
import ru.quipy.status.eda.logic.delete
import ru.quipy.status.eda.projections.status_view.StatusViewService
import java.util.*

@RestController
@RequestMapping("/status/{projectId}")
class StatusController(
        private val statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>,
        private val statusViewService: StatusViewService
) {
    @PutMapping
    fun addStatus(@PathVariable projectId: UUID, @RequestBody req: StatusAdd): StatusAddedEvent? {
        if (statusViewService.isStatusExist(projectId, req.statusName, req.color)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "status already exist.")
        }

        return statusEsService.update(projectId) {
            it.addStatus(
                UUID.randomUUID(),
                req.statusName,
                req.color
            )
        }
    }

    @PostMapping
    fun createStatus(@PathVariable projectId: UUID): StatusCreatedEvent? {

        return statusEsService.create {
            it.create(projectId)
        }
    }

    @GetMapping("/{statusId}")
    fun getStatus(@PathVariable projectId: UUID, @PathVariable statusId: UUID): StatusViewModel {
        return statusViewService.getStatus(projectId, statusId)
    }

    @GetMapping
    fun getStatuses(@PathVariable projectId: UUID): List<StatusViewModel> {
        return statusViewService.getStatuses(projectId)
    }

    @DeleteMapping("/{statusId}")
    fun deleteStatus(@PathVariable projectId: UUID, @PathVariable statusId: UUID): StatusDeletedEvent {
        return statusEsService.update(projectId) {
            it.delete(statusId)
        }
    }
}