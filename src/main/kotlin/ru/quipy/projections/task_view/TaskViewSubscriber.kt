package ru.quipy.projections.task_view

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.eda.api.TaskAggregate
import ru.quipy.task.eda.api.TaskCreatedEvent
import ru.quipy.task.eda.api.TaskExecutorAddedEvent
import java.util.UUID
import javax.annotation.PostConstruct

@Component
class TaskViewSubscriber (
        private val taskViewService: TaskViewService,
        private val subscriptionsManager: AggregateSubscriptionsManager
) {
    private val logger = LoggerFactory.getLogger(TaskViewSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(TaskAggregate::class, "projects::task-view") {
            `when`(TaskCreatedEvent::class) { event ->
                taskViewService.createOne(TaskCreate(id = event.taskId, name = event.taskName, description = event.taskDescription, projectId = event.projectId, statusId = event.statusId))
                logger.info("Create task ${event.taskId}, project - ${event.projectId}")
            }
            `when`(TaskExecutorAddedEvent::class) { event ->
                taskViewService.addExecutor(event.taskId, event.executorId)
                logger.info("Add user ${event.executorId} to task ${event.taskId}")
            }
//            `when`(Tasksta::class) { event ->
//                taskViewService.addExecutor(event.taskId, event.executorId)
//                logger.info("Add user ${event.executorId} to task ${event.taskId}")
//            }
        }

    }
}