package ru.quipy.project

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Event
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.logic.addUser
import ru.quipy.project.eda.logic.create
import java.lang.IllegalStateException
import java.util.*

@Service
class ProjectService(
    private val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>) {

    fun state(id: UUID): ProjectAggregateState? {
        return projectEsService.getState(id)
    }

    fun createProject(data: ProjectCreate): ProjectCreatedEvent {
        return projectEsService.create { state ->
            state.create(
                    data.id,
                    data.title,
                    data.ownerId
            )
        }
    }

    @Throws(IllegalArgumentException::class)
    fun addUserToProject(projectId: UUID, userId: UUID) : List<Event<ProjectAggregate>> {
        return projectEsService.updateSerial(projectId) {
            it.addUser(userId)
        }
    }
}
