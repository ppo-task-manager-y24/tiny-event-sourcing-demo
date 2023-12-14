package ru.quipy.projections.task_view

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.quipy.task.dto.TaskModel
import java.util.*

@RestController
@RequestMapping("/tasks-view")
class TaskViewController(
        val taskViewService: TaskViewService
) {
    @GetMapping("/{id}")
    fun getTask(@PathVariable id: UUID): TaskModel? {
        return taskViewService.getOne(id)
    }

    @GetMapping("/{id}/executors")
    fun getTaskExecutors(@PathVariable id: UUID): MutableList<UUID> {
        return taskViewService.getOne(id).executors
    }
}
