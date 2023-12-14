package ru.quipy.projections.project_view

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import java.util.*

@Service
class ProjectViewService(
        private val projectViewRepository: ProjectViewRepository) {

    fun createOne(data: ProjectCreate): ProjectModel {
        val projectEntity = projectViewRepository.save(data.toEntity())
        return projectEntity.toModel()
    }

    fun getOne(id: UUID): ProjectModel {
        val projectEntity: ProjectViewEntity =
                projectViewRepository.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "project not found")
        return projectEntity.toModel()
    }

    fun addUser(projectId: UUID, userId: UUID) {
        val projectEntity = getOne(projectId)
        projectEntity.participants.add(userId)
    }

    fun ProjectViewEntity.toModel(): ProjectModel = kotlin.runCatching {
        ProjectModel(
                id = this.id,
                participants = this.participants,
                name = this.name,
                ownerId = this.ownerId,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt
        )
    }.getOrElse { _ -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "some fields are missing") }
}