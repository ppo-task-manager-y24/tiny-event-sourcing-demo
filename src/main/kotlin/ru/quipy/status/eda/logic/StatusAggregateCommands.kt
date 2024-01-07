package ru.quipy.status.eda.logic

import ru.quipy.status.eda.api.*
import java.util.*

fun StatusAggregateState.addStatus(
        statusId: UUID,
        statusName: String,
        statusColor: Int
): StatusAddedEvent {
    if (statuses.containsKey(statusId) && !statuses[statusId]?.isDeleted()!!) {
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
    if (!statuses.containsKey(statusId)) {
        throw IllegalStateException("there is no status with such id: ${statusId} in project: ${getId()}")
    }

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

fun StatusAggregateState.changeStatusForTask(taskId: UUID, newStatusId: UUID): StatusChangedInTaskEvent {
    if (!statuses.containsKey(newStatusId)) {
        throw IllegalStateException("Status with such id: $newStatusId has not yet been created. First create it")
    }

    val currentStatusId = statuses.toList().firstOrNull {
        it.second.usedTaskIds.contains(taskId)
    }?.first

    if (currentStatusId == null) {
        return StatusChangedInTaskEvent(getId(), newStatusId, taskId)
    }

    if (currentStatusId == newStatusId) {
        throw IllegalStateException("Can't change to same status")
    }

    return StatusChangedInTaskEvent(getId(), newStatusId, taskId)
}