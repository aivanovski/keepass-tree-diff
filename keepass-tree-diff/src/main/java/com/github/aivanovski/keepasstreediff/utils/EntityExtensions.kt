package com.github.aivanovski.keepasstreediff.utils

import com.github.aivanovski.keepasstreediff.entity.EntryEntity
import com.github.aivanovski.keepasstreediff.entity.FieldEntity
import com.github.aivanovski.keepasstreediff.entity.GroupEntity
import com.github.aivanovski.keepasstreediff.entity.InternalFieldEntity
import com.github.aivanovski.keepasstreediff.utils.Fields.FIELD_UUID
import java.util.UUID

internal fun FieldEntity.toInternalFieldEntity(): InternalFieldEntity {
    return InternalFieldEntity(
        uuid = UUID(0, name.hashCode().toLong()),
        name = name,
        source = this
    )
}

internal fun EntryEntity.getUuidField(): FieldEntity {
    return FieldEntity(
        name = FIELD_UUID,
        value = uuid.toString()
    )
}

internal fun GroupEntity.getUuidField(): FieldEntity {
    return FieldEntity(
        name = FIELD_UUID,
        value = uuid.toString()
    )
}