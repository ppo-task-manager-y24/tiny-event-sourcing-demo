package ru.quipy.status.eda.projections

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.api.StatusCreatedEvent
import ru.quipy.status.eda.api.StatusDeletedEvent
import ru.quipy.streams.AggregateSubscriptionsManager
import javax.annotation.PostConstruct

@Service
class StatusEventsSubscriber {

    val logger: Logger = LoggerFactory.getLogger(StatusEventsSubscriber::class.java)

    @Autowired
    lateinit var subscriptionsManager: AggregateSubscriptionsManager

    @Autowired
    lateinit var statusViewService: StatusViewService

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(StatusAggregate::class, "status-sub") {

            `when`(StatusCreatedEvent::class) { event ->
                statusViewService.createStatus(event.statusId, event.statusName, event.color)
                logger.info("status created: ID = {}, name = {}, color = {}", event.statusId, event.statusName, event.color)
            }

            `when`(StatusDeletedEvent::class) { event ->
                statusViewService.deleteStatus(event.statusId)
                logger.info("Status deleted: ID = {}", event.statusId)
            }
        }
    }
}