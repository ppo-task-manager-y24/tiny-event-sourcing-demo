package ru.quipy.status.eda.logic

import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.status.eda.api.*
import java.math.BigDecimal
import java.util.UUID


class StatusAggregateState: AggregateState<UUID, StatusAggregate> {
    private lateinit var projectId: UUID

    internal var statuses: MutableMap<UUID, Status> = mutableMapOf()
    override fun getId() = projectId

    @StateTransitionFunc
    fun statusCreatedApply(event: StatusCreatedEvent) {
        projectId = event.projectId

        statuses[defaultStatusId] = Status(defaultStatusId, event.projectId, defaultStatusName)
    }

    @StateTransitionFunc
    fun statusAddedApply(event: StatusAddedEvent) {
        statuses[event.statusId] = Status(
            id = event.statusId,
            projectId = event.projectId,
            statusName = event.statusName,
            color = event.color,
            createdAt = event.createdAt
        )
    }

    @StateTransitionFunc
    fun statusDeletedApply(event: StatusDeletedEvent) {
        statuses[event.statusId]?.isDeleted  = true
        statuses[event.statusId]?.updatedAt = System.currentTimeMillis()
    }

    @StateTransitionFunc
    fun usedInTaskApply(event: StatusUsedInTaskEvent) {
        statuses[event.statusId]?.usedTaskIds?.add(event.taskId)
    }

    @StateTransitionFunc
    fun removedFromTaskApply(event: StatusRemovedFromTaskEvent) {
        statuses[event.statusId]?.usedTaskIds?.remove(event.taskId)
    }

    @StateTransitionFunc
    fun statusChangedInTaskApply(event: StatusChangedInTaskEvent) {

    }

    companion object {
        const val defaultStatusName = "DEFAULT"
        val defaultStatusId = UUID.randomUUID()
    }
}

data class Status(
    val id: UUID = UUID.randomUUID(),
    val projectId: UUID,
    internal var statusName: String,
    internal var color: Int = 0,
    internal var isDeleted: Boolean = false,
    internal var usedTaskIds: MutableSet<UUID> = mutableSetOf(),
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun getStatusName() = statusName

    fun getColor() = color

    fun isDeleted() = isDeleted && usedTaskIds.isEmpty()
}