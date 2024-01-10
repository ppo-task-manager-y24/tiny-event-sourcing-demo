package ru.quipy.projections.project_view

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import ru.quipy.project.dto.TaskModel
import ru.quipy.projections.task_view.TaskViewService
import ru.quipy.status.dto.StatusViewModel
import ru.quipy.status.eda.projections.status_view.StatusViewService
import java.util.*

// - Пользователь должен иметь возможность посмотреть участников в проект
// - Пользователь должен иметь возможность посмотреть задачи в проекте
// - Пользователь должен иметь возможность посмотреть все статусы в проекте

data class ProjectViewModel(
    var id: UUID,
    var participants: List<String> = mutableListOf(),
    var name: String,
    var owner: String,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var tasks: List<String> = mutableListOf(),
    var statuses: List<String> = mutableListOf()
)

@Service
class ProjectViewService(
    private val projectViewRepository: ProjectViewRepository,
    private val projectUserViewRepository: ProjectUserViewRepository,
    private val taskViewService: TaskViewService,
    private val statusViewService: StatusViewService,
    private val projectStatusViewRepository: ProjectStatusViewRepository
) {

    private val logger = LoggerFactory.getLogger(ProjectViewService::class.java)

    fun createOne(data: ProjectCreate): ProjectModel {
        val projectEntity = projectViewRepository.save(data.toEntity())
        return projectEntity.toModel()
    }

    fun getOne(id: UUID): ProjectModel {
        val projectEntity: ProjectViewEntity =
                projectViewRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("No project with $id in view")
        return projectEntity.toModel()
    }

    fun getProject(id: UUID): ProjectViewModel {
        val projectEntity = getOne(id)
        return ProjectViewModel(
            id = id,
            participants = getParticipants(id),
            name = projectEntity.name,
            owner = getUserName(projectEntity.ownerId),
            createdAt = projectEntity.createdAt,
            updatedAt = projectEntity.updatedAt,
            tasks = getTasks(id).map { model -> model.name },
            statuses = getStatuses(id)
        )
    }

    fun addParticipant(projectId: UUID, userId: UUID) {
        val projectModel = getOne(projectId)
        projectModel.participants.add(userId)
        projectViewRepository.save(projectModel.toEntity())
    }

    fun addTask(projectId: UUID, taskId: UUID) {
        val projectModel = getOne(projectId)
        projectModel.tasks.add(taskId)
        projectViewRepository.save(projectModel.toEntity())
    }

    fun getTaskIds(projectId: UUID) : MutableList<UUID> {
        val projectModel = getOne(projectId)
        return projectModel.tasks
    }

    fun getTasks(projectId: UUID) : List<TaskModel> {
        return getTaskIds(projectId).map { id -> taskViewService.getTask(id) }
    }

    fun addUser(userId: UUID, name: String) {
        projectUserViewRepository.save(ProjectUserViewEntity(id = userId, name = name))
    }

    fun getParticipantIds(projectId: UUID) : MutableList<UUID> {
        return getOne(projectId).participants
    }

    private fun getUserName(id: UUID): String {
        return (projectUserViewRepository.findByIdOrNull(id) ?: throw IllegalArgumentException("No user with $id in view")).name
    }

    fun getParticipants(projectId: UUID) : List<String> {
        return getParticipantIds(projectId).map { id ->
            getUserName(id)
        }
    }

    fun addStatus(projectId: UUID, statusId: UUID, name: String, color: Int) {
        if (projectStatusViewRepository.existsById(statusId)) {
            throw IllegalArgumentException("Status $statusId already exist")
        }
        projectStatusViewRepository.save(ProjectStatusViewEntity(statusId, projectId, name, color))
    }

    fun deleteStatus(projectId: UUID, statusId: UUID) {
        projectStatusViewRepository.deleteById(statusId)
//        val projectModel = getOne(projectId)
//        val isRemoved = projectModel.statuses.remove(statusId)
//        logger.info("isRemoved - $isRemoved")
//        projectViewRepository.save(projectModel.toEntity())
    }

    fun getStatusIds(projectId: UUID) : List<UUID> {
        return projectStatusViewRepository.findByProjectId(projectId).map { entity ->
            entity.id
        }
    }

    fun getStatuses(projectId: UUID) : List<String> {
        return projectStatusViewRepository.findByProjectId(projectId).map { entity ->
            entity.name
        }
    }

    fun ProjectViewEntity.toModel(): ProjectModel = kotlin.runCatching {
        ProjectModel(
            id = this.id,
            participants = this.participants,
            name = this.name,
            ownerId = this.ownerId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            tasks = this.tasks,
            statuses = this.statuses
        )
    }.getOrElse { _ -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "some fields are missing") }
}