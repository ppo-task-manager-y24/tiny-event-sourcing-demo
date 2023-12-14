package ru.quipy.projections.project_view

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.quipy.project.dto.ProjectModel
import java.util.*

@RestController
@RequestMapping("/projects-view")
class ProjectViewController(
        val projectViewService: ProjectViewService
) {
    @GetMapping("/{id}")
    fun getProject(@PathVariable id: UUID): ProjectModel? {
        return projectViewService.getOne(id)
    }

    @GetMapping("/{id}/tasks")
    fun getProjectTasks(@PathVariable id: UUID): MutableList<UUID> {
        return projectViewService.getOne(id).tasks
    }
}