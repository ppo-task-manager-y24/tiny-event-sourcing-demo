package ru.quipy.user

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import ru.quipy.core.EventSourcingService
import ru.quipy.user.dto.UserLogin
import ru.quipy.user.dto.UserModel
import ru.quipy.user.dto.UserRegister
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.logic.UserAggregateState
import ru.quipy.user.eda.logic.create
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>,
    private val userService: UserService
) {

    @PostMapping
    fun register(@RequestBody request: UserRegister): UserAggregateState? {
        userService.checkAvailableUsername(request.username)
        val event = userEsService.create { it.create(
            UUID.randomUUID(),
            request.username,
            request.realName,
            BCryptPasswordEncoder().encode(request.password)) }
        return userEsService.getState(event.userId)
    }

    @GetMapping("/{username}")
    fun getUser(@PathVariable username: String): UserModel {
        return userService.getOneByUsername(username)
    }

    @GetMapping("/login")
    fun login(@RequestBody request: UserLogin): UserModel = userService.logIn(request)
}