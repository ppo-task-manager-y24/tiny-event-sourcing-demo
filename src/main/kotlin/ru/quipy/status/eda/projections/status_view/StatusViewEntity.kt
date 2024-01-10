package ru.quipy.status.eda.projections.status_view

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("status")
data class StatusViewEntity(
        @Id
        var id: UUID,
        var projectId: UUID,
        var statuses: Map<UUID, StatusEntity>
)

data class StatusEntity(
        var projectId: UUID,
        var statusId: UUID,
        var statusName: String,
        var color: Int,
        var isDeleted: Boolean,
        var usedTaskIds: Set<UUID>,
        var createdAt: Long = System.currentTimeMillis(),
        var updatedAt: Long = System.currentTimeMillis()
)