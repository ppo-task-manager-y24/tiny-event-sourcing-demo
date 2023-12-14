package ru.quipy.project

import org.springframework.web.bind.annotation.*
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Event
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.project.eda.logic.addUser
import ru.quipy.project.eda.logic.create
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
        val projectService: ProjectService
) {
    @PostMapping("/{title}")
    fun createProject(@PathVariable title: String, @RequestParam ownerId: UUID): ProjectAggregateState? {
        return projectService.createOne(ProjectCreate(
                id = UUID.randomUUID(),
                title = title,
                ownerId = ownerId)
        )
    }

    @GetMapping("/{id}")
    fun getProject(@PathVariable id: UUID): ProjectAggregateState? {
        return projectService.getOne(id)
    }

    @PostMapping("/{id}/{userId}")
    fun addUserToProject(@PathVariable id: UUID, @PathVariable userId: UUID): ProjectAggregateState? {
        return projectService.addUser(id, userId)
    }
}
