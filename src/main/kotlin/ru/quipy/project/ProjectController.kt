package ru.quipy.project

import org.springframework.web.bind.annotation.*
import ru.quipy.domain.Event
import ru.quipy.project.eda.logic.ProjectAggregateState
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import java.util.*

@RestController
@RequestMapping("/projects")
class ProjectController(
        val projectService: ProjectService
) {
    @PostMapping("/{title}")
    fun createProject(@PathVariable title: String, @RequestParam ownerId: UUID): ProjectCreatedEvent {
        return projectService.createProject(ProjectCreate(
                id = UUID.randomUUID(),
                title = title,
                ownerId = ownerId)
        )
    }

    @GetMapping("/{id}")
    fun getProject(@PathVariable id: UUID): ProjectAggregateState? {
        return projectService.state(id)
    }

    @PostMapping("/{id}/{userId}")
    fun addUserToProject(@PathVariable id: UUID, @PathVariable userId: UUID): List<Event<ProjectAggregate>> {
        return projectService.addUserToProject(id, userId)
    }
}
