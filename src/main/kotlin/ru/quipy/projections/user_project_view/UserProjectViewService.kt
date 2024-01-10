package ru.quipy.projections.user_project_view

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.quipy.projections.project_view.ProjectViewService
import ru.quipy.projections.task_view.TaskViewService
import java.util.*

@Service
class UserProjectViewService(
    private val userProjectViewRepository: UserProjectViewRepository,
    private val projectViewService: ProjectViewService,
    private val taskViewService: TaskViewService
) {
    private val logger = LoggerFactory.getLogger(UserProjectViewService::class.java)

    fun createOne(userId: UUID): UserProjectViewEntity {
        return userProjectViewRepository.save(UserProjectViewEntity(userId = userId))
    }

    fun getOne(userId: UUID): UserProjectViewEntity {
        return userProjectViewRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("No user with $userId in view")
    }

    fun getProjects(userId: UUID): List<ProjectModel> {
        val record = getOne(userId)
        return record.projects.map { id -> ProjectModel(id, projectViewService.getOne(id).name) }
    }

    fun getProjectsIds(userId: UUID): MutableList<UUID> {
        val record = getOne(userId)
        return record.projects
    }

    fun getTasks(userId: UUID, projectId: UUID): MutableList<TaskModel> {
        val record = getOne(userId)
        if (!record.tasks.contains(projectId)) {
            throw IllegalArgumentException("No project with $projectId for user with $userId in view")
        }
        return record.tasks.getValue(projectId)
    }

    fun addProject(userId: UUID, projectId: UUID) {
        val projects = getProjectsIds(userId)
        if (projects.contains(projectId)) {
            throw IllegalArgumentException("Project $projectId for user $userId already exist")
        }
        projects.add(projectId)

        val record = getOne(userId)
        record.projects = projects
        userProjectViewRepository.save(record)
    }

    fun addTask(userId: UUID, projectId: UUID, taskId: UUID, taskName: String) {
        val tasks = getTasks(userId, projectId)
        if (tasks.any {obj -> obj.id == taskId}) {
            throw IllegalArgumentException("Task $taskId in project $projectId for user $userId already exist")
        }
        tasks.add(TaskModel(taskId, taskViewService.getTask(taskId).name))

        val record = getOne(userId)
        record.tasks[projectId]!!.add(TaskModel(taskId, taskName))
        userProjectViewRepository.save(record)
    }

    fun renameTask(taskId: UUID, newName: String) {
        val task = taskViewService.getTask(taskId)
        task.executors.forEach {
            val userTasks = getTasks(it, task.projectId)
            if (!userTasks.any { obj -> obj.id == taskId }) {
                throw IllegalArgumentException("No task with $taskId in project ${task.projectId} for user with $it in view")
            }
            val i = userTasks.indexOfFirst { obj -> obj.id == taskId }
            userTasks[i].name = newName

            val record = getOne(it)
            record.tasks[task.projectId] = userTasks
            userProjectViewRepository.save(record)
        }
    }
}
