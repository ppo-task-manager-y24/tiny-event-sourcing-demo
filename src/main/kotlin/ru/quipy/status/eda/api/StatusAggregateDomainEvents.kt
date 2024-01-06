package ru.quipy.status.eda.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import ru.quipy.project.eda.api.STATUS_REMOVED_FROM_TASK_EVENT
import ru.quipy.project.eda.api.STATUS_USED_IN_TASK_EVENT
import ru.quipy.status.eda.logic.Status
import java.util.*

const val STATUS_ADDED_EVENT = "STATUS_ADDED_EVENT"
const val STATUS_CREATED_EVENT = "STATUS_CREATED_EVENT"
const val STATUS_DELETED_EVENT = "STATUS_DELETED_EVENT"

@DomainEvent(name = STATUS_CREATED_EVENT)
class StatusCreatedEvent(
        val projectId: UUID,
        createdAt: Long = System.currentTimeMillis(),
) : Event<StatusAggregate>(
        name = STATUS_CREATED_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = STATUS_ADDED_EVENT)
class StatusAddedEvent(
        val projectId: UUID,
        val statusId: UUID,
        val statusName: String,
        val color: Int,
        createdAt: Long = System.currentTimeMillis(),
) : Event<StatusAggregate>(
        name = STATUS_ADDED_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = STATUS_DELETED_EVENT)
class StatusDeletedEvent(
        val projectId: UUID,
        val statusId: UUID,
        createdAt: Long = System.currentTimeMillis()
) : Event<StatusAggregate>(
        name = STATUS_DELETED_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = STATUS_USED_IN_TASK_EVENT)
class StatusUsedInTaskEvent(
        val projectId: UUID,
        val statusId: UUID,
        val taskId: UUID,
        createdAt: Long = System.currentTimeMillis()
): Event<StatusAggregate>(
        name = STATUS_USED_IN_TASK_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = STATUS_REMOVED_FROM_TASK_EVENT)
class StatusRemovedFromTaskEvent(
        val projectId: UUID,
        val statusId: UUID,
        val taskId: UUID,
        createdAt: Long = System.currentTimeMillis()
): Event<StatusAggregate>(
        name = STATUS_REMOVED_FROM_TASK_EVENT,
        createdAt = createdAt
)