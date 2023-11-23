package ru.quipy.user.logic

import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import ru.quipy.user.api.UserAggregate
import ru.quipy.user.api.UserCreatedEvent

import java.util.UUID

class UserAggregateState: AggregateState<UUID, UserAggregate> {
    private lateinit var id: UUID
    private lateinit var username: String
    private lateinit var name: String
    private lateinit var password: String
    private var createdAt: Long = System.currentTimeMillis()

    override fun getId() = id

    @StateTransitionFunc
    fun userCreatedApply(event: UserCreatedEvent) {
        id = event.userId
        username = event.username
        name = event.realName
        password = event.password
    }
}

data class UserEntity(
        val id: UUID = UUID.randomUUID(),
        val username: String,
        val realName: String,
        val passwd: String
)
