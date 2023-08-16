package com.github.ai.keepasstreediff.testUtils

import com.github.ai.keepasstreediff.entity.EntryEntity
import com.github.ai.keepasstreediff.entity.FieldEntity
import com.github.ai.keepasstreediff.entity.GroupEntity
import com.github.ai.keepasstreediff.utils.getOrThrow
import java.util.UUID

internal fun GroupEntity.modify(
    uuid: UUID = this.uuid,
    newFields: Map<String, String> = emptyMap()
): GroupEntity {
    val mergedFields = fields.toMutableMap()

    for ((key, field) in newFields.entries) {
        mergedFields[key] = FieldEntity(key, field)
    }

    return GroupEntity(
        uuid = uuid,
        fields = mergedFields
    )
}

internal fun EntryEntity.modify(
    uuid: UUID = this.uuid,
    newFields: Map<String, String> = emptyMap()
): EntryEntity {
    val mergedFields = fields.toMutableMap()

    for ((key, field) in newFields.entries) {
        mergedFields[key] = FieldEntity(key, field)
    }

    return EntryEntity(
        uuid = uuid,
        fields = mergedFields
    )
}

internal fun EntryEntity.getField(fieldName: String): FieldEntity {
    return fields.getOrThrow(fieldName)
}

internal fun GroupEntity.getField(fieldName: String): FieldEntity {
    return fields.getOrThrow(fieldName)
}