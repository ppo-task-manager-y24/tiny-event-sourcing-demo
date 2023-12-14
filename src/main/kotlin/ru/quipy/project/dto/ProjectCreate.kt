package ru.quipy.project.dto

import java.util.*

data class ProjectCreate(
        val title: String,
        val ownerId: UUID
)

