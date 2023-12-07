package ru.quipy.task.eda.api

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "aggregate-task")
class TaskAggregate : Aggregate