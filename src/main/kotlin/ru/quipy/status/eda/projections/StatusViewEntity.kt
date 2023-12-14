package ru.quipy.status.eda.projections

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("status")
data class StatusViewEntity(
        @Id
        var statusId: UUID,
        var statusName: String,
        var color: Int,
        var isDeleted: Boolean,
        var createdAt: Long = System.currentTimeMillis(),
        var updatedAt: Long = System.currentTimeMillis()
)