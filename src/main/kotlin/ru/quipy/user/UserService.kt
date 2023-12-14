package ru.quipy.user

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.core.EventSourcingService
import ru.quipy.user.dto.UserLogin
import ru.quipy.user.dto.UserModel
import ru.quipy.user.dto.UserRegister
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.api.UserCreatedEvent
import ru.quipy.user.eda.logic.UserAggregateState
import ru.quipy.user.eda.logic.create
import java.lang.Exception
import java.util.*

interface UserService {
    fun createOne(data: UserRegister, userId: UUID): UserModel
    fun checkAvailableUsername(username: String)
    fun getOneByUsername(username: String): UserModel
    fun logIn(data: UserLogin): UserModel
    fun addProject(userId: UUID, projectId: UUID): UserModel
    fun addTask(userId: UUID, projectId: UUID, taskId: UUID): UserModel
}



@Service
class UserServiceImpl(
        private val userRepository: UserRepository
): UserService {

    override fun createOne(data: UserRegister, userId: UUID): UserModel {
        checkAvailableUsername(data.username)
        val dataEntity = data.toEntity()
        dataEntity.userId = userId
        val userEntity = userRepository.save(dataEntity)
        return userEntity.toModel()
    }

    override fun checkAvailableUsername(username: String) {
        var foundUser: UserModel? = null
        try {
            foundUser = getOneByUsername(username)
        } catch (e: Exception) {
            // skip if exists
        }
        if (foundUser != null) throw ResponseStatusException(HttpStatus.CONFLICT, "user already exists")
    }
    fun getOne(userId: UUID): UserEntity {
        return this.userRepository.findByIdOrNull(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
    }

    override fun getOneByUsername(username: String): UserModel {
        return this.userRepository.findByUsername(username)?.toModel()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
    }

    override fun addProject(userId: UUID, projectId: UUID): UserModel {
        val userEntity = getOne(userId)
        userEntity.projects.add(projectId)
        return this.userRepository.save(userEntity).toModel()
    }

    override fun addTask(userId: UUID, projectId: UUID, taskId: UUID): UserModel {
        val userEntity = getOne(userId)
        if (userEntity.tasks[projectId] == null) userEntity.tasks[projectId] = mutableSetOf<UUID>()
        userEntity.tasks[projectId]?.add(taskId)
        return this.userRepository.save(userEntity).toModel()
    }

    override fun logIn(data: UserLogin): UserModel {
        val userEntity: UserEntity =
                userRepository.findByUsername(data.username) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        if (comparePassword(userEntity.password, data.password)) {
            return userEntity.toModel()
        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "password does not match")
    }

    fun UserRegister.toEntity(): UserEntity =
            UserEntity(
                    userId = null,
                    username = this.username,
                    realName = this.realName,
                    password = this.password
            )

    fun UserEntity.toModel(): UserModel = kotlin.runCatching {
        UserModel(
                userId = this.userId!!,
                username = this.username,
                realName = this.realName,
                password = this.password,
                projects = this.projects,
                tasks = this.tasks

        )
    }.getOrElse { _ -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "some fields are missing") }


    private fun comparePassword(encodedPassword: String, newPassword: String): Boolean = BCryptPasswordEncoder().matches(newPassword, encodedPassword)
}