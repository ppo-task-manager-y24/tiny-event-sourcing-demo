package ru.quipy

import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.findAllAndRemove
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import ru.quipy.core.EventSourcingService
import ru.quipy.project.ProjectService
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.TaskCreate
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.projections.project_view.ProjectStatusViewEntity
import ru.quipy.projections.project_view.ProjectUserViewEntity
import ru.quipy.projections.project_view.ProjectViewEntity
import ru.quipy.projections.project_view.ProjectViewService
import ru.quipy.projections.task_view.TaskViewEntity
import ru.quipy.projections.user_project_view.UserProjectViewEntity
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.logic.*
import ru.quipy.status.eda.projections.status_view.StatusService
import ru.quipy.status.eda.projections.status_view.StatusViewEntity
import ru.quipy.user.UserEntity
import ru.quipy.user.UserService
import ru.quipy.user.dto.UserModel
import ru.quipy.user.dto.UserRegister
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.logic.UserAggregateState
import ru.quipy.user.eda.logic.create
import java.util.*
import java.util.concurrent.TimeUnit

@SpringBootTest
class ProjectViewTest {
    companion object {
        private val projectId: UUID = UUID.randomUUID()
        private val projectTitle = "title"
        private val projectOwnerId: UUID = UUID.randomUUID()

        val userName = "username"
        private val projectOwnerName = userName
        private val users: MutableList<UUID> = mutableListOf()

        val statusName = "statusname"
        private val statuses: MutableList<UUID> = mutableListOf()
        private val statusNames: MutableList<String> = mutableListOf()

        val taskName = "taskname"
        private val tasks: MutableList<UUID> = mutableListOf()
        private val taskNames: MutableList<String> = mutableListOf()
    }

    @Autowired
    private lateinit var userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var projectService: ProjectService

    @Autowired
    lateinit var statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>

    @Autowired
    private lateinit var projectViewService: ProjectViewService

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun init() {
        cleanDatabase()
        users.clear()
        statuses.clear()
        tasks.clear()
        initProject()
        initUsers()
        initStatuses()
        initTasks()
        Thread.sleep(5000)
    }

    @AfterEach
    fun dest() {
        cleanDatabase()
        users.clear()
        statuses.clear()
        statusNames.clear()
        tasks.clear()
        taskNames.clear()
    }

    private fun initProject() {
        projectService.createProject(
            ProjectCreate(
                id = projectId,
                title = projectTitle,
                ownerId = projectOwnerId
            )
        )
    }

    private fun initUsers() {
        val userRealName = "Dimas"
        val userPassword = "12345678"

        userEsService.create { state ->
            state.create(
                projectOwnerId,
                projectOwnerName,
                userRealName,
                userPassword
            )
        }
        users.add(projectOwnerId)

        val id = UUID.randomUUID()
        userEsService.create { state ->
            state.create(
                id,
                userName + "_",
                userRealName,
                userPassword
            )
        }
        users.add(id)

        projectService.addUserToProject(projectId, users[0])
    }

    private fun initStatuses() {
        statusEsService.create {
            it.create(projectId)
        }

        for (i in 0..1) {
            val id = UUID.randomUUID()
            statusEsService.update(projectId) {
                it.addStatus(id, statusName + i.toString(), i)
            }
            statusNames.add(statusName + i.toString())
            statuses.add(id)
        }
    }

    private fun initTasks() {
        for (i in 0..1) {
            val id = UUID.randomUUID()
            val taskCreate = TaskCreate(
                id = id,
                name = taskName + i.toString(),
                description = "",
                projectId = projectId,
                statusId = statuses[0]
            )
            projectService.createTask(taskCreate)
            taskNames.add(taskName + i.toString())
            tasks.add(id)
        }
        projectService.addUserToTask(projectId, tasks[0], users[0])
    }

