package ru.quipy.status.eda.logic

import ru.quipy.status.dto.StatusCreate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import java.util.*

fun StatusAggregateState.create(
        status: StatusCreate
): StatusCreatedEvent {
    return StatusCreatedEvent(UUID.randomUUID(), status.statusName, status.color)
}

fun StatusAggregateState.delete(): StatusDeletedEvent {
    return StatusDeletedEvent(getId())
}