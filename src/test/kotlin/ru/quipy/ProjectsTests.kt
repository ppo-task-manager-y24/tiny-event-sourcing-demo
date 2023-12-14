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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.server.ResponseStatusException
import ru.quipy.domain.Event
import ru.quipy.project.ProjectService
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import java.sql.Timestamp
import java.util.*

@SpringBootTest
class ProjectsTests {
    companion object {
        private val id = UUID.randomUUID()
        private val participants = MutableList(2, {
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
            updatedAt
        )

        private val title = "Project title"

        private val projectCreateModel = ProjectCreate(
            id,
            title,
            ownerId
        )
    }

    @Autowired
    lateinit var projectEsService: ProjectService

    @Test
    fun createNewProject() {
        var event: ProjectCreatedEvent? = null

        Assertions.assertDoesNotThrow( {
            event = projectEsService.createOne(projectCreateModel)
        }, "can't create new project")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(event!!.projectId, id, "project ids doesn't match") },
            Executable { Assertions.assertEquals(event!!.projectName, title,
                "project names doesn't match") },
            Executable { Assertions.assertEquals(event!!.projectOwner, ownerId,
            "project owners doesn't match") }
        )
    }

    @Test
    fun addUserToProject() {
        var projectCreatedEvent: ProjectCreatedEvent? = null

        Assertions.assertDoesNotThrow( {
            projectCreatedEvent = projectEsService.createOne(projectCreateModel)
        }, "can't create new project")

        var userAddEvents: List<Event<ProjectAggregate>>? = null

        Assertions.assertDoesNotThrow( {
            userAddEvents = projectEsService.addUser(projectModel.id, participants[0])
        }, "can't add user")

        Assertions.assertAll(
            Executable { Assertions.assertEquals(2, userAddEvents!!.count(), "wrong number of events") },
        )
    }

}