    private fun cleanDatabase() {
        mongoTemplate.dropCollection(ProjectViewEntity::class.java)
        mongoTemplate.dropCollection(ProjectUserViewEntity::class.java)
        mongoTemplate.dropCollection(ProjectStatusViewEntity::class.java)
        mongoTemplate.dropCollection(TaskViewEntity::class.java)
        mongoTemplate.dropCollection(UserProjectViewEntity::class.java)
        mongoTemplate.dropCollection(UserEntity::class.java)
        mongoTemplate.dropCollection(StatusViewEntity::class.java)

        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(ProjectViewTest.projectId)), "aggregate-project")
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(ProjectViewTest.projectId)), "snapshots")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(projectId)), "aggregate-status")
        users.forEach {
            mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(it)), "user-aggregate")
            mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(it)), "snapshots")
        }
        statuses.forEach {
            mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(it)), "aggregate-status")
            mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(it)), "snapshots")
        }
        tasks.forEach {
            mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(it)), "aggregate-project")
            mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(it)), "snapshots")
        }
    }

    @Test
    fun getParticipantIds() {
        projectService.addUserToProject(projectId, users[1])

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val participantIds = projectViewService.getParticipantIds(projectId)

                Assertions.assertEquals(participantIds.size, users.size)
                Assertions.assertEquals(participantIds.last(), users.last())
            }
    }

    @Test
    fun getParticipants() {
        projectService.addUserToProject(projectId, users[1])

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val participants = projectViewService.getParticipants(projectId)

                Assertions.assertEquals(participants.size, users.size)
                Assertions.assertTrue(participants.last().contains(userName))
            }
    }

    @Test
    fun getStatusIds() {
        val id = UUID.randomUUID()
        val color = statuses.size
        statusEsService.update(projectId) {
            it.addStatus(id, statusName + "test", color)
        }
        statuses.add(id)

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val statuses_ = projectViewService.getStatusIds(projectId)

                Assertions.assertEquals(statuses.size, statuses_.size)
                Assertions.assertEquals(statuses.last(), statuses_.last())
            }

        statusEsService.update(projectId) {
            it.delete(id)
        }
        statuses.removeLast()
        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val statuses_ = projectViewService.getStatusIds(projectId)

                Assertions.assertEquals(statuses.size, statuses_.size)
                Assertions.assertEquals(statuses.last(), statuses_.last())
            }
    }

    @Test
    fun getStatuses() {
        val id = UUID.randomUUID()
        val color = statuses.size
        statusEsService.update(projectId) {
            it.addStatus(id, statusName + "test", color)
        }
        statuses.add(id)

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val statuses_ = projectViewService.getStatuses(projectId)

                Assertions.assertEquals(statuses.size, statuses_.size)
                Assertions.assertTrue(statuses_.last().contains(statusName))
            }
    }

    @Test
    fun getTaskIds() {
        val id = UUID.randomUUID()
        val name = taskName + "test_"
        val taskCreate = TaskCreate(
            id = id,
            name = taskName + "test",
            description = "",
            projectId = projectId,
            statusId = statuses[0]
        )
        projectService.createTask(taskCreate)
        projectService.renameTask(projectId, id, name)
        tasks.add(id)
        projectService.addUserToTask(projectId, tasks.last(), users[0])

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val tasks_ = projectViewService.getTaskIds(projectId)

                Assertions.assertEquals(tasks.size, tasks_.size)
                Assertions.assertEquals(tasks.last(), tasks_.last())
            }
    }

    @Test
    fun getTasks() {
        val id = UUID.randomUUID()
        val name = taskName + "test_"
        val taskCreate = TaskCreate(
            id = id,
            name = taskName + "test",
            description = "",
            projectId = projectId,
            statusId = statuses[0]
        )
        projectService.createTask(taskCreate)
        projectService.renameTask(projectId, id, name)
        tasks.add(id)
        projectService.addUserToTask(projectId, tasks.last(), users[0])

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val tasks_ = projectViewService.getTasks(projectId)

                Assertions.assertEquals(tasks.size, tasks_.size)
                Assertions.assertEquals(projectId, tasks_.last().projectId)
                Assertions.assertEquals(name, tasks_.last().name)
                Assertions.assertEquals(statuses[0], tasks_.last().statusId)
                Assertions.assertEquals(1, tasks_.last().executors.size)
                Assertions.assertEquals(users[0], tasks_.last().executors.last())
            }
    }

    @Test
    fun getProject() {
        projectService.createProject(
            ProjectCreate(
                id = UUID.randomUUID(),
                title = projectTitle,
                ownerId = projectOwnerId
            )
        )

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val project = projectViewService.getProject(projectId)

                Assertions.assertEquals(projectId, project.id)
                Assertions.assertEquals(listOf(projectOwnerName), project.participants)
                Assertions.assertEquals(projectTitle, project.name)
                Assertions.assertDoesNotThrow {
                    Assertions.assertEquals(userService.getOneByUsername(project.owner).userId, projectOwnerId)
                }
                Assertions.assertEquals(taskNames, project.tasks.toMutableList())
                Assertions.assertEquals(statusNames, project.statuses.toMutableList())
            }
    }

    @Test
    fun UserAggrProjectAggrSync() {
        val userId1 = UUID.randomUUID()
        val username1 = "UserAggrProjectAggrSync1"
        userEsService.create {
            it.create(
                userId1,
                username1,
                "",
                ""
            )
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)

        projectService.addUserToProject(projectId, userId1)

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val projects = projectViewService.getParticipants(projectId)

                Assertions.assertTrue(projects.contains(username1))
            }

        val userId2 = UUID.randomUUID()
        val username2 = "UserAggrProjectAggrSync2"
        projectService.addUserToProject(projectId, userId2)

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)

        userEsService.create {
            it.create(
                userId2,
                username2,
                "",
                ""
            )
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val projects = projectViewService.getParticipants(projectId)

                Assertions.assertTrue(projects.contains(username2))
            }
    }

    @Test
    fun StatusAggrProjectAggrSync() {
        val statusId1 = UUID.randomUUID()
        val statusName1 = "StatusAggrProjectAggrSync1"
        statusEsService.update(projectId) {
            it.addStatus(statusId1, statusName1, 0)
        }
        val taskId1 = UUID.randomUUID()
        val taskName1 = "StatusAggrProjectAggrSync1_task"
        projectService.createTask(TaskCreate(taskId1, taskName1, "", projectId, statuses[0]))

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)

        statusEsService.update(projectId) {
            it.changeStatusForTask(taskId1, statusId1)
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val statuses_ = projectViewService.getStatuses(projectId)
                Assertions.assertTrue(statuses_.contains(statusName1))

                val tasks_ = projectViewService.getTasks(projectId)
                tasks_.any { task -> task.name == taskName1 && task.statusId == statusId1 }
            }

        val statusId2 = UUID.randomUUID()
        val statusName2 = "StatusAggrProjectAggrSync2"
        statusEsService.update(projectId) {
            it.addStatus(statusId2, statusName2, 0)
        }
        val taskId2 = UUID.randomUUID()
        val taskName2 = "StatusAggrProjectAggrSync2_task"
        statusEsService.update(projectId) {
            it.changeStatusForTask(taskId2, statusId2)
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)

        projectService.createTask(TaskCreate(taskId2, taskName2, "", projectId, statuses[0]))

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val statuses_ = projectViewService.getStatuses(projectId)
                Assertions.assertTrue(statuses_.contains(statusName2))

                val tasks_ = projectViewService.getTasks(projectId)
                tasks_.any { task -> task.name == taskName2 && task.statusId == statusId2 }
            }
    }
}
