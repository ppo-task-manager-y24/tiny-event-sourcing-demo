package ru.quipy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.server.ResponseStatusException
import ru.quipy.domain.Event
import ru.quipy.task.eda.TaskService
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.eda.api.TaskAggregate
import ru.quipy.task.eda.api.TaskCreatedEvent
import java.sql.Timestamp
import java.util.*

@SpringBootTest
class TasksTests {
    companion object {
        private val id = UUID.randomUUID()
        private val name = "taskName"
        private val description = "task description"
        private val projectId = UUID.randomUUID()
        private val statusId = UUID.randomUUID()

        private val taskCreateModel = TaskCreate(
            id,
            name,
            description,
            projectId,
            statusId
        )

        private val newTaskName = "taskName 1"
        private val assigneeId = UUID.randomUUID()
    }

    @Autowired
    lateinit var taskEsService: TaskService

    @Test
    fun createNewTask() {
        var event: TaskCreatedEvent? = null

        Assertions.assertDoesNotThrow( {
            event = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(event!!.taskId, taskCreateModel.id, "task ids doesn't match") },
            Executable { Assertions.assertEquals(event!!.taskName, taskCreateModel.name,
                "task names doesn't match") },
            Executable { Assertions.assertEquals(event!!.taskDescription, taskCreateModel.description,
                "task descriptions doesn't match") },
            Executable { Assertions.assertEquals(event!!.projectId, taskCreateModel.projectId,
                "task project ids doesn't match") },
            Executable { Assertions.assertEquals(event!!.statusId, taskCreateModel.statusId,
                "task status ids doesn't match") }
        )
    }

    @Test
    fun renameTask() {
        var taskCreatedEvent: TaskCreatedEvent? = null

        Assertions.assertDoesNotThrow( {
            taskCreatedEvent = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        var taskRenameEvents: List<Event<TaskAggregate>>? = null

        Assertions.assertDoesNotThrow( {
            taskRenameEvents = taskEsService.rename(taskCreateModel.id, newTaskName)
        }, "can't rename task")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(taskRenameEvents!!.count(), 1, "wrong number of events") },
        )
    }

    @Test
    fun addUser() {
        var taskCreatedEvent: TaskCreatedEvent? = null

        Assertions.assertDoesNotThrow( {
            taskCreatedEvent = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        var addUserEvents: List<Event<TaskAggregate>>? = null

        Assertions.assertDoesNotThrow( {
            addUserEvents = taskEsService.addUser(taskCreateModel.id, assigneeId)
        }, "can't rename task")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(2, addUserEvents!!.count(), "wrong number of events") },
        )
    }

}
