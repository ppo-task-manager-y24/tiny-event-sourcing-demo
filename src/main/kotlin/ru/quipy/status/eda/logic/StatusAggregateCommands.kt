package ru.quipy.status.eda.logic

import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import java.util.*

fun StatusAggregateState.create(
        statusName: String,
        statusColor: Int
): StatusCreatedEvent {
    return StatusCreatedEvent(
            UUID.randomUUID(),
            statusName,
            statusColor
    )
}

fun StatusAggregateState.delete(): StatusDeletedEvent {
    return StatusDeletedEvent(this.getId())
}