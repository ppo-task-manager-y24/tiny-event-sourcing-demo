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
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.ProjectService
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.user.UserEntity
import java.lang.NullPointerException
import java.sql.Timestamp
import java.util.*

@SpringBootTest
class ProjectsTests {
    companion object {
        private val id = UUID.randomUUID()
        private val participants = MutableList(2, {
            UUID.randomUUID()!!
        })

        private val tasks = MutableList(2, {
            UUID.randomUUID()!!
        })

        private val name = "projectName"
        private val ownerId = UUID.randomUUID()
        private val createdAt = System.currentTimeMillis()
        private val updatedAt = System.currentTimeMillis() + 100

        private val projectModel = ProjectModel(
            id,
            participants,
            name,
            ownerId,
            createdAt,
            updatedAt,
            tasks
        )

        private val title = "Project title"

        private val projectCreateModel = ProjectCreate(
            id,
            title,
            ownerId
        )

        private var state: ProjectAggregateState? = null
    }

    @Autowired
    lateinit var projectEsService: ProjectService

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun cleanDatabase() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("projectId").`is`(projectEsService.getOne(projectCreateModel.id)!!.getId())),
                ProjectModel::class.java)
        } catch (e: NullPointerException) {
            return
        }
    }

    @AfterEach
    fun cleanDatabaseAfter() {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("projectId").`is`(projectEsService.getOne(state!!.getId())!!.getId())),
                ProjectModel::class.java)
        } catch (e: NullPointerException) {
            return
        }

        state = null
    }

    @Test
    fun createNewProject() {

        val projectCreateModel = ProjectCreate(
            UUID.randomUUID(),
            title,
            ownerId
        )

        Assertions.assertDoesNotThrow( {
            state = projectEsService.createOne(projectCreateModel)
        }, "can't create new project")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(state!!.participants, MutableList<UUID>(0, {
                UUID.randomUUID()
            }), "project participants doesn't match") },
            Executable { Assertions.assertEquals(state!!.name, title,
                "project names doesn't match") },
            Executable { Assertions.assertEquals(state!!.owner, ownerId,
            "project owners doesn't match") }
        )
    }

    @Test
    fun addUserToProject() {

        val projectCreateModel = ProjectCreate(
            UUID.randomUUID(),
            title,
            ownerId
        )

        Assertions.assertDoesNotThrow( {
            state = projectEsService.createOne(projectCreateModel)
        }, "can't create new project")

        var userAddState: ProjectAggregateState? = null

        var projectId = state?.getId()

        Assertions.assertNotNull(projectId)

        Assertions.assertDoesNotThrow( {
            userAddState = projectEsService.addUser(projectId!!, participants[0])
        }, "can't add user")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(userAddState!!.participants, MutableList<UUID>(1, {
                participants[0]
            }), "project participants doesn't match") },
            Executable { Assertions.assertEquals(userAddState!!.name, title,
                "project names doesn't match") },
            Executable { Assertions.assertEquals(userAddState!!.owner, ownerId,
                "project owners doesn't match") }
        )
    }

}
