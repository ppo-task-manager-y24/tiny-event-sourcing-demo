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
import ru.quipy.project.ProjectService
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import ru.quipy.project.dto.TaskCreate
import ru.quipy.project.eda.logic.TaskEntity
import java.lang.NullPointerException
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
    lateinit var taskEsService: ProjectService

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    lateinit var projectEsService: ProjectService

    @BeforeEach
    fun cleanDatabase() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("projectId").`is`(projectId)),
                ProjectModel::class.java)
            mongoTemplate.remove(Query.query(Criteria.where("taskId").`is`(taskEsService.getTask(projectCreateModel.id, id)!!.id)),
                ProjectModel::class.java)
        } catch (e: NullPointerException) {

        }
        projectId = UUID.randomUUID()
        projectCreateModel.id = projectId
        taskCreateModel.projectId = projectId
        projectEsService.createProject(projectCreateModel)
    }

    @AfterEach
    fun cleanDatabaseAfter() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("projectId").`is`(projectId)),
                ProjectModel::class.java)
            mongoTemplate.remove(Query.query(Criteria.where("taskId").`is`(taskEsService.getTask(projectCreateModel.id, id)!!.id)),
                ProjectModel::class.java)
        } catch (e: NullPointerException) {
            return
        }

        state = null
    }

    @Test
    fun createNewTask_Succeeds() {

        Assertions.assertDoesNotThrow( {
            taskEsService.createTask(taskCreateModel)
            state = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
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
    fun createExistingTask_Fails() {

        Assertions.assertDoesNotThrow( {
            taskEsService.createTask(taskCreateModel)
            state = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
        }, "can't create new task")

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            {
                taskEsService.createTask(
                    TaskCreate(UUID.randomUUID(), name, description, projectId, statusId)
                )
            }, "task created but task with such name already exist in the project."
        )
    }

    @Test
    fun renameTask_Succeeds() {
        Assertions.assertDoesNotThrow( {
            taskEsService.createTask(taskCreateModel)
            state = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
        }, "can't create new task")

        var taskRenameState: TaskEntity? = null

        var taskId = state?.id

        Assertions.assertNotNull(taskId)

        Assertions.assertDoesNotThrow( {
            taskEsService.renameTask(projectId, taskId!!, newTaskName)
            taskRenameState = taskEsService.getTask(projectId, taskId)
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
    fun renameNotExistingTask_Fails() {
        Assertions.assertDoesNotThrow( {
            taskEsService.createTask(taskCreateModel)
            state = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
        }, "can't create new task")

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            {
                taskEsService.renameTask(projectId, UUID.randomUUID(), newTaskName)
            }, "rename task but it doesn't exist in the project."
        )
    }

    @Test
    fun addUser_Succeeds() {

        var state: TaskEntity? = null

        Assertions.assertDoesNotThrow( {
            taskEsService.createTask(taskCreateModel)
            state = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
        }, "can't create new task")

        var addUserState: TaskEntity? = null

        var taskId = state?.id

        Assertions.assertNotNull(taskId)

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            {
                taskEsService.addUserToTask(taskCreateModel.projectId, taskCreateModel.id, assigneeId)
                addUserState = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
            },
            "user added")

        projectEsService.addUserToProject(taskCreateModel.projectId, assigneeId)

        Assertions.assertDoesNotThrow( {
            taskEsService.addUserToTask(projectId, taskId!!, assigneeId)
            addUserState = taskEsService.getTask(projectId, taskId)
        }, "can't add task's executor")

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

    @Test
    fun addNonExistingUserToTask_Fails() {
        var state: TaskEntity? = null

        Assertions.assertDoesNotThrow( {
            taskEsService.createTask(taskCreateModel)
            state = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
        }, "can't create new task")

        var addUserState: TaskEntity? = null

        val taskId = state?.id

        Assertions.assertNotNull(taskId)

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            {
                taskEsService.addUserToTask(taskCreateModel.projectId, taskCreateModel.id, assigneeId)
                addUserState = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
            },
            "user added")

        projectEsService.addUserToProject(taskCreateModel.projectId, assigneeId)

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
        {
            taskEsService.addUserToTask(projectId, taskId!!, UUID.randomUUID())
            addUserState = taskEsService.getTask(projectId, taskId)
        }, "added non existing user as task's executor")
    }

    @Test
    fun addExecutorToNonExistingTask_Fails() {
        var state: TaskEntity? = null

        Assertions.assertDoesNotThrow( {
            taskEsService.createTask(taskCreateModel)
            state = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
        }, "can't create new task")

        var addUserState: TaskEntity? = null

        val taskId = state?.id

        Assertions.assertNotNull(taskId)

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            {
                taskEsService.addUserToTask(taskCreateModel.projectId, taskCreateModel.id, assigneeId)
                addUserState = taskEsService.getTask(taskCreateModel.projectId, taskCreateModel.id)
            },
            "user added")

        projectEsService.addUserToProject(taskCreateModel.projectId, assigneeId)

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            {
                val id = UUID.randomUUID()

                taskEsService.addUserToTask(projectId, id, assigneeId)
                addUserState = taskEsService.getTask(projectId, id)
            }, "added non existing user as task's executor")
    }

}
