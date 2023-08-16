package com.github.ai.keepasstreediff.utils

import com.github.ai.keepasstreediff.entity.DiffEvent
import com.github.ai.keepasstreediff.entity.DiffEventType
import com.github.ai.keepasstreediff.entity.Entity

internal fun DiffEvent<*>.getType(): DiffEventType {
    return when (this) {
        is DiffEvent.Insert -> DiffEventType.INSERT
        is DiffEvent.Delete -> DiffEventType.DELETE
        is DiffEvent.Update -> DiffEventType.UPDATE
    }
}

internal fun DiffEvent<Entity>.getEntity(): Entity {
    return when (this) {
        is DiffEvent.Update -> newEntity
        is DiffEvent.Delete -> entity
        is DiffEvent.Insert -> entity
    }
}