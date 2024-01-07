package ru.quipy

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import ru.quipy.core.EventSourcingService
import ru.quipy.project.eda.logic.ProjectAggregateState
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.logic.create
import ru.quipy.status.eda.api.*
import ru.quipy.status.eda.logic.*
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.task.dto.TaskCreate
import ru.quipy.task.eda.TaskService
import java.lang.Long.min
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

@SpringBootTest(properties = ["event.sourcing.stream-batch-size=3"])
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StatusStressTestsFails {
    companion object {
        private val taskId = UUID.fromString("1decfc93-d2f9-4270-9308-b8f6ac9be8af")
        private val projectId = UUID.fromString("1decfc93-d2f9-4270-9308-b8f6ac9be8af")

        private val statusId = taskId

        private val statuses: Set<UUID> = buildSet {
            for (i in 0 until STATUSES_COUNT) {
                add(UUID.randomUUID())
            }
        }

        private const val STATUSES_COUNT = 49
    }

    @Autowired
    lateinit var statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>

    @Autowired
    private lateinit var projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>

    @Autowired
    private lateinit var taskEsService: TaskService

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var subscriptionManager: AggregateSubscriptionsManager

    @BeforeEach
    fun init() {
        cleanDatabase()
    }

    private val statusChangedCounter = AtomicInteger(0)
    private val statusAddedCounter = AtomicInteger(0)

    @Test
    fun eventOrderTest() {
        Assertions.assertThrows(ClassCastException::class.java) {
            eventOrderTestFailing()
        }
    }

    private fun eventOrderTestFailing() {
        createAndAssignStatusToTask()

        statusEsService.create {
            it.create(projectId)
        }

        val allStatuses = statuses.toList()

        subscriptionManager.createSubscriber(StatusAggregate::class, "StressTestChecker") {
            `when`(StatusChangedInTaskEvent::class) {
                if (it.projectId == projectId) {
                    statusChangedCounter.getAndIncrement()
                }
            }

            `when`(StatusAddedEvent::class) {
                if (it.projectId == projectId) {
                    statusAddedCounter.getAndIncrement()
                }
            }
        }

        val time1 = measureTimeMillis {
            runBlocking {
                for (i in 0 until STATUSES_COUNT) {
                    launch {
                        statusEsService.update(projectId) {
                            it.addStatus(allStatuses[i], "name${i}", i)
                        }
                    }
                }
            }
        }

        val time2 = measureTimeMillis {
            runBlocking {
                for (i in 0 until STATUSES_COUNT) {
                    launch {
                        statusEsService.update(projectId) {
                            it.changeStatusForTask(taskId, allStatuses[i])
                        }
                    }
                }
            }
        }

        println("rps: ${STATUSES_COUNT / (min(time1, time2).toDouble() / 1000.0) }")

        Awaitility
            .await()
            .atMost(5, TimeUnit.SECONDS)
            .pollDelay(1, TimeUnit.SECONDS)
            .untilAsserted {
                Assertions.assertEquals(STATUSES_COUNT, statusAddedCounter.get())
                Assertions.assertEquals(STATUSES_COUNT, statusChangedCounter.get())

                val task = taskEsService.getOne(projectId, taskId)
                Assertions.assertNotNull(task)
                Assertions.assertNotNull(task)

                Assertions.assertTrue(statuses.contains(task!!.statusId))
            }
    }

    private fun cleanDatabase() {
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(statusId)), "aggregate-project")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(projectId)), "aggregate-project")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(statusId)), "aggregate-status")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(projectId)), "aggregate-status")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(taskId)), "aggregate-status")
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(taskId)), "aggregate-project")
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(taskId)), "snapshots")

        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(statusId)), "snapshots")
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(projectId)), "snapshots")
    }

    private fun createAndAssignStatusToTask() {

        projectEsService.create {
            it.create(projectId, "projectTitle", UUID.randomUUID())
        }

        taskEsService.createOne(
            TaskCreate(
                taskId,
                "taskName",
                "taskDescription",
                projectId,
                statusId
            )
        )
    }
}