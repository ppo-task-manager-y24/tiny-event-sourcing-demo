package ru.quipy.project.eda.logic

import ru.quipy.domain.Event
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.logic.TaskEntity
import ru.quipy.project.eda.api.*
import java.util.*


// Commands : takes something -> returns event
// Here the commands are represented by extension functions, but also can be the class member functions

fun ProjectAggregateState.create(id: UUID, name: String, ownerId: UUID): ProjectCreatedEvent {
    return ProjectCreatedEvent(
        projectId = id,
        projectName = name,
        projectOwner = ownerId,
    )
}

fun ProjectAggregateState.addUser(id: UUID): List<Event<ProjectAggregate>> {
    if(participants.contains(id)) {
        throw IllegalArgumentException("User with id: $id already exists in project")
    }
    return ArrayList(
        listOf(
            ProjectParticipantAddedEvent(this.getId(), id),
            ProjectUpdatedEvent(this.getId())
        )
    )
}

fun ProjectAggregateState.createTask(taskId: UUID, name: String, description: String, statusId: UUID): TaskCreatedEvent {
    return TaskCreatedEvent(
        taskId = taskId,
        taskName = name,
        taskDescription = description,
        projectId = this.getId(),
        statusId = statusId
    )
}

fun ProjectAggregateState.renameTask(id: UUID, name: String): List<Event<ProjectAggregate>> {
    return ArrayList(
        listOf(
            TaskUpdatedEvent(id),
            TaskNameChangedEvent(id, name)
        )
    )
}

fun ProjectAggregateState.addTaskExecutor(taskId: UUID, userId: UUID): List<Event<ProjectAggregate>> {
    if (!participants.contains(userId)) {
        throw IllegalArgumentException("User with id '$userId' not in project")
    }
    return ArrayList(
        listOf(
            TaskUpdatedEvent(taskId),
            TaskExecutorAddedEvent(taskId, userId)
        )
    )
}

fun ProjectAggregateState.getTask(taskId: UUID): TaskEntity? {
    return tasks[taskId]
}
