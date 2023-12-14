package ru.quipy.projections.task_view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.dto.TaskModel
import java.util.*

@Document("project")
data class TaskViewEntity(
        @Id
        var id: UUID,
        var executors: MutableList<UUID> = mutableListOf(),
        var name: String,
        var description: String,
        var projectId: UUID,
        var statusId: UUID,
        var createdAt: Long = System.currentTimeMillis(),
        var updatedAt: Long = System.currentTimeMillis()
)

fun TaskCreate.toEntity(): TaskViewEntity =
        TaskViewEntity(
                id = this.id,
                name = this.name,
                description = this.description,
                projectId = this.projectId,
                statusId = this.statusId
        )

fun TaskModel.toEntity(): TaskViewEntity =
        TaskViewEntity(
                id = this.id,
                executors = this.executors,
                name = this.name,
                description = this.description,
                projectId = this.projectId,
                statusId = this.statusId,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt
        )
