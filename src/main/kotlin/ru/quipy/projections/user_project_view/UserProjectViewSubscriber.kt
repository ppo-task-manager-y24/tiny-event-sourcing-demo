package ru.quipy.projections.user_project_view

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.quipy.project.eda.api.*
import ru.quipy.projections.task_view.TaskViewService
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.api.UserCreatedEvent
import javax.annotation.PostConstruct

@Component
class UserProjectViewSubscriber(
    private val userProjectViewService: UserProjectViewService,
    private val subscriptionsManager: AggregateSubscriptionsManager
) {
    private val logger = LoggerFactory.getLogger(UserProjectViewSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(UserAggregate::class, "user::user-project-view") {
            `when`(UserCreatedEvent::class) { event ->
                userProjectViewService.createOne(event.userId)
                logger.info("Create user-project-view record for user ${event.userId}")
            }
        }
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "project::user-project-view") {
            `when`(ProjectCreatedEvent::class) { event ->
                userProjectViewService.addProject(event.projectOwner, event.projectId)
                logger.info("Project ${event.projectId} added, owner user ${event.projectOwner}")
            }
            `when`(TaskExecutorAddedEvent::class) { event ->
                userProjectViewService.addTask(event.executorId, event.projectId, event.taskId, event.taskName)
                logger.info("Executor for task ${event.taskId} (${event.taskName}) added, project ${event.projectId}, user ${event.executorId}")
            }
            `when`(TaskNameChangedEvent::class) { event ->
                userProjectViewService.renameTask(event.taskId, event.taskName)
                logger.info("Task ${event.taskId} renamed, new name '${event.taskName}'")
            }
        }
    }
}
