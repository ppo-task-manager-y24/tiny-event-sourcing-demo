package ru.quipy.projections.project_view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import java.util.*

@Document("project")
data class ProjectViewEntity(
        @Id
        var id: UUID,
        var participants: MutableList<UUID> = mutableListOf(),
        var name: String,
        var ownerId: UUID,
        var createdAt: Long = System.currentTimeMillis(),
        var updatedAt: Long = System.currentTimeMillis(),
        var tasks: MutableList<UUID> = mutableListOf(),
        var statuses: MutableList<UUID> = mutableListOf()
)

@Document("project-user")
data class ProjectUserViewEntity(
        @Id
        var id: UUID,
        var name: String
)

@Document("project-status")
data class ProjectStatusViewEntity(
        @Id
        var id: UUID,
        var projectId: UUID,
        var name: String,
        var color: Int
)

fun ProjectCreate.toEntity(): ProjectViewEntity =
        ProjectViewEntity(
                name = this.title,
                ownerId = this.ownerId,
                id = this.id
        )

fun ProjectModel.toEntity(): ProjectViewEntity =
        ProjectViewEntity(
                id = this.id,
                name = this.name,
                ownerId = this.ownerId,
                participants = this.participants,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt,
                tasks = this.tasks,
                statuses = this.statuses
        )

@Repository
interface ProjectViewRepository : MongoRepository<ProjectViewEntity, UUID>

@Repository
interface ProjectUserViewRepository : MongoRepository<ProjectUserViewEntity, UUID>

@Repository
interface ProjectStatusViewRepository : MongoRepository<ProjectStatusViewEntity, UUID> {
        fun findByProjectId(projectId: UUID): List<ProjectStatusViewEntity>
}
