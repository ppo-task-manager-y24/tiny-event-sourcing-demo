package ru.quipy.projections.project_view

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProjectViewRepository : MongoRepository<ProjectViewEntity, UUID> {
}
