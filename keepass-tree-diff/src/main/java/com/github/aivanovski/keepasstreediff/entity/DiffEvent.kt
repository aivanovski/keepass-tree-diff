package com.github.aivanovski.keepasstreediff.entity

import java.util.UUID

sealed class DiffEvent<T : Entity> {

    data class Insert<T : Entity>(
        val parentUuid: UUID?,
        val entity: T
    ) : DiffEvent<T>()

    data class Delete<T : Entity>(
        val parentUuid: UUID?,
        val entity: T
    ) : DiffEvent<T>()

    data class Update<T : Entity>(
        val oldParentUuid: UUID?,
        val newParentUuid: UUID?,
        val oldEntity: T,
        val newEntity: T
    ) : DiffEvent<T>()
}