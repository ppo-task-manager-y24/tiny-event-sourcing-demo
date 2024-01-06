package ru.quipy.status.eda.projections

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.quipy.status.eda.api.StatusAddedEvent
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusDeletedEvent
import ru.quipy.status.eda.projections.status_view.StatusViewService
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.streams.annotation.SubscribeEvent
import javax.annotation.PostConstruct

@Service
@AggregateSubscriber(StatusAggregate::class, "projection::status-subscriber")
class StatusEventsSubscriber(
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val statusViewService: StatusViewService
) {

    val logger: Logger = LoggerFactory.getLogger(StatusEventsSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.subscribe<StatusAggregate>(this)
    }

    @SubscribeEvent
    fun statusAddedEventSubscriber(event: StatusAddedEvent) {
        statusViewService.createStatus(event.projectId, event.statusId, event.statusName, event.color)
        logger.info("status created: ID = {}, name = {}, color = {}", event.statusId, event.statusName, event.color)
    }

    @SubscribeEvent
    fun statusDeletedEventSubscriber(event: StatusDeletedEvent) {
        statusViewService.deleteStatus(event.projectId, event.statusId)
        logger.info("Status deleted: ID = {}", event.statusId)
    }
}