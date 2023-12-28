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
import ru.quipy.logic.TaskEntity
import ru.quipy.project.ProjectService
import ru.quipy.project.dto.ProjectCreate
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
        private var projectId = UUID.randomUUID()
        private val statusId = UUID.randomUUID()
        private val ownerId = UUID.randomUUID()

        private var taskCreateModel = TaskCreate(
            id,
            name,
            description,
            projectId,
            statusId
        )
        private val projectTitle = "Project title"
        private var projectCreateModel = ProjectCreate(
            projectId,
            projectTitle,
            ownerId
        )

        private val newTaskName = "taskName 1"
        private val assigneeId = UUID.randomUUID()

        private var state: TaskEntity? = null
    }

    @Autowired
    lateinit var taskEsService: TaskService

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    lateinit var projectEsService: ProjectService

    @BeforeEach
    fun cleanDatabase() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("projectId").`is`(projectId)),
                ProjectModel::class.java)
            mongoTemplate.remove(Query.query(Criteria.where("taskId").`is`(taskEsService.getOne(projectCreateModel.id, id)!!.id)),
                ProjectModel::class.java)
        } catch (e: NullPointerException) {

        }
        projectId = UUID.randomUUID()
        projectCreateModel.id = projectId
        taskCreateModel.projectId = projectId
        projectEsService.createOne(projectCreateModel)
    }

    @AfterEach
    fun cleanDatabaseAfter() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("projectId").`is`(projectId)),
                ProjectModel::class.java)
            mongoTemplate.remove(Query.query(Criteria.where("taskId").`is`(taskEsService.getOne(projectCreateModel.id, id)!!.id)),
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

        var taskRenameState: TaskEntity? = null

        var taskId = state?.id

        Assertions.assertNotNull(taskId)

        Assertions.assertDoesNotThrow( {
            taskRenameState = taskEsService.rename(projectId, taskId!!, newTaskName)
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

        var state: TaskEntity? = null

        Assertions.assertDoesNotThrow( {
            state = taskEsService.createOne(taskCreateModel)
        }, "can't create new task")

        var addUserState: TaskEntity? = null

        var taskId = state?.id

        Assertions.assertNotNull(taskId)

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            {
                addUserState = taskEsService.addUser(taskCreateModel.projectId, taskCreateModel.id, assigneeId)
            },
            "user added")

        projectEsService.addUser(taskCreateModel.projectId, assigneeId)

        Assertions.assertDoesNotThrow( {
            addUserState = taskEsService.addUser(projectId, taskId!!, assigneeId)
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
