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

    fun createOne(data: TaskCreate): TaskCreatedEvent {
        logger.error("createOne")
        return taskEsService.create {
            it.create(data.name,
                    data.description,
                    data.projectId,
                    data.statusId)
        }
    }

    fun rename(id: UUID, name: String) : List<Event<TaskAggregate>> {
        return taskEsService.updateSerial(id) {
            it.rename(name)
        }
    }

    fun addUser(taskId: UUID, userId: UUID): List<Event<TaskAggregate>> {
        return taskEsService.updateSerial(taskId) {
            it.addExecutor(userId)
        }
    }
}