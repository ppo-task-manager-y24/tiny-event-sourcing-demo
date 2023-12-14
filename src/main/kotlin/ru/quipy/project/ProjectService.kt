package ru.quipy.project

import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Event
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.logic.addUser
import ru.quipy.project.eda.logic.create
import java.util.*

@Service
class ProjectService(
        private val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>) {

    fun createOne(data: ProjectCreate): ProjectCreatedEvent {
        return projectEsService.create {
            it.create(
                    UUID.randomUUID(),
                    data.title,
                    data.ownerId)
        }
    }

    fun addUser(projectId: UUID, userId: UUID) : List<Event<ProjectAggregate>> {
        return projectEsService.updateSerial(projectId) {
            it.addUser(userId)
        }
    }
}
