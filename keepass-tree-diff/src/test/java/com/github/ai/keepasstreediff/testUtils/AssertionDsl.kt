package com.github.ai.keepasstreediff.testUtils

import com.github.ai.keepasstreediff.entity.DiffEvent
import com.github.ai.keepasstreediff.entity.DiffEventType
import com.github.ai.keepasstreediff.entity.Entity
import com.github.ai.keepasstreediff.utils.getType
import io.kotest.matchers.shouldBe
import java.util.UUID

internal object AssertionDsl {

    fun List<DiffEvent<Entity>>.shouldBe(
        content: DiffAssertionBuilder.() -> Unit
    ) {
        DiffAssertionBuilder(this)
            .apply {
                content.invoke(this)
            }
    }

    class DiffAssertionBuilder(
        private val actualEvents: List<DiffEvent<Entity>>
    ) {

        private var index = 0

        fun size(size: Int) {
            actualEvents.size shouldBe size
        }

        fun update(
            oldParent: UUID,
            newParent: UUID,
            oldEntity: Entity,
            newEntity: Entity
        ) {
            val actualEvent = actualEvents[index]

            actualEvent.getType() shouldBe DiffEventType.UPDATE

            val updateEvent = actualEvent as DiffEvent.Update

            updateEvent.oldParentUuid shouldBe oldParent
            updateEvent.newParentUuid shouldBe newParent

            updateEvent.oldEntity shouldBe oldEntity
            updateEvent.newEntity shouldBe newEntity

            index++
        }

        fun delete(
            parent: UUID,
            entity: Entity
        ) {
            val actualEvent = actualEvents[index]

            actualEvent.getType() shouldBe DiffEventType.DELETE

            val deleteEvent = actualEvent as DiffEvent.Delete
            deleteEvent.parentUuid shouldBe parent
            deleteEvent.entity shouldBe entity

            index++
        }

        fun insert(
            parent: UUID,
            entity: Entity
        ) {
            val actualEvent = actualEvents[index]

            actualEvent.getType() shouldBe DiffEventType.INSERT

            val insertEvent = actualEvent as DiffEvent.Insert
            insertEvent.parentUuid shouldBe parent
            insertEvent.entity shouldBe entity

            index++
        }
    }
}