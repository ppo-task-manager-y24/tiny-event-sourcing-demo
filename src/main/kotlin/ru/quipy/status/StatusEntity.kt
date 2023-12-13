package ru.quipy.status

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("status")
class StatusEntity(projectId: UUID, name: String, color: String) {
    @Id
    var statusId: UUID = UUID.randomUUID()
    @Id
    lateinit var projectId: UUID
    lateinit var statusName: String
    lateinit var color: String
    var isDeleted: Boolean = false
}
