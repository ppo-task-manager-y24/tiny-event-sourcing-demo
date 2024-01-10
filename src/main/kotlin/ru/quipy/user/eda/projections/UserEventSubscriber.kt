package ru.quipy.user.eda.projections

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.project.eda.api.TaskExecutorAddedEvent
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.user.UserService
import ru.quipy.user.dto.UserRegister
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.api.UserCreatedEvent
import javax.annotation.PostConstruct

@Service
class UserEventSubscriber {

    val logger = LoggerFactory.getLogger(UserEventSubscriber::class.java)

    @Autowired
    lateinit var subscriptionsManager: AggregateSubscriptionsManager

    @Autowired
    lateinit var userService: UserService

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(UserAggregate::class, "some-subscriber-name") {

            `when`(UserCreatedEvent::class) { event ->
                userService.createOne(
                    UserRegister(
                        event.username,
                        event.password,
                        event.realName
                    ),
                    event.userId
                )
                logger.info("Task created: {}", event.userId)
            }
        }

        subscriptionsManager.createSubscriber(ProjectAggregate::class, "project-sub") {

            `when`(ProjectParticipantAddedEvent::class) { event ->
                userService.addProject(event.participantId, event.projectId)
                logger.info("User {} assigned to project: {}", event.participantId, event.projectId)
            }
        }

        subscriptionsManager.createSubscriber(ProjectAggregate::class, "task-sub") {


            `when`(TaskExecutorAddedEvent::class) { event ->
                userService.addTask(event.executorId, event.projectId, event.taskId)
                logger.info("User {} assigned to task: {}", event.executorId, event.taskId)
            }
        }
    }
}
