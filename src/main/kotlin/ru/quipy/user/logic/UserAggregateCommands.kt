package ru.quipy.user.logic

import ru.quipy.user.api.UserCreatedEvent
import java.util.*

fun UserAggregateState.create(
        id: UUID, username: String, realName: String, passwd: String
): UserCreatedEvent {
    return UserCreatedEvent(id, username, realName, passwd)
}