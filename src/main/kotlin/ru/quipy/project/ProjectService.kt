package ru.quipy.project

import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Event
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.TaskCreate
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.logic.*
import java.util.*

@Service
class ProjectService(
    private val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>) {

    // test only
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

    // test only
    fun getTask(projectId: UUID, taskId: UUID): TaskEntity? {
        return state(projectId)?.tasks?.get(taskId)
    }

    fun createTask(data: TaskCreate): List<Event<ProjectAggregate>> {
        return projectEsService.updateSerial(data.projectId) {
            it.createTask(
                data.id,
                data.name,
                data.description,
                data.statusId
            )
        }
    }

    fun renameTask(projectId: UUID, taskId: UUID, name: String) : List<Event<ProjectAggregate>> {
        return projectEsService.updateSerial(projectId) {
            it.renameTask(taskId, name)
        }
    }

    fun addUserToTask(projectId: UUID, taskId: UUID, userId: UUID): List<Event<ProjectAggregate>> {
        return projectEsService.updateSerial(projectId) {
            it.addTaskExecutor(taskId, userId)
        }
    }
}
