package ru.quipy.status

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StatusRepository : MongoRepository<StatusEntity, String> {
    @Query("{projectId: ?0, isDeleted: False}")
    fun findByProject(projectId: UUID): List<StatusEntity>?

    @Query("{statusName:  ?0, isDeleted: False}")
    fun findByName(statusName: String): StatusEntity?

    @Query("{statusId:  ?0, isDeleted:  False}")
    fun findById(statusId: UUID): StatusEntity?
}