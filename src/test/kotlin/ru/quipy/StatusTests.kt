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
import org.springframework.web.server.ResponseStatusException
import ru.quipy.core.EventSourcingService
import ru.quipy.status.dto.StatusViewModel
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.logic.StatusAggregateState
import ru.quipy.status.eda.logic.create
import ru.quipy.status.eda.projections.StatusViewEntity
import ru.quipy.status.eda.projections.StatusViewService
import java.util.*

@SpringBootTest
class StatusTests {
    companion object {
        private const val STATUS_NAME = "myStatus"
        private const val COLOR = 255
    }

    @Autowired
    lateinit var statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>

    @Autowired
    private lateinit var statusViewService: StatusViewService

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun cleanDatabase() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("statusName").`is`(STATUS_NAME)),
                    StatusViewEntity::class.java)
        } catch (e: ResponseStatusException) {
            if (e.status != HttpStatus.NOT_FOUND)
            {
                throw e
            }
        }
    }

    @Test
    fun createStatus() {
        val event = statusEsService.create {
            it.create(STATUS_NAME, COLOR)
        }

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