package ru.quipy.status

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("status")
class StatusEntity(name: String, color: String) {
    @Id
    var statusId: UUID = UUID.randomUUID()
    lateinit var statusName: String
    lateinit var color: String
}
