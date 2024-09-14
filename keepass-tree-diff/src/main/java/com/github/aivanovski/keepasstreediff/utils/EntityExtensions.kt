package com.github.aivanovski.keepasstreediff.utils

import com.github.aivanovski.keepasstreediff.entity.EntryEntity
import com.github.aivanovski.keepasstreediff.entity.Field
import com.github.aivanovski.keepasstreediff.entity.GroupEntity
import com.github.aivanovski.keepasstreediff.entity.InternalFieldEntity
import com.github.aivanovski.keepasstreediff.entity.UUIDField
import com.github.aivanovski.keepasstreediff.utils.Fields.FIELD_UUID
import java.util.UUID

internal fun Field<*>.toInternalFieldEntity(): InternalFieldEntity {
    return InternalFieldEntity(
        uuid = UUID(0, name.hashCode().toLong()),
        name = name,
        source = this
    )
}

internal fun EntryEntity.getUuidField(): UUIDField {
    return UUIDField(
        name = FIELD_UUID,
        value = uuid
    )
}

internal fun GroupEntity.getUuidField(): UUIDField {
    return UUIDField(
        name = FIELD_UUID,
        value = uuid
    )
}