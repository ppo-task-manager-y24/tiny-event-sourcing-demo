//package ru.quipy.task
//
//import org.springframework.web.bind.annotation.*
//import ru.quipy.core.EventSourcingService
//import ru.quipy.domain.Event
//import ru.quipy.logic.ProjectAggregateState
//import ru.quipy.task.eda.api.TaskAggregate
//import ru.quipy.task.eda.api.TaskCreatedEvent
//import ru.quipy.task.eda.api.TaskNameChangedEvent
//import ru.quipy.task.eda.logic.TaskAggregateState
//import ru.quipy.task.eda.logic.addExecutor
//import ru.quipy.task.eda.logic.create
//import ru.quipy.task.eda.logic.rename
//import java.util.*
//
//@RestController
//@RequestMapping("/tasks")
//class TaskController(
//        val taskEsService: EventSourcingService<UUID, TaskAggregate, TaskAggregateState>
//) {
//    @PostMapping("/create")
//    fun createTask(
//            @RequestParam projectId: UUID,
//            @RequestParam statusId: UUID,
//            @RequestParam description: String,
//            @RequestParam name: String
//    ): TaskCreatedEvent {
//        return taskEsService.create {
//            it.create(
//                    name,
//                    description,
//                    projectId,
//                    statusId
//            )
//        }
//    }
//
//    @PostMapping("/{id}")
//    fun renameTask(@PathVariable id: UUID, @RequestParam name: String): List<Event<TaskAggregate>> {
//        return taskEsService.updateSerial(id) {
//            it.rename(name)
//        }
//    }
//
//    @PostMapping("/{id}/{userId}")
//    fun assignUserToTask(@PathVariable id: UUID, @PathVariable userId: UUID): List<Event<TaskAggregate>> {
//        return taskEsService.updateSerial(id) {
//            it.addExecutor(userId)
//        }
//    }
//}