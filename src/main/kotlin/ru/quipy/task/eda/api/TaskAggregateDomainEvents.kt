package ru.quipy.task.eda.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val TASK_CREATED_EVENT = "TASK_CREATED_EVENT"
const val TASK_UPDATED_EVENT = "TASK_UPDATED_EVENT"
const val TASK_NAME_CHANGED_EVENT = "TASK_NAME_CHANGED_EVENT"
const val TASK_EXECUTOR_ADDED_EVENT = "TASK_EXECUTOR_ADDED_EVENT"

@DomainEvent(name = TASK_CREATED_EVENT)
class TaskCreatedEvent(
        val taskId: UUID,
        val taskName: String,
        val taskDescription: String,
        val projectId: UUID,
        val statusId: UUID,
        createdAt: Long = System.currentTimeMillis(),
) : Event<TaskAggregate>(
        name = TASK_CREATED_EVENT,
        createdAt = createdAt,
)

@DomainEvent(name = TASK_UPDATED_EVENT)
class TaskUpdatedEvent(
        val taskId: UUID,
        createdAt: Long = System.currentTimeMillis(),
) : Event<TaskAggregate>(
        name = TASK_UPDATED_EVENT,
        createdAt = createdAt,
)

@DomainEvent(name = TASK_NAME_CHANGED_EVENT)
class TaskNameChangedEvent(
        val taskId: UUID,
        val taskName: String,
        createdAt: Long = System.currentTimeMillis(),
) : Event<TaskAggregate>(
        name = TASK_NAME_CHANGED_EVENT,
        createdAt = createdAt,
)

@DomainEvent(name = TASK_EXECUTOR_ADDED_EVENT)
class TaskExecutorAddedEvent(
        val taskId: UUID,
        val executorId: UUID,
        createdAt: Long = System.currentTimeMillis(),
) : Event<TaskAggregate>(
        name = TASK_EXECUTOR_ADDED_EVENT,
        createdAt = createdAt,
)