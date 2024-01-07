package ru.quipy.status.projections

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.project.eda.api.ProjectAggregate
import ru.quipy.project.eda.api.StatusRemovedFromTaskEvent
import ru.quipy.project.eda.api.StatusUsedInTaskEvent
import ru.quipy.status.eda.api.StatusAggregate
import ru.quipy.status.eda.logic.StatusAggregateState
import ru.quipy.status.eda.logic.statusRemovedInTask
import ru.quipy.status.eda.logic.statusUsedInTask
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.streams.annotation.SubscribeEvent
import java.util.*
import javax.annotation.PostConstruct

@Service
@AggregateSubscriber(ProjectAggregate::class, "statuses::project-subscriber")
class ProjectEventsSubscriber(
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val statusEsService: EventSourcingService<UUID, StatusAggregate, StatusAggregateState>
) {
    private val logger: Logger = LoggerFactory.getLogger(ProjectEventsSubscriber::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.subscribe<ProjectAggregate>(this)
    }

    @SubscribeEvent
    fun statusUsedInTaskEventSubscriber(event: StatusUsedInTaskEvent) {
        statusEsService.update(event.projectId) {
            it.statusUsedInTask(event.taskId, event.statusId)
        }
    }

    @SubscribeEvent
    fun statusRemovedFromTaskSubscriber(event: StatusRemovedFromTaskEvent) {
        statusEsService.update(event.projectId) {
            it.statusRemovedInTask(event.taskId, event.statusId)
        }
    }
}