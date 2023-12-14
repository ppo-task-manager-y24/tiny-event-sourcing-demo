package ru.quipy.projections.task_view

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskViewRepository : MongoRepository<TaskViewEntity, UUID> {
}
