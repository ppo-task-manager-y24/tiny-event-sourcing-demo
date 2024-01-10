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
import ru.quipy.domain.Event
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.ProjectService
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.dto.ProjectModel
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.ProjectCreatedEvent
import ru.quipy.project.eda.api.ProjectParticipantAddedEvent
import ru.quipy.project.eda.api.ProjectUpdatedEvent
import java.util.*

@SpringBootTest
class ProjectsTests {
    companion object {
        private val id = UUID.randomUUID()
        private val participants = MutableList(2) { UUID.randomUUID()!! }

        private val tasks = MutableList(2) { UUID.randomUUID()!! }
        private const val name = "projectName"
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
            tasks,
            mutableListOf()
        )

        private const val title = "Project title"

        private val projectCreateModel = ProjectCreate(
            id,
            title,
            ownerId
        )
    }

    @Autowired
    lateinit var projectEsService: ProjectService

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun setUp() {
        cleanDatabase()
    }

    @Test
    fun createNewProject_Succeeds() {

        var projectCreatedEvent: ProjectCreatedEvent? = null

        Assertions.assertDoesNotThrow( { projectCreatedEvent = projectEsService.createProject(projectCreateModel) }, "can't create new project")

        Assertions.assertNotNull(projectCreatedEvent)

        Assertions.assertAll( {
            Executable {
                Assertions.assertEquals(projectCreatedEvent!!.projectOwner, ownerId)
            }
            Executable {
                Assertions.assertEquals(projectCreatedEvent!!.projectId, projectCreateModel.id)
            }
            Executable {
                Assertions.assertEquals(projectCreatedEvent!!.projectName, projectCreateModel.title)
            }
        })

        val state: ProjectAggregateState? = projectEsService.state(projectCreateModel.id)

        Assertions.assertNotNull(state)

        Assertions.assertAll(
            Executable { Assertions.assertEquals(state!!.participants.count(), 0, "project participants doesn't match") },
            Executable { Assertions.assertEquals(state!!.name, title,
                "project names doesn't match") },
            Executable { Assertions.assertEquals(state!!.owner, ownerId,
            "project owners doesn't match") }
        )
    }

    @Test
    fun addUserToProject_Succeeds() {

        setUpSut()

        var events: List<Event<ProjectAggregate>>? = null

        Assertions.assertDoesNotThrow( {
            events = projectEsService.addUserToProject(id, participants[0])
        }, "can't add user")

        Assertions.assertEquals(2, events!!.count(), "wrong number of events produced")

        val projectParticipantAddedEvent: ProjectParticipantAddedEvent? = events!![0] as? ProjectParticipantAddedEvent
        val projectUpdatedEvent: ProjectUpdatedEvent? = events!![1] as? ProjectUpdatedEvent

        val state: ProjectAggregateState = projectEsService.state(id)!!

        Assertions.assertNotNull(projectParticipantAddedEvent)
        Assertions.assertNotNull(projectUpdatedEvent)

        Assertions.assertEquals(projectParticipantAddedEvent!!.participantId, participants[0], "project participants doesn't match")

        Assertions.assertEquals(projectParticipantAddedEvent.projectId, projectCreateModel.id, "project participants doesn't match")

        Assertions.assertEquals(projectUpdatedEvent!!.projectId, projectCreateModel.id)

        Assertions.assertEquals(participants.subList(0, 1), state.participants,
            "project participants doesn't match")
    }

    @Test
    fun addSameUserTwice_Fails() {
        setUpSut()

        Assertions.assertDoesNotThrow {
            projectEsService.addUserToProject(id, participants[0])
        }

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            projectEsService.addUserToProject(id, participants[0])
        }
    }

    private fun setUpSut() {
        Assertions.assertDoesNotThrow({ projectEsService.createProject(projectCreateModel) }, "can't create new project")
    }

    private fun cleanDatabase() {
        mongoTemplate.remove(Query.query(Criteria.where("aggregateId").`is`(id)), "aggregate-project")
        mongoTemplate.remove(Query.query(Criteria.where("_id").`is`(id)), "snapshots")
    }
}
