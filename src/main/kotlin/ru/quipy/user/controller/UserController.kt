package ru.quipy.user.controller

import org.springframework.web.bind.annotation.*

import ru.quipy.user.api.UserCreatedEvent
import ru.quipy.user.dto.UserDto
import ru.quipy.user.service.UserEsService

@RestController
@RequestMapping("/users")
class UserController(
        val userEsService: UserEsService
) {
    @PostMapping("/")
    fun createUser(@RequestBody user: UserDto): UserCreatedEvent {
        return userEsService.createUser(user)
    }
}