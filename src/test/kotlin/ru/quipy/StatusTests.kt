package ru.quipy

import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import ru.quipy.core.EventSourcingService
import ru.quipy.project.eda.logic.ProjectAggregateState
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.logic.create
import ru.quipy.status.dto.StatusViewModel
import ru.quipy.status.eda.api.*
import ru.quipy.status.eda.logic.*
import ru.quipy.status.eda.projections.status_view.StatusViewService
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.eda.TaskService
import java.util.*
import java.util.concurrent.TimeUnit

@SpringBootTest
class StatusTests {
    companion object {
        private val taskId = UUID.randomUUID()
        private val projectId = UUID.randomUUID()

        private val statusId = UUID.randomUUID()
        private const val STATUS_NAME = "firstStatus"
        private const val COLOR = 255

        private val secondStatusId = UUID.randomUUID()
        private const val SECOND_STATUS_NAME = "secondStatus"
        private const val SECOND_COLOR = 128
    }

    @Autowired
    lateinit var statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>

    @Autowired
    private lateinit var statusViewService: StatusViewService

    @Autowired
    private lateinit var taskEsService: TaskService

    @Autowired
    private lateinit var projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun init() {
        cleanDatabase()
        initStatusAggregate()
    }

    private fun initStatusAggregate() {
        statusEsService.create {
            it.create(projectId)
        }
    }

    private fun cleanDatabase() {
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(statusId)), "aggregate-project")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(projectId)), "aggregate-project")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(statusId)), "aggregate-status")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(projectId)), "aggregate-status")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(taskId)), "aggregate-project")
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(taskId)), "snapshots")

        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(statusId)), "snapshots")
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(projectId)), "snapshots")
    }

    @Test
    fun defaultStatusInProject() {
        val state = statusEsService.getState(projectId)!!

        Assertions.assertNotNull(state)

        val statuses = state.statuses.toList()
        Assertions.assertEquals(statuses.count(), 1)

        val status = statuses.first().second
        Assertions.assertNotNull(status)


        Assertions.assertEquals(status.statusName, StatusAggregateState.defaultStatusName)
        Assertions.assertEquals(status.id, StatusAggregateState.defaultStatusId)
    }

    @Test
    fun addStatus() {
        statusEsService.update(projectId) {
            it.addStatus(statusId, STATUS_NAME, COLOR)
        }

        val state = statusEsService.getState(projectId)

        Assertions.assertNotNull(state)

        val statuses = state!!.statuses.toList()

        Assertions.assertEquals(statuses.count(), 2)

        val status = statuses.first {
            it.first == statusId
        }.second

        Assertions.assertAll(
            Executable { Assertions.assertEquals(state.getId(), projectId,
                "projectId's doesn't match.") },
            Executable { Assertions.assertEquals(
                status.projectId, projectId,
                "statusId's doesn't match.") },
            Executable { Assertions.assertEquals(
                status.statusName, STATUS_NAME,
                "status names doesn't match.") },
            Executable { Assertions.assertEquals(
                status.color, COLOR,
                "colors doesn't match.") }
        )
    }

    @Test
    fun createStatus_readFromProjection_Succeeds() {

        statusEsService.update(projectId) {
            it.addStatus(statusId, STATUS_NAME, COLOR)
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                var storedStatus: StatusViewModel? = null

                Assertions.assertDoesNotThrow( {
                    storedStatus = statusViewService.getStatus(projectId, statusId)
                }, "status doesn't exist.")

                Assertions.assertAll(
                    Executable { Assertions.assertEquals(storedStatus!!.statusId, statusId,
                        "statusId's doesn't match.") },
                    Executable { Assertions.assertEquals(storedStatus!!.statusName, STATUS_NAME,
                        "status names doesn't match.") },
                    Executable { Assertions.assertEquals(storedStatus!!.color, COLOR,
                        "colors doesn't match.") }
                )
            }
    }

    @Test
    fun deleteStatus_readFromProjection_Succeeds() {
        statusEsService.update(projectId) {
            it.addStatus(statusId, STATUS_NAME, COLOR)
        }

        statusEsService.update(projectId) {
            it.delete(statusId)
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                var storedStatuses: List<StatusViewModel>? = null

                Assertions.assertDoesNotThrow( {
                    storedStatuses = statusViewService.getStatuses(projectId)
                }, "status doesn't exist.")

                Assertions.assertNotNull(storedStatuses)

                val deletedStatus = storedStatuses!!.firstOrNull {
                    it.statusId == statusId
                }

                Assertions.assertNull(deletedStatus)
            }
    }

    @Test
    fun deleteAssignedStatus_Fails() {
        createAndAssignStatusToTask()

        Awaitility
           .await()
           .pollDelay(1, TimeUnit.SECONDS)
           .untilAsserted {

               Assertions.assertThrows(IllegalStateException::class.java) {
                   statusEsService.update(projectId) {
                       it.delete(statusId)
                   }
               }
           }
    }

    @Test
    fun deleteNotAssignedStatus_Succeeds() {

        createStatus()

        statusEsService.update(projectId) {
            it.delete(statusId)
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val isDeleted = statusEsService.getState(projectId)?.statuses?.get(statusId)?.isDeleted()

                Assertions.assertNotNull(isDeleted)

                Assertions.assertTrue(isDeleted!!)
            }

    }

    @Test
    fun changeToNotExistingStatus_Fails() {
        createAndAssignStatusToTask()

        Assertions.assertThrows(IllegalStateException::class.java) {
            statusEsService.update(projectId) {
                it.changeStatusForTask(taskId, UUID.randomUUID())
            }
        }
    }

    @Test
    fun changeToExistingStatus_FirstTime_Succeeds() {
        createAnotherStatus()

        var event: StatusChangedInTaskEvent? = null

        Assertions.assertDoesNotThrow {
            event =  statusEsService.update(projectId) {
                it.changeStatusForTask(taskId, secondStatusId)
            }
        }

        Assertions.assertNotNull(event)
        Assertions.assertEquals(event!!.statusId, secondStatusId)
    }

    @Test
    fun changeToExistingStatus_Succeeds() {
        createAndAssignStatusToTask()

        statusEsService.update(projectId) {
            it.changeStatusForTask(taskId, statusId)
        }

        createAnotherStatus()

        var event: StatusChangedInTaskEvent? = null

        Assertions.assertDoesNotThrow {
            event =  statusEsService.update(projectId) {
                it.changeStatusForTask(taskId, secondStatusId)
            }
        }

        Assertions.assertNotNull(event)
        Assertions.assertEquals(event!!.statusId, secondStatusId)
    }

    @Test
    fun changeStatus_ProjectAggregateIsUpdated() {
        createAndAssignStatusToTask()
        createAnotherStatus()

        statusEsService.update(projectId) {
            it.changeStatusForTask(taskId, secondStatusId)
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val task = taskEsService.getOne(projectId, taskId)
                Assertions.assertNotNull(task)
                Assertions.assertNotNull(task)

                Assertions.assertEquals(task!!.statusId, secondStatusId)
            }

    }

    private fun createAndAssignStatusToTask() {
        statusEsService.update(projectId) {
            it.addStatus(statusId, STATUS_NAME, COLOR)
        }

        projectEsService.create {
            it.create(projectId, "projectTitle", UUID.randomUUID())
        }

        taskEsService.createOne(TaskCreate(
            taskId,
            "taskName",
            "taskDescription",
            projectId,
            statusId
        ))
    }

    private fun createAnotherStatus() {
        statusEsService.update(projectId) {
            it.addStatus(secondStatusId, SECOND_STATUS_NAME, SECOND_COLOR)
        }
    }

    private fun createStatus() {
        statusEsService.update(projectId) {
            it.addStatus(statusId, STATUS_NAME, COLOR)
        }

        projectEsService.create {
            it.create(projectId, "projectTitle", UUID.randomUUID())
        }
    }
}
