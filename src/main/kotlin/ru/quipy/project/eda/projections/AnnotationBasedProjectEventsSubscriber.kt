package ru.quipy.project.eda.projections

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import ru.quipy.project.ProjectRepository
import ru.quipy.project.eda.api.*
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.streams.annotation.SubscribeEvent
import ru.quipy.user.eda.api.UserAggregate
import java.util.UUID
import javax.annotation.PostConstruct

@Component
class UsersProjectEventsSubscriber(
    private val userProjectsRepository: ProjectRepository,
    private val subscriptionsManager: AggregateSubscriptionsManager) {

    val logger: Logger = LoggerFactory.getLogger(UsersProjectEventsSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(ProjectAggregate::class, "some-subscriber-name") {
            `when`(ProjectCreatedEvent::class) { event ->
                logger.info("Project created: {}", event.projectName)
            }
            `when`(ProjectParticipantAddedEvent::class) { event ->
                logger.info("Participant {} added for project {}", event.participantId, event.projectId)
            }
        }
    }

    private fun createOrUpdateUserProjects(userId: UUID, projectId: UUID, projectName: String) {
        var userProjects = userProjectsRepository.findByIdOrNull(projectId)
        if (userProjects == null)
            userProjects = UserProjects(userId)
        userProjects.projects[projectId] = Project(projectId, projectName)
        userProjectsRepository.save(userProjects)
    }

}