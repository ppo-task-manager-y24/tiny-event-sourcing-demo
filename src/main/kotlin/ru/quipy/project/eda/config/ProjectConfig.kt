package ru.quipy.project.eda.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.quipy.core.EventSourcingServiceFactory
import ru.quipy.logic.ProjectAggregateState
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.streams.AggregateEventStreamManager
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.user.eda.api.UserAggregate
import ru.quipy.user.eda.config.UserConfig
import ru.quipy.user.eda.logic.UserAggregateState
import java.util.*
import javax.annotation.PostConstruct

@Configuration
class ProjectConfig {

    private val logger = LoggerFactory.getLogger(ProjectConfig::class.java)

    @Autowired
    private lateinit var subscriptionsManager: AggregateSubscriptionsManager

    @Autowired
    private lateinit var eventStreamManager: AggregateEventStreamManager

    @Autowired
    private lateinit var eventSourcingServiceFactory: EventSourcingServiceFactory

    @Bean
    fun projectEsService() = eventSourcingServiceFactory.create<UUID, ProjectAggregate, ProjectAggregateState>()

    @PostConstruct
    fun init() {
//        subscriptionsManager.subscribe<UserAggregate>(userEventSubscriber)

        eventStreamManager.maintenance {
            onRecordHandledSuccessfully { streamName, eventName ->
                logger.info("Stream $streamName successfully processed record of $eventName")
            }

            onBatchRead { streamName, batchSize ->
                logger.info("Stream $streamName read batch size: $batchSize")
            }
        }
    }

    @Bean
    fun userESService() = eventSourcingServiceFactory.create<UUID, UserAggregate, UserAggregateState>()
}
