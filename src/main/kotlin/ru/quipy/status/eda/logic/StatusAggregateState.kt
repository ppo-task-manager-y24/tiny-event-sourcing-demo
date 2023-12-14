package ru.quipy.status.eda.logic

import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import java.util.UUID


class StatusAggregateState: AggregateState<UUID, StatusAggregate> {
    private lateinit var statusId: UUID
    private lateinit var statusName: String
    private var color: Int = 0
    private var isDeleted: Boolean = false
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    override fun getId() = statusId

    fun isDeleted() = isDeleted

    @StateTransitionFunc
    fun statusCreatedApply(event: StatusCreatedEvent) {
        statusId = event.statusId
        statusName = event.statusName
        color = event.color
    }

    @StateTransitionFunc
    fun statusDeletedApply(event: StatusDeletedEvent) {
        isDeleted = true
        updatedAt = System.currentTimeMillis()
    }
}