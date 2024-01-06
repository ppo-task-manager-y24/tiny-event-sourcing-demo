package ru.quipy.status.eda.projections.status_view

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StatusViewRepository : MongoRepository<StatusViewEntity, UUID> {
}