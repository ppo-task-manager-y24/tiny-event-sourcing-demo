package ru.quipy.project

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.project.eda.logic.ProjectAggregateState
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.logic.changeStatus
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusChangedInTaskEvent
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.streams.annotation.SubscribeEvent
import java.util.*
import javax.annotation.PostConstruct

@Service
@AggregateSubscriber(StatusAggregate::class, "projects::status-subscriber")
class StatusEventsProjectSubscriber(
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val projectEsService: EventSourcingService<UUID, ProjectAggregate, ProjectAggregateState>
) {
    private val logger: Logger = LoggerFactory.getLogger(StatusEventsProjectSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.subscribe<StatusAggregate>(this)
    }

    @SubscribeEvent
    fun statusChangedInTaskEventSubscriber(event: StatusChangedInTaskEvent) {
        projectEsService.updateSerial(event.projectId) {
            it.changeStatus(event.taskId, event.statusId)
        }
    }
}