package ru.quipy.task.eda.logic

import ru.quipy.task.eda.api.*
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

class TaskAggregateState : AggregateState<UUID, TaskAggregate> {
    private lateinit var taskId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    lateinit var name: String
    lateinit var description: String
    lateinit var projectId: UUID
    lateinit var statusId: UUID
    var executors: MutableList<UUID> = mutableListOf()

    override fun getId() = taskId

    @StateTransitionFunc
    fun taskCreatedApply(event: TaskCreatedEvent) {
        taskId = event.taskId
        updatedAt = event.createdAt
        name = event.taskName
        description = event.taskDescription
        projectId = event.projectId
        statusId = event.statusId
    }

    @StateTransitionFunc
    fun taskUpdatedApply(event: TaskUpdatedEvent) {
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun taskNameChangedApply(event: TaskNameChangedEvent) {
        name = event.taskName
        updatedAt = event.createdAt
    }

    @StateTransitionFunc
    fun taskExecutorAddedApply(event: TaskExecutorAddedEvent) {
        executors.add(event.executorId)
        updatedAt = event.createdAt
    }
}