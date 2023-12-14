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
import ru.quipy.core.EventSourcingService
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.ProjectService
import ru.quipy.project.dto.ProjectCreate
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.logic.create
import ru.quipy.projections.project_view.ProjectViewService
import ru.quipy.user.UserEntity
import ru.quipy.user.UserServiceImpl
import ru.quipy.user.dto.UserLogin
import ru.quipy.user.dto.UserModel
import ru.quipy.user.dto.UserRegister
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.logic.UserAggregateState
import java.lang.Exception
import java.util.*

@SpringBootTest
class ProjectTest {
    companion object {
//        private
    }

    @Autowired
    private lateinit var projectService: ProjectService

    @Autowired
    private lateinit var projectViewService: ProjectViewService

    @Test
    fun createProject() {
        val id = UUID.randomUUID()
    }
}
