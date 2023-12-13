package ru.quipy.status.eda.logic

import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusCreatedEvent
import java.util.UUID


class StatusAggregateState: AggregateState<UUID, StatusAggregate> {
    private lateinit var statusId: UUID
    private lateinit var statusName: String
    private lateinit var color: String
    private var deleted: Boolean = false

    override fun getId() = statusId

    @StateTransitionFunc
    fun statusCreatedApply(event: StatusCreatedEvent) {
        statusId = event.statusId
        statusName = event.statusName
        color = event.color
    }

    @StateTransitionFunc
    fun statusDeletedApply() {
        deleted = true
    }
}