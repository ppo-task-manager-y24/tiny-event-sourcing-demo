package ru.quipy.status.eda.logic

import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import java.util.*

fun StatusAggregateState.create(
        projectId: UUID,
        statusId: UUID,
        statusName: String,
        statusColor: String
): StatusCreatedEvent {
    return StatusCreatedEvent(
            projectId,
            statusId,
            statusName,
            statusColor
    )
}

fun StatusAggregateState.delete(projectId: UUID): StatusDeletedEvent {
    return StatusDeletedEvent(projectId, getId())
}