package ru.quipy.projections.user_project_view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

// - Пользователь должен иметь возможность посмотреть свои проекты
// - Пользователь должен иметь возможность посмотреть свои задачи в проекте

data class ProjectModel (
    val id: UUID,
    val name: String
)

data class TaskModel (
    val id: UUID,
    var name: String
)

@Document("UserProject")
data class UserProjectViewEntity(
    @Id
    var userId: UUID,
    var projects: MutableList<UUID> = mutableListOf(),
    var tasks: MutableMap<UUID, MutableList<TaskModel>> = mutableMapOf()
)

@Repository
interface UserProjectViewRepository : MongoRepository<UserProjectViewEntity, UUID>
