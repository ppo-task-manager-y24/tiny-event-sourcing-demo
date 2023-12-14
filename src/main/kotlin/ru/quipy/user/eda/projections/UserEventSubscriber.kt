package ru.quipy.user.eda.projections

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.task.eda.api.TaskAggregate
import ru.quipy.task.eda.api.TaskExecutorAddedEvent
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.api.UserAssignedToProjectEvent
import ru.quipy.user.eda.api.UserAssignedToTaskEvent
import ru.quipy.user.eda.api.UserCreatedEvent
import javax.annotation.PostConstruct

@Service
class UserEventSubscriber {

    val logger = LoggerFactory.getLogger(UserEventSubscriber::class.java)

    @Autowired
    lateinit var subscriptionsManager: AggregateSubscriptionsManager

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(UserAggregate::class, "some-subscriber-name") {

            `when`(UserCreatedEvent::class) { event ->
                logger.info("Task created: {}", event.userId)
            }
        }

        subscriptionsManager.createSubscriber(ProjectAggregate::class, "project-sub") {

            `when`(ProjectParticipantAddedEvent::class) { event ->
                logger.info("User {} assigned to project: {}", event.participantId, event.projectId)
            }
        }

        subscriptionsManager.createSubscriber(TaskAggregate::class, "task-sub") {


            `when`(TaskExecutorAddedEvent::class) { event ->
                logger.info("User {} assigned to task: {}", event.executorId, event.taskId)
            }
        }
    }
}
