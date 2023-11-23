package ru.quipy.user.service

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.core.EventSourcingService
import ru.quipy.user.api.UserAggregate
import ru.quipy.user.api.UserCreatedEvent
import ru.quipy.user.dto.UserDto
import ru.quipy.user.logic.UserAggregateState
import ru.quipy.user.logic.UserEntity
import ru.quipy.user.logic.create
import ru.quipy.user.repository.UserRepository
import java.util.*

@Service
class UserEsService(
        private val repository: UserRepository,
        private val esService: EventSourcingService<UUID, UserAggregate, UserAggregateState>
) {
    fun createUser(user: UserDto): UserCreatedEvent {
        var foundUser: UserEntity? = null

        try {
            foundUser = getUser(user.username)
        } catch (e: Exception) {
            //skip if not exists
        }

        if (foundUser != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "user already exists")
        }

        val entity = UserEntity(UUID.randomUUID(), user.username, user.realName, BCryptPasswordEncoder().encode(user.passwd))

        repository.save(entity)

        return esService.create { it.create(entity.id, entity.username, entity.realName, entity.passwd) }
    }

    fun getUser(username: String): UserEntity {
        return repository.findByUsername(username) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
    }


}