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
import ru.quipy.task.eda.logic.TaskAggregateState
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
        var state: TaskAggregateState? = null

        Assertions.assertDoesNotThrow( {
            state = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(state!!.getId(), taskCreateModel.id, "task ids doesn't match") },
            Executable { Assertions.assertEquals(state!!.name, taskCreateModel.name,
                "task names doesn't match") },
            Executable { Assertions.assertEquals(state!!.description, taskCreateModel.description,
                "task descriptions doesn't match") },
            Executable { Assertions.assertEquals(state!!.projectId, taskCreateModel.projectId,
                "task project ids doesn't match") },
            Executable { Assertions.assertEquals(state!!.statusId, taskCreateModel.statusId,
                "task status ids doesn't match") }
        )
    }

    @Test
    fun renameTask() {
        var taskCreatedState: TaskAggregateState? = null

        Assertions.assertDoesNotThrow( {
            taskCreatedState = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        var taskRenameState: TaskAggregateState? = null

        Assertions.assertDoesNotThrow( {
            taskRenameState = taskEsService.rename(taskCreateModel.id, newTaskName)
        }, "can't rename task")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(taskRenameState!!.getId(), taskCreateModel.id, "task ids doesn't match") },
            Executable { Assertions.assertEquals(taskRenameState!!.name, newTaskName,
                "task names doesn't match") },
            Executable { Assertions.assertEquals(taskRenameState!!.description, taskCreateModel.description,
                "task descriptions doesn't match") },
            Executable { Assertions.assertEquals(taskRenameState!!.projectId, taskCreateModel.projectId,
                "task project ids doesn't match") },
            Executable { Assertions.assertEquals(taskRenameState!!.statusId, taskCreateModel.statusId,
                "task status ids doesn't match") }
        )
    }

    @Test
    fun addUser() {

        var taskCreatedState: TaskAggregateState? = null

        Assertions.assertDoesNotThrow( {
            taskCreatedState = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        var addUserState: TaskAggregateState? = null

        Assertions.assertDoesNotThrow( {
            addUserState = taskEsService.addUser(taskCreateModel.id, assigneeId)
        }, "can't rename task")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(addUserState!!.getId(), taskCreateModel.id, "task ids doesn't match") },
            Executable { Assertions.assertEquals(addUserState!!.name, newTaskName,
                "task names doesn't match") },
            Executable { Assertions.assertEquals(addUserState!!.description, taskCreateModel.description,
                "task descriptions doesn't match") },
            Executable { Assertions.assertEquals(addUserState!!.projectId, taskCreateModel.projectId,
                "task project ids doesn't match") },
            Executable { Assertions.assertEquals(addUserState!!.executors.count(), 1,
                "task executors doesn't match") },
            Executable { Assertions.assertEquals(addUserState!!.executors[0], assigneeId,
                "task executors id doesn't match") },
            Executable { Assertions.assertEquals(addUserState!!.statusId, taskCreateModel.statusId,
                "task status ids doesn't match") }
        )
    }

}
