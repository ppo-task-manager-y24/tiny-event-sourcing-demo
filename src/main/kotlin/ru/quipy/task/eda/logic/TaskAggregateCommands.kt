package ru.quipy.task.eda.logic

import ru.quipy.task.eda.api.*
import ru.quipy.domain.Event
import java.util.*

fun TaskAggregateState.create(name: String, description: String, projectId: UUID, statusId: UUID): List<Event<TaskAggregate>> {
    val taskId = UUID.randomUUID()

    return listOf(
        TaskCreatedEvent(
            taskId = taskId,
            taskName = name,
            taskDescription = description,
            projectId = projectId,
            statusId = statusId
        )
    )
}

fun TaskAggregateState.rename(name: String): List<Event<TaskAggregate>> {
    return ArrayList(
            listOf(
                    TaskUpdatedEvent(this.getId()),
                    TaskNameChangedEvent(this.getId(), name)
            )
    )
}

fun TaskAggregateState.addExecutor(id: UUID): List<Event<TaskAggregate>> {
    return ArrayList(
            listOf(
                    TaskUpdatedEvent(this.getId()),
                    TaskExecutorAddedEvent(this.getId(), this.projectId, id)
            )
    )
}