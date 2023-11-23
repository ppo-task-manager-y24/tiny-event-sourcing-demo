package ru.quipy.user.projections

import org.springframework.stereotype.Service
import ru.quipy.streams.annotation.AggregateSubscriber
import ru.quipy.user.api.UserAggregate


@Service
@AggregateSubscriber(aggregateClass = UserAggregate::class, subscriberName = "user-subs-stream")
class AnnotationBasedUserEventsSubscriber {
}