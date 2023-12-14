package ru.quipy.projections.project_view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
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
        var tasks: MutableList<UUID> = mutableListOf()
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
                tasks = this.tasks
        )
