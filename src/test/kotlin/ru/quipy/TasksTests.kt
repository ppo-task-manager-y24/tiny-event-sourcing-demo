package ru.quipy

import org.junit.jupiter.api.AfterEach
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
import ru.quipy.project.dto.ProjectModel
import ru.quipy.task.eda.TaskService
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.eda.api.TaskAggregate
import ru.quipy.task.eda.api.TaskCreatedEvent
import ru.quipy.task.eda.logic.TaskAggregateState
import java.lang.NullPointerException
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

        private var state: TaskAggregateState? = null
    }

    @Autowired
    lateinit var taskEsService: TaskService

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun cleanDatabase() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("taskId").`is`(taskEsService.getOne(id)!!.getId())),
                ProjectModel::class.java)
        } catch (e: NullPointerException) {
            return
        }
    }

    @AfterEach
    fun cleanDatabaseAfter() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("taskId").`is`(taskEsService.getOne(state!!.getId())!!.getId())),
                ProjectModel::class.java)
        } catch (e: NullPointerException) {
            return
        }

        state = null
    }


    @Test
    fun createNewTask() {

        Assertions.assertDoesNotThrow( {
            state = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        Assertions.assertAll(
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

        Assertions.assertDoesNotThrow( {
            state = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        var taskRenameState: TaskAggregateState? = null

        var taskId = state?.getId()

        Assertions.assertNotNull(taskId)

        Assertions.assertDoesNotThrow( {
            taskRenameState = taskEsService.rename(taskId!!, newTaskName)
        }, "can't rename task")

        Assertions.assertAll(
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

        var state: TaskAggregateState? = null

        Assertions.assertDoesNotThrow( {
            state = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        var addUserState: TaskAggregateState? = null

        var taskId = state?.getId()

        Assertions.assertNotNull(taskId)

        Assertions.assertDoesNotThrow( {
            addUserState = taskEsService.addUser(taskId!!, assigneeId)
        }, "can't rename task")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(addUserState!!.name, taskCreateModel.name,
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
