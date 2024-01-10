package ru.quipy.projections.project_view

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.eda.api.*
import ru.quipy.status.eda.api.StatusAddedEvent
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusDeletedEvent
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.api.UserCreatedEvent
import javax.annotation.PostConstruct

@Component
class ProjectViewSubscriber (
        private val projectViewService: ProjectViewService,
        private val subscriptionsManager: AggregateSubscriptionsManager
) {
    private val logger = LoggerFactory.getLogger(ProjectViewSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "project::project-view") {
            `when`(ProjectCreatedEvent::class) { event ->
                projectViewService.createOne(ProjectCreate(title = event.projectName, ownerId = event.projectOwner, id = event.projectId))
                logger.info("Create project ${event.projectId}, owner - ${event.projectOwner}")
            }
            `when`(ProjectParticipantAddedEvent::class) { event ->
                projectViewService.addParticipant(event.projectId, event.participantId)
                logger.info("Add user ${event.participantId} to project ${event.projectId}")
            }
            `when`(TaskCreatedEvent::class) { event ->
                projectViewService.addTask(event.projectId, event.taskId)
                logger.info("Add task ${event.taskId} to project ${event.projectId}")
            }
        }

        subscriptionsManager.createSubscriber(StatusAggregate::class, "status::project-view") {
            `when`(StatusAddedEvent::class) { event ->
                projectViewService.addStatus(event.projectId, event.statusId, event.statusName, event.color)
                logger.info("Add status ${event.statusId} in project ${event.projectId}")
            }
            `when`(StatusDeletedEvent::class) { event ->
                projectViewService.deleteStatus(event.projectId, event.statusId)
                logger.info("Delete status ${event.statusId} in project ${event.projectId}")
            }
        }

        subscriptionsManager.createSubscriber(UserAggregate::class, "user::project-view") {
            `when`(UserCreatedEvent::class) { event ->
                projectViewService.addUser(event.userId, event.username)
                logger.info("Add user ${event.userId} with name ${event.username}")
            }
        }
    }
}
