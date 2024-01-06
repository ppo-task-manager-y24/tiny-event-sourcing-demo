package ru.quipy.status.eda.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.quipy.core.EventSourcingServiceFactory
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.logic.StatusAggregateState
import ru.quipy.status.projections.ProjectEventsSubscriber
import ru.quipy.streams.AggregateEventStreamManager
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct

@Configuration
class StatusConfig {

    private val logger = LoggerFactory.getLogger(StatusConfig::class.java)

    @Autowired
    private lateinit var subscriptionsManager: AggregateSubscriptionsManager

    @Autowired
    private lateinit var eventStreamManager: AggregateEventStreamManager

    @Autowired
    private lateinit var eventSourcingServiceFactory: EventSourcingServiceFactory

    @Bean
    fun statusEsService() = eventSourcingServiceFactory.create<UUID, StatusAggregate, StatusAggregateState>()

    @PostConstruct
    fun init() {
        eventStreamManager.maintenance {
            onRecordHandledSuccessfully { streamName, eventName ->
                logger.info("Stream $streamName successfully processed record of $eventName")
            }

            onBatchRead { streamName, batchSize ->
                logger.info("Stream $streamName read batch size: $batchSize")
            }
        }
    }
}