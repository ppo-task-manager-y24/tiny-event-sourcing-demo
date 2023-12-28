package ru.quipy.project.dto

import java.util.*

data class ProjectCreate(
    var id: UUID,
    val title: String,
    val ownerId: UUID
)

