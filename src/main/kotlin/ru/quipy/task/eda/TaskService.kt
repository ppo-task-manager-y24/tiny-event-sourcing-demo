package ru.quipy.task.eda

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Event
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.eda.api.TaskAggregate
import ru.quipy.task.eda.api.TaskCreatedEvent
import ru.quipy.task.eda.logic.TaskAggregateState
import ru.quipy.task.eda.logic.addExecutor
import ru.quipy.task.eda.logic.create
import ru.quipy.task.eda.logic.rename
import java.util.*

@Service
class TaskService(
        private val taskEsService: EventSourcingService<UUID, TaskAggregate, TaskAggregateState>) {

    private val logger = LoggerFactory.getLogger(TaskService::class.java)

    fun getOne(id: UUID): TaskAggregateState? {
        return taskEsService.getState(id)
    }

    fun createOne(data: TaskCreate): TaskAggregateState? {
        val event = taskEsService.create {
            it.create(data.name,
                    data.description,
                    data.projectId,
                    data.statusId)
        }
        return getOne(event.taskId);
    }

    fun rename(id: UUID, name: String) : TaskAggregateState? {
        taskEsService.updateSerial(id) {
            it.rename(name)
        }
        return getOne(id)
    }

    fun addUser(taskId: UUID, userId: UUID): TaskAggregateState? {
        taskEsService.updateSerial(taskId) {
            it.addExecutor(userId)
        }
        return getOne(taskId)
    }
}