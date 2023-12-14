package ru.quipy.status

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import ru.quipy.status.eda.projections.StatusViewEntity
import java.util.*

@Repository
interface StatusRepository : MongoRepository<StatusViewEntity, UUID> {
    @Query("{statusName: ?0, color: ?1, isDeleted: False}")
    fun findByNameAndColor(statusName: String, color: Int): StatusViewEntity?

    @Query("{statusId:  ?0, isDeleted:  False}")
    override fun findById(statusId: UUID): Optional<StatusViewEntity>
}