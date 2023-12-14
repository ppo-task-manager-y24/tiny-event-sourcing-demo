package ru.quipy.task.dto

import org.springframework.data.annotation.Id
import java.util.*

data class TaskModel(
        var id: UUID,
        var executors: MutableList<UUID>,
        var name: String,
        var description: String,
        var projectId: UUID,
        var statusId: UUID,
        var createdAt: Long,
        var updatedAt: Long
)
