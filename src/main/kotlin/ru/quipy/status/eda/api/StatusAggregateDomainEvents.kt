package ru.quipy.status.eda.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val STATUS_CREATED_EVENT = "STATUS_CREATED_EVENT"
const val STATUS_DELETED_EVENT = "STATUS_DELETED_EVENT"

@DomainEvent(name = STATUS_CREATED_EVENT)
class StatusCreatedEvent(
        val statusId: UUID,
        val statusName: String,
        val color: Int,
        createdAt: Long = System.currentTimeMillis(),
) : Event<StatusAggregate>(
        name = STATUS_CREATED_EVENT,
        createdAt = createdAt
)

@DomainEvent(name = STATUS_DELETED_EVENT)
class StatusDeletedEvent(
        val statusId: UUID,
        createdAt: Long = System.currentTimeMillis()
) : Event<StatusAggregate>(
        name = STATUS_CREATED_EVENT,
        createdAt = createdAt
)