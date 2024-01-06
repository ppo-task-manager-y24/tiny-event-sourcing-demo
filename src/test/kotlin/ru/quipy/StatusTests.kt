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
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.logic.create
import ru.quipy.status.dto.StatusViewModel
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.logic.StatusAggregateState
import ru.quipy.status.eda.logic.create
import ru.quipy.status.eda.logic.delete
import ru.quipy.status.eda.projections.StatusViewService
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.eda.TaskService
import java.util.*
import java.util.concurrent.TimeUnit

@SpringBootTest
class StatusTests {
    companion object {
        private val taskId = UUID.randomUUID()
        private val statusId = UUID.randomUUID()
        private val projectId = UUID.randomUUID()
        private const val STATUS_NAME = "myStatus"
        private const val COLOR = 255
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
    fun cleanDatabase() {
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
    fun creatStatus() {
        statusEsService.create {
            it.create(statusId, STATUS_NAME, COLOR)
        }

        val state = statusEsService.getState(statusId)

        Assertions.assertNotNull(state)

        Assertions.assertAll(
            Executable { Assertions.assertEquals(state!!.getId(), statusId,
                "statusId's doesn't match.") },
            Executable { Assertions.assertEquals(state!!.getStatusName(), STATUS_NAME,
                "status names doesn't match.") },
            Executable { Assertions.assertEquals(state!!.getColor(), COLOR,
                "colors doesn't match.") }
        )
    }

    @Test
    fun createStatus_readFromProjection_Succeeds() {
        val event = statusEsService.create {
            it.create(statusId, STATUS_NAME, COLOR)
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                var storedStatus: StatusViewModel? = null

                Assertions.assertDoesNotThrow( {
                    storedStatus = statusViewService.getStatus(event.statusId)
                }, "status doesn't exist.")

                Assertions.assertAll(
                    Executable { Assertions.assertEquals(storedStatus!!.statusId, event.statusId,
                        "statusId's doesn't match.") },
                    Executable { Assertions.assertEquals(event.statusName, STATUS_NAME,
                        "status names doesn't match.") },
                    Executable { Assertions.assertEquals(event.color, COLOR,
                        "colors doesn't match.") }
                )
            }
    }

    @Test
    fun deleteAssignedStatus_Fails() {
        createAndAssignStatusToTask()

        Awaitility
           .await()
           .pollDelay(1, TimeUnit.SECONDS)
           .untilAsserted {
               val isDeleted = statusEsService.getState(statusId)?.isDeleted()

               Assertions.assertNotNull(isDeleted)

               Assertions.assertFalse(isDeleted!!)
           }
    }

    @Test
    fun deleteNotAssignedStatus_Succeeds() {

        createAndAssignStatusToTask()

        statusEsService.update(statusId) {
            it.delete()
        }

        Awaitility
            .await()
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                val isDeleted = statusEsService.getState(statusId)?.isDeleted()

                Assertions.assertNotNull(isDeleted)

                Assertions.assertTrue(isDeleted!!)
            }

    }

    private fun createAndAssignStatusToTask() {
        statusEsService.create {
            it.create(statusId, STATUS_NAME, COLOR)
        }

        projectEsService.create {
            it.create(projectId, "projectTitle", UUID.randomUUID())
        }

        taskEsService.createOne(TaskCreate(
            UUID.randomUUID(),
            "taskName",
            "taskDescription",
            projectId,
            statusId
        ))
    }
}
