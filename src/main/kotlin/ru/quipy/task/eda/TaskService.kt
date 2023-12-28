package ru.quipy.task.eda

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Event
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.logic.TaskEntity
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.logic.addTaskExecutor
import ru.quipy.project.eda.logic.createTask
import ru.quipy.project.eda.logic.getTask
import ru.quipy.project.eda.logic.renameTask
import ru.quipy.task.dto.TaskCreate
import java.util.*

@Service
class TaskService(
    private val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>) {

    private val logger = LoggerFactory.getLogger(TaskService::class.java)

    fun getOne(projectId: UUID, taskId: UUID): TaskEntity? {
        return projectEsService.getState(projectId)?.getTask(taskId)
    }

    fun createOne(data: TaskCreate): TaskEntity? {
        val event = projectEsService.update(data.projectId) {
            it.createTask(
                data.id,
                data.name,
                data.description,
                data.statusId)
        }
        return getOne(data.projectId, event.taskId)
    }

    fun rename(projectId: UUID, taskId: UUID, name: String) : TaskEntity? {
        projectEsService.updateSerial(projectId) {
            it.renameTask(taskId, name)
        }
        return getOne(projectId, taskId)
    }

    fun addUser(projectId: UUID, taskId: UUID, userId: UUID): TaskEntity? {
        projectEsService.updateSerial(projectId) {
            it.addTaskExecutor(taskId, userId)
        }
        return getOne(projectId, taskId)
    }

}