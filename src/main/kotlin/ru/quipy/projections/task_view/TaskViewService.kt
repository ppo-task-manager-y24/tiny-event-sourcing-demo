package ru.quipy.projections.task_view

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.dto.TaskModel
import java.util.*

@Service
class TaskViewService(
        private val taskViewRepository: TaskViewRepository) {

    private val logger = LoggerFactory.getLogger(TaskViewService::class.java)

    fun createOne(data: TaskCreate): TaskModel {
        val projectEntity = taskViewRepository.save(data.toEntity())
        return projectEntity.toModel()
    }

    fun getOne(id: UUID): TaskModel {
        val taskEntity =
                taskViewRepository.findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "project not found")
        return taskEntity.toModel()
    }

    fun addExecutor(taskId: UUID, userId: UUID) {
        val taskModel = getOne(taskId)
        taskModel.executors.add(userId)
        taskViewRepository.save(taskModel.toEntity())
    }

    fun editStatus(taskId: UUID, statusId: UUID) {
        val taskModel = getOne(taskId)
        taskModel.statusId = statusId
        taskViewRepository.save(taskModel.toEntity())
    }

    fun TaskViewEntity.toModel(): TaskModel = kotlin.runCatching {
        TaskModel(
                id = this.id,
                executors = this.executors,
                name = this.name,
                description = this.description,
                projectId = this.projectId,
                statusId = this.statusId,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt
        )
    }.getOrElse { _ -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "some fields are missing") }
}