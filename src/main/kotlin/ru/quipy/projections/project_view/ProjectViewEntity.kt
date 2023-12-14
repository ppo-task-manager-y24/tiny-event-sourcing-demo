package ru.quipy.projections.project_view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.quipy.project.dto.ProjectCreate
import java.util.*

@Document("project")
data class ProjectViewEntity(
        @Id
        var id: UUID = UUID.randomUUID(),
        var participants: MutableList<UUID> = mutableListOf(),
        var name: String,
        var ownerId: UUID,
        var createdAt: Long = System.currentTimeMillis(),
        var updatedAt: Long = System.currentTimeMillis()
)

fun ProjectCreate.toEntity(): ProjectViewEntity =
        ProjectViewEntity(
                name = this.title,
                ownerId = this.ownerId
        )
