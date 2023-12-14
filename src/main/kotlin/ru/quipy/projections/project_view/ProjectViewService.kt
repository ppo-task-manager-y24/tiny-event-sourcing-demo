package ru.quipy.projections.project_view

import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(ProjectViewService::class.java)

    fun createOne(data: ProjectCreate): ProjectModel {
        logger.error("createOne1")
        val projectEntity = projectViewRepository.save(data.toEntity())
        return projectEntity.toModel()
    }

    fun getOne(id: UUID): ProjectModel {
        val projectEntity: ProjectViewEntity =
                projectViewRepository.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "project not found")
        return projectEntity.toModel()
    }

    fun addUser(projectId: UUID, userId: UUID) {
        val projectModel = getOne(projectId)
        projectModel.participants.add(userId)
        projectViewRepository.save(projectModel.toEntity())
    }

    fun addTask(projectId: UUID, taskId: UUID) {
        val projectModel = getOne(projectId)
        projectModel.tasks.add(taskId)
        projectViewRepository.save(projectModel.toEntity())
    }

    fun getAllTasks(projectId: UUID) : MutableList<UUID> {
        val projectModel = getOne(projectId)
        return projectModel.tasks
    }

    fun ProjectViewEntity.toModel(): ProjectModel = kotlin.runCatching {
        ProjectModel(
                id = this.id,
                participants = this.participants,
                name = this.name,
                ownerId = this.ownerId,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt,
                tasks = this.tasks
        )
    }.getOrElse { _ -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "some fields are missing") }
}