package ru.quipy.user

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document("users")
data class UserEntity(
    @Id
    var userId: UUID?,
    var username: String,
    var realName: String,
    var password: String,
    var projects: MutableSet<UUID> = mutableSetOf<UUID>(),
    var tasks: MutableMap<UUID, MutableSet<UUID>> = mutableMapOf<UUID, MutableSet<UUID>>(),
    var createdAt: Long = System.currentTimeMillis()
) {
}
