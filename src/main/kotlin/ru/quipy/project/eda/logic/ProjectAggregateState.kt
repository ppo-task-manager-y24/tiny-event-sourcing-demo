package ru.quipy.logic

import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.project.eda.api.ProjectUpdatedEvent
import ru.quipy.project.eda.api.TaskCreatedEvent
import ru.quipy.project.eda.api.TaskExecutorAddedEvent
import ru.quipy.project.eda.api.TaskNameChangedEvent
import ru.quipy.project.eda.api.TaskUpdatedEvent
import java.util.*

// Service's business logic
class ProjectAggregateState : AggregateState<UUID, ProjectAggregate> {
    private lateinit var projectId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    lateinit var name: String
    lateinit var owner: UUID
    var participants: MutableList<UUID> = mutableListOf()
    var tasks = mutableMapOf<UUID, TaskEntity>()

    override fun getId() = projectId

    // State transition functions which is represented by the class member function
    @StateTransitionFunc
    fun projectCreatedApply(event: ProjectCreatedEvent) {
        projectId = event.projectId
        name = event.projectName
        owner = event.projectOwner
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun projectUpdatedApply(event: ProjectUpdatedEvent) {
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun projectParticipantAddedApply(event: ProjectParticipantAddedEvent) {
        participants.add(event.participantId)
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun taskCreatedApply(event: TaskCreatedEvent) {
        tasks[event.taskId] = TaskEntity(
            event.taskId,
            event.taskName,
            event.taskDescription,
            event.statusId,
            event.projectId)
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun taskUpdatedApply(event: TaskUpdatedEvent) {
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun taskNameChangedApply(event: TaskNameChangedEvent) {
        tasks[event.taskId]?.name  = event.taskName
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun taskExecutorAddedApply(event: TaskExecutorAddedEvent) {
        tasks[event.taskId]?.executors?.add(event.executorId)
        updatedAt = event.createdAt
    }
}

data class TaskEntity(
    val id: UUID = UUID.randomUUID(),
    var name: String,
    val description: String,
    val statusId: UUID,
    val projectId: UUID,
    var executors: MutableList<UUID> = mutableListOf()
)
