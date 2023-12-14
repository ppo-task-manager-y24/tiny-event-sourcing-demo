package ru.quipy.user.eda.logic

import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.task.eda.api.TaskExecutorAddedEvent
import ru.quipy.user.dto.UserModel
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.api.UserCreatedEvent

import java.util.UUID

class UserAggregateState: AggregateState<UUID, UserAggregate> {
    private lateinit var userId: UUID
    private lateinit var username: String
    private lateinit var realName: String
    private lateinit var password: String
    var projects = mutableSetOf<UUID>()
    var tasks = mutableMapOf<UUID, MutableSet<UUID>>()
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    override fun getId() = userId


    @StateTransitionFunc
    fun userCreatedApply(event: UserCreatedEvent) {
        userId = event.userId
        username = event.username
        realName = event.realName
        password = event.password
        createdAt = event.createdAt
    }
}
