package com.github.ai.keepasstreediff.utils

import com.github.ai.keepasstreediff.entity.EntryEntity
import com.github.ai.keepasstreediff.entity.FieldEntity
import com.github.ai.keepasstreediff.entity.GroupEntity
import com.github.ai.keepasstreediff.entity.InternalFieldEntity
import com.github.ai.keepasstreediff.utils.Fields.FIELD_UUID
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