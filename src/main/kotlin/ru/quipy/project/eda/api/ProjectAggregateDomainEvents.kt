package ru.quipy.project.eda.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val PROJECT_CREATED_EVENT = "PROJECT_CREATED_EVENT"
const val PROJECT_UPDATED_EVENT = "PROJECT_UPDATED_EVENT"
const val PROJECT_PARTICIPANT_ADDED_EVENT = "PROJECT_PARTICIPANT_ADDED_EVENT"

const val TASK_CREATED_EVENT = "TASK_CREATED_EVENT"
const val TASK_UPDATED_EVENT = "TASK_UPDATED_EVENT"
const val TASK_NAME_CHANGED_EVENT = "TASK_NAME_CHANGED_EVENT"
const val TASK_EXECUTOR_ADDED_EVENT = "TASK_EXECUTOR_ADDED_EVENT"

const val STATUS_USED_IN_TASK_EVENT = "STATUS_USED_IN_TASK_EVENT"
const val STATUS_REMOVED_FROM_TASK_EVENT = "STATUS_REMOVED_FROM_TASK_EVENT"
const val STATUS_CHANGED_IN_TASK_EVENT = "STATUS_CHANGED_IN_TASK_EVENT"

// API
@DomainEvent(name = PROJECT_CREATED_EVENT)
class ProjectCreatedEvent(
    val projectId: UUID,
    val projectName: String,
    val projectOwner: UUID,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = PROJECT_CREATED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = PROJECT_UPDATED_EVENT)
class ProjectUpdatedEvent(
    val projectId: UUID,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = PROJECT_UPDATED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = PROJECT_PARTICIPANT_ADDED_EVENT)
class ProjectParticipantAddedEvent(
    val projectId: UUID,
    val participantId: UUID,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = PROJECT_PARTICIPANT_ADDED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = TASK_CREATED_EVENT)
class TaskCreatedEvent(
    val taskId: UUID,
    val taskName: String,
    val taskDescription: String,
    val projectId: UUID,
    val statusId: UUID,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = TASK_CREATED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = TASK_UPDATED_EVENT)
class TaskUpdatedEvent(
    val taskId: UUID,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = TASK_UPDATED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = TASK_NAME_CHANGED_EVENT)
class TaskNameChangedEvent(
    val taskId: UUID,
    val taskName: String,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = TASK_NAME_CHANGED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = TASK_EXECUTOR_ADDED_EVENT)
class TaskExecutorAddedEvent(
    val projectId: UUID,
    val taskId: UUID,
    val taskName: String,
    val executorId: UUID,
    createdAt: Long = System.currentTimeMillis(),
) : Event<ProjectAggregate>(
    name = TASK_EXECUTOR_ADDED_EVENT,
    createdAt = createdAt,
)

@DomainEvent(name = STATUS_USED_IN_TASK_EVENT)
class StatusUsedInTaskEvent(
    val projectId: UUID,
    val statusId: UUID,
    val taskId: UUID,
    createdAt: Long = System.currentTimeMillis()
): Event<ProjectAggregate>(
    name = STATUS_USED_IN_TASK_EVENT,
    createdAt = createdAt
)

@DomainEvent(name = STATUS_REMOVED_FROM_TASK_EVENT)
class StatusRemovedFromTaskEvent(
    val projectId: UUID,
    val statusId: UUID,
    val taskId: UUID,
    createdAt: Long = System.currentTimeMillis()
): Event<ProjectAggregate>(
    name = STATUS_REMOVED_FROM_TASK_EVENT,
    createdAt = createdAt
)

@DomainEvent(name = STATUS_CHANGED_IN_TASK_EVENT)
class StatusChangedInTaskEvent(
    val projectId: UUID,
    val statusId: UUID,
    val taskId: UUID,
    createdAt: Long = System.currentTimeMillis()
): Event<ProjectAggregate>(
    name = STATUS_CHANGED_IN_TASK_EVENT,
    createdAt = createdAt
)