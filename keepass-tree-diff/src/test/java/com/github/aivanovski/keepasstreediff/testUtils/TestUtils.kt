package com.github.aivanovski.keepasstreediff.testUtils

import com.github.aivanovski.keepasstreediff.entity.DiffEvent
import com.github.aivanovski.keepasstreediff.entity.DiffEventType
import com.github.aivanovski.keepasstreediff.entity.Entity
import com.github.aivanovski.keepasstreediff.entity.EntryEntity
import com.github.aivanovski.keepasstreediff.entity.Field
import com.github.aivanovski.keepasstreediff.entity.GroupEntity
import com.github.aivanovski.keepasstreediff.utils.getEntity
import java.util.UUID

internal fun createUuidFrom(value: Any): UUID {
    return UUID(0, value.hashCode().toLong())
}

/**
 * Sort diff events by entity type, then by event type and then by name.
 * Firstly goes Update events, then Delete events and then Insert events for [GroupEntity].
 * Then events goes is the same order for [EntryEntity] and then for [Field].
 *
 * Example:
 * 1. [DiffEvent.Update] with [GroupEntity]
 * 2. [DiffEvent.Delete] with [GroupEntity]
 * 3. [DiffEvent.Insert] with [GroupEntity]
 *
 * 4. [DiffEvent.Update] with [EntryEntity]
 * 5. [DiffEvent.Delete] with [EntryEntity]
 * 6. [DiffEvent.Insert] with [EntryEntity]
 *
 * 7. [DiffEvent.Update] with [Field]
 * 8. [DiffEvent.Delete] with [Field]
 * 9. [DiffEvent.Insert] with [Field]
 */
internal fun List<DiffEvent<Entity>>.sortForAssertions(): List<DiffEvent<Entity>> {
    val groupEvents = this.mapNotNull { event ->
        if (event.getEntity() is GroupEntity) event else null
    }

    val entryEvents = this.mapNotNull { event ->
        if (event.getEntity() is EntryEntity) event else null
    }

    val fieldEvents = this.mapNotNull { event ->
        if (event.getEntity() is Field<*>) event else null
    }

    val groupEventsSorted = groupEvents.splitByEventTypeNew()
        .values
        .map { events -> events.sortByNameNew() }
        .flatten()

    val entryEventsSorted = entryEvents.splitByEventTypeNew()
        .values
        .map { events -> events.sortByNameNew() }
        .flatten()

    val fieldEventsSorted = fieldEvents.splitByEventTypeNew()
        .values
        .map { events -> events.sortByNameNew() }
        .flatten()

    return groupEventsSorted + entryEventsSorted + fieldEventsSorted
}

private fun List<DiffEvent<Entity>>.splitByEventTypeNew():
    Map<DiffEventType, List<DiffEvent<Entity>>> {
    val updateEvents = this.mapNotNull { event ->
        if (event is DiffEvent.Update<*>) event else null
    }

    val deleteEvents = this.mapNotNull { event ->
        if (event is DiffEvent.Delete<*>) event else null
    }

    val insertEvents = this.mapNotNull { event ->
        if (event is DiffEvent.Insert<*>) event else null
    }

    return mapOf(
        DiffEventType.UPDATE to updateEvents,
        DiffEventType.DELETE to deleteEvents,
        DiffEventType.INSERT to insertEvents
    )
}

private fun List<DiffEvent<Entity>>.sortByNameNew(): List<DiffEvent<Entity>> {
    return this.sortedBy { event ->
        when (event) {
            is DiffEvent.Update -> event.newEntity.name
            is DiffEvent.Delete -> event.entity.name
            is DiffEvent.Insert -> event.entity.name
        }
    }
}