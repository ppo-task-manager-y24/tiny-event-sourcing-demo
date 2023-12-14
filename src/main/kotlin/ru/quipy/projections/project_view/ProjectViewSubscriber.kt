package ru.quipy.projections.project_view

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.streams.AggregateSubscriptionsManager
import javax.annotation.PostConstruct

@Component
class ProjectViewSubscriber {
    private val logger = LoggerFactory.getLogger(ProjectViewSubscriber::class.java)

    @Autowired
    lateinit var subscriptionsManager: AggregateSubscriptionsManager

    @Autowired
    lateinit var projectViewService: ProjectViewService

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "projects::project-view") {
            `when`(ProjectCreatedEvent::class) { event ->
                projectViewService.createOne(ProjectCreate(title = event.projectName, ownerId = event.projectOwner))
                logger.info("Create project ${event.projectId}, owner - ${event.projectOwner}")
            }
            `when`(ProjectParticipantAddedEvent::class) { event ->
                projectViewService.addUser(event.projectId, event.participantId)
                logger.info("Add user ${event.participantId} to project ${event.projectId}")
            }
        }
    }
}