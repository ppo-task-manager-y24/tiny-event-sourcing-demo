package ru.quipy

import org.awaitility.kotlin.await
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
import ru.quipy.project.eda.logic.create
import ru.quipy.status.eda.api.*
import ru.quipy.status.eda.logic.*
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import java.util.concurrent.TimeUnit

@SpringBootTest(properties = ["event.sourcing.stream-batch-size=3"])
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StatusOrderTests {
    companion object {
        private val taskId = UUID.fromString("1decfc93-d2f9-4270-9308-b8f6ac9be8af")
        private val projectId = taskId

        private val statusId = taskId
        private const val STATUS_NAME = "firstStatus"
        private const val COLOR = 255
    }

    @Autowired
    lateinit var statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var subscriptionManager: AggregateSubscriptionsManager

    @BeforeEach
    fun init() {
        cleanDatabase()
    }

    private val stringBuilder = StringBuilder()

    @Test
    fun eventOrderTest() {

        statusEsService.create {
            it.create(projectId)
        }

        subscriptionManager.createSubscriber(StatusAggregate::class, "EventOrderChecker") {
            `when`(StatusAddedEvent::class) {
                stringBuilder.append(0)
            }

            `when`(StatusDeletedEvent::class) {
                stringBuilder.append(1)
            }
        }

        for (i in 0..10) {
            if (i % 2 == 0) {
                Assertions.assertDoesNotThrow {
                    statusEsService.update(projectId) {
                        it.addStatus(statusId, STATUS_NAME, COLOR)
                    }
                }
            } else {
                Assertions.assertDoesNotThrow {
                    statusEsService.update(projectId) {
                        it.delete(statusId)
                    }
                }
            }
        }

        val expectedStr = StringBuilder()

        for (i in 0..10) {
            if (i % 2 == 0) {
                expectedStr.append(0)
            } else {
                expectedStr.append(1)
            }
        }

        await.atMost(10, TimeUnit.SECONDS).until {
            expectedStr.toString() == stringBuilder.toString()
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
}