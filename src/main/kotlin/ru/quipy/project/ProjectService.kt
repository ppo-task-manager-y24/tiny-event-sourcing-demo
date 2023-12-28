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
import java.util.*

@Service
class ProjectService(
    private val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>) {

    fun getOne(id: UUID): ProjectAggregateState? {
        return projectEsService.getState(id)
    }

    fun createOne(data: ProjectCreate): ProjectAggregateState? {
        val event = projectEsService.create {
            it.create(
                    data.id,
                    data.title,
                    data.ownerId)
        }
        return getOne(event.projectId)
    }

    fun addUser(projectId: UUID, userId: UUID) : ProjectAggregateState? {
        projectEsService.updateSerial(projectId) {
            it.addUser(userId)
        }
        return getOne(projectId)
    }
}
