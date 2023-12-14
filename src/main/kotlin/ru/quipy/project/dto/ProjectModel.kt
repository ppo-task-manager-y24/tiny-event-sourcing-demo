package ru.quipy.project.dto

import java.util.*

data class ProjectModel(
        val id: UUID,
        val participants: MutableList<UUID>,
        val name: String,
        val ownerId: UUID,
        val createdAt: Long,
        val updatedAt: Long,
        val tasks: MutableList<UUID>) {
}
