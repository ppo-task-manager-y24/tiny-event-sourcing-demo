package ru.quipy.status.dto

import java.util.*

data class StatusCreate(
        val projectId: UUID,
        val statusName: String,
        val color: String
)