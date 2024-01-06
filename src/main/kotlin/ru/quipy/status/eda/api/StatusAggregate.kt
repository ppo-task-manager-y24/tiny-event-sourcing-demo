package ru.quipy.status.eda.api

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "aggregate-status")
class StatusAggregate : Aggregate
