package ru.quipy.project

import org.springframework.web.bind.annotation.*
import ru.quipy.core.EventSourcingService
import ru.quipy.domain.Event
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.project.eda.logic.addUser
import ru.quipy.project.eda.logic.create
import java.util.*

//@RestController
//@RequestMapping("/projects")
//class ProjectController(
//        val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>
//) {
//    @PostMapping("/{title}")
//    fun createProject(@PathVariable title: String, @RequestParam ownerId: UUID): ProjectCreatedEvent {
//        return projectEsService.create { it.create(UUID.randomUUID(), title, ownerId) }
//    }
//
//    @GetMapping("/{id}")
//    fun getProject(@PathVariable id: UUID): ProjectAggregateState? {
//        return projectEsService.getState(id)
//    }
//
//    @PostMapping("/{id}/{userId}")
//    fun addUserToProject(@PathVariable id: UUID, @PathVariable userId: UUID): List<Event<ProjectAggregate>> {
//        return projectEsService.updateSerial(id) {
//            it.addUser(userId)
//        }
//    }
//}