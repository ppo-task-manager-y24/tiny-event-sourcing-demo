package ru.quipy.status.eda.logic

import ru.quipy.status.eda.api.StatusRemovedFromTaskEvent
import ru.quipy.status.eda.api.StatusUsedInTaskEvent
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import java.util.*

fun StatusAggregateState.create(
        id: UUID,
        statusName: String,
        statusColor: Int
): StatusCreatedEvent {
    return StatusCreatedEvent(
            id,
            statusName,
            statusColor
    )
}

fun StatusAggregateState.delete(): StatusDeletedEvent {
    if (usedTaskIds.isNotEmpty()) {
        throw IllegalStateException("Unable to delete status – status ${this.getId()} is used in tasks: ${this.usedTaskIds}")
    }

    return StatusDeletedEvent(this.getId())
}

fun StatusAggregateState.statusUsedInTask(taskId: UUID): StatusUsedInTaskEvent {
    return StatusUsedInTaskEvent(taskId)
}

fun StatusAggregateState.statusRemovedInTask(taskId: UUID): StatusRemovedFromTaskEvent {
    return StatusRemovedFromTaskEvent(taskId)
}