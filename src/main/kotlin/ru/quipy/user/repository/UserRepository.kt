package ru.quipy.user.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import ru.quipy.user.logic.UserEntity

interface UserRepository: MongoRepository<UserEntity, String> {
    @Query("{username:  ?0}")
    fun findByUsername(username: String): UserEntity?
}