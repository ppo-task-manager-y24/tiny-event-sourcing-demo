package ru.quipy.project.eda.logic

import ru.quipy.domain.Event
import ru.quipy.project.eda.api.*
import java.util.*


// Commands : takes something -> returns event
// Here the commands are represented by extension functions, but also can be the class member functions

fun ProjectAggregateState.create(id: UUID = UUID.randomUUID(), name: String, ownerId: UUID): ProjectCreatedEvent {
    return ProjectCreatedEvent(
        projectId = id,
        projectName = name,
        projectOwner = ownerId,
    )
}

@Throws(IllegalArgumentException::class)
fun ProjectAggregateState.addUser(id: UUID): List<Event<ProjectAggregate>> {
    if (participants.contains(id)) {
        throw IllegalArgumentException("User with id: $id already exists in project")
    }
    return ArrayList(
        listOf(
            ProjectParticipantAddedEvent(this.getId(), id),
            ProjectUpdatedEvent(this.getId())
        )
    )
}

fun ProjectAggregateState.createTask(taskId: UUID, name: String, description: String, statusId: UUID): List<Event<ProjectAggregate>> {
    if (tasksNames.contains(name)) {
        throw IllegalArgumentException("Task with name: $name already exists in project")
    }

    return listOf(
        TaskCreatedEvent(
            taskId = taskId,
            taskName = name,
            taskDescription = description,
            projectId = this.getId(),
            statusId = statusId
        ),
        StatusUsedInTaskEvent(
            getId(),
            statusId,
            taskId
        )
    )
}

@Throws(IllegalArgumentException::class)
fun ProjectAggregateState.renameTask(id: UUID, name: String): List<Event<ProjectAggregate>> {
    if (!tasks.contains(id)) {
        throw IllegalArgumentException("Task with id: $id does not exist in project")
    }

    return ArrayList(
        listOf(
            TaskUpdatedEvent(id),
            TaskNameChangedEvent(id, name)
        )
    )
}

fun ProjectAggregateState.changeStatus(id: UUID, newStatusId: UUID): List<Event<ProjectAggregate>> {
    val task = tasks[id] ?: throw IllegalArgumentException("Task with id: $id does not exist in project")

    return ArrayList(
        listOf(
            TaskUpdatedEvent(id),
            StatusChangedInTaskEvent(getId(), newStatusId, id)
        )
    )
}

fun ProjectAggregateState.addTaskExecutor(taskId: UUID, userId: UUID): List<Event<ProjectAggregate>> {
    if (!participants.contains(userId)) {
        throw IllegalArgumentException("User with id '$userId' not in project")
    }
    if (!tasks.contains(taskId)) {
        throw IllegalArgumentException("task with id '$taskId' not in project")
    }
    return ArrayList(
        listOf(
            TaskUpdatedEvent(taskId),
            TaskExecutorAddedEvent(getId(), taskId, tasks[taskId]!!.name, userId)
        )
    )
}
