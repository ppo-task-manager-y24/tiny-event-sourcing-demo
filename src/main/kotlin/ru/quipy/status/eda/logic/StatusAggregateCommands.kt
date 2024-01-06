package ru.quipy.status.eda.logic

import ru.quipy.status.eda.api.*
import java.util.*

fun StatusAggregateState.addStatus(
        statusId: UUID,
        statusName: String,
        statusColor: Int
): StatusAddedEvent {
    if (statuses.containsKey(statusId)) {
        throw IllegalStateException("there is already status with such id: ${statusId} in project: ${getId()}")
    }

    return StatusAddedEvent(
            getId(),
            statusId,
            statusName,
            statusColor
    )
}

fun StatusAggregateState.create(
    projectId: UUID
): StatusCreatedEvent {
    return StatusCreatedEvent(projectId)
}

@Throws(IllegalStateException::class)
fun StatusAggregateState.delete(statusId: UUID): StatusDeletedEvent {
    if (statuses[statusId]?.usedTaskIds?.isNotEmpty() != false) {
        throw IllegalStateException("Unable to delete status – status ${this.getId()} is used in tasks: ${this.statuses[statusId]?.usedTaskIds}")
    }

    return StatusDeletedEvent(getId(), statusId)
}

fun StatusAggregateState.statusUsedInTask(taskId: UUID, statusId: UUID): StatusUsedInTaskEvent {
    return StatusUsedInTaskEvent(getId(), statusId, taskId)
}

fun StatusAggregateState.statusRemovedInTask(taskId: UUID, statusId: UUID): StatusRemovedFromTaskEvent {
    return StatusRemovedFromTaskEvent(getId(), statusId, taskId)
}