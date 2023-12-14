package ru.quipy.projections.project_view

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.streams.annotation.SubscribeEvent
import ru.quipy.task.eda.api.TaskAggregate
import ru.quipy.task.eda.api.TaskCreatedEvent
import ru.quipy.task.eda.logic.TaskAggregateState
import javax.annotation.PostConstruct

@Component
class ProjectViewSubscriber (
        private val projectViewService: ProjectViewService,
        private val subscriptionsManager: AggregateSubscriptionsManager
) {
    private val logger = LoggerFactory.getLogger(ProjectViewSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "projects::project-view") {
            `when`(ProjectCreatedEvent::class) { event ->
                projectViewService.createOne(ProjectCreate(title = event.projectName, ownerId = event.projectOwner, id = event.projectId))
                logger.info("Create project ${event.projectId}, owner - ${event.projectOwner}")
            }
            `when`(ProjectParticipantAddedEvent::class) { event ->
                projectViewService.addUser(event.projectId, event.participantId)
                logger.info("Add user ${event.participantId} to project ${event.projectId}")
            }
        }

        subscriptionsManager.createSubscriber(TaskAggregate::class, "task::project-view") {
            `when`(TaskCreatedEvent::class) { event ->
                projectViewService.addTask(event.projectId, event.taskId)
                logger.info("Add task ${event.taskId} to project ${event.projectId}")
            }
        }
    }
}
