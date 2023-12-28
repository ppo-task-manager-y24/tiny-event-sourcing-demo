package ru.quipy.task.dto

import java.util.*

data class TaskCreate(
        val id: UUID,
        val name: String,
        val description: String,
        var projectId: UUID,
        val statusId: UUID
)
