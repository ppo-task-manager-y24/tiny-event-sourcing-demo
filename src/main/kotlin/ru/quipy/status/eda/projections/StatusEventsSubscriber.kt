package ru.quipy.status.eda.projections

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
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


//    @PostConstruct
//    fun init() {
//        subscriptionsManager.createSubscriber(StatusAggregate::class, "status-sub") {
//
//            `when`(StatusCreatedEvent::class) { event ->
//                statusViewService.createStatus(event.statusId, event.statusName, event.color)
//                logger.info("status created: ID = {}, name = {}, color = {}", event.statusId, event.statusName, event.color)
//            }
//
//            `when`(StatusDeletedEvent::class) { event ->
//                statusViewService.deleteStatus(event.statusId)
//                logger.info("Status deleted: ID = {}", event.statusId)
//            }
//        }
//
//
//    }

    @PostConstruct
    fun init() {
        subscriptionsManager.subscribe<StatusAggregate>(this)
    }

    @SubscribeEvent
    fun statusCreatedEventSubscriber(event: StatusCreatedEvent) {
        statusViewService.createStatus(event.statusId, event.statusName, event.color)
        logger.info("status created: ID = {}, name = {}, color = {}", event.statusId, event.statusName, event.color)
    }

    @SubscribeEvent
    fun statusDeletedEventSubscriber(event: StatusDeletedEvent) {
        statusViewService.deleteStatus(event.statusId)
        logger.info("Status deleted: ID = {}", event.statusId)
    }
}