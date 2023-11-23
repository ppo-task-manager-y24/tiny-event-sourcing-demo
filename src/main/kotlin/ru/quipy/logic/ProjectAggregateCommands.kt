package ru.quipy.logic

import ru.quipy.api.*
import ru.quipy.domain.Event
import java.util.*


// Commands : takes something -> returns event
// Here the commands are represented by extension functions, but also can be the class member functions

fun ProjectAggregateState.create(id: UUID, name: String, ownerId: UUID): ProjectCreatedEvent {
    return ProjectCreatedEvent(
        projectId = id,
        projectName = name,
        projectOwner = ownerId,
    )
}

fun ProjectAggregateState.addUser(id: UUID): List<Event<ProjectAggregate>> {
    return ArrayList(
        listOf(
            ProjectParticipantAddedEvent(this.getId(), id),
            ProjectUpdatedEvent(this.getId())
        )
    )
}