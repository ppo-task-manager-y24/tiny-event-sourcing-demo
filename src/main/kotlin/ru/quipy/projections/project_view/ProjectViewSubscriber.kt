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
import javax.annotation.PostConstruct

@Component
class ProjectViewSubscriber (
        private val projectViewService: ProjectViewService,
        private val subscriptionsManager: AggregateSubscriptionsManager) {
    private val logger = LoggerFactory.getLogger(ProjectViewSubscriber::class.java)

//    @Autowired
//    lateinit var subscriptionsManager: AggregateSubscriptionsManager
//
//    @Autowired
//    lateinit var projectViewService: ProjectViewService

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "projects::project-view") {
            `when`(ProjectCreatedEvent::class) { event ->
                projectViewService.createOne(ProjectCreate(title = event.projectName, ownerId = event.projectOwner, id = event.projectId))
                logger.error("Create project ${event.projectId}, owner - ${event.projectOwner}")
            }
            `when`(ProjectParticipantAddedEvent::class) { event ->
                projectViewService.addUser(event.projectId, event.participantId)
                logger.error("Add user ${event.participantId} to project ${event.projectId}")
            }
        }
    }
}

//@Service
//@AggregateSubscriber(
//        aggregateClass = ProjectAggregate::class, subscriberName = "demo-subs-stream"
//)
//class ProjectViewSubscriber {
//    private val logger = LoggerFactory.getLogger(ProjectViewSubscriber::class.java)
//
//    @SubscribeEvent
//    fun taskCreatedSubscriber(event: ProjectCreatedEvent) {
//        throw error("fd")
//        logger.error("Task created: {}", event.projectId)
//    }
//
////    @SubscribeEvent
////    fun tagCreatedSubscriber(event: TagCreatedEvent) {
////        logger.info("Tag created: {}", event.tagName)
////    }
//}