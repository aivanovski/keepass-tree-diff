package com.github.aivanovski.keepasstreediff.testUtils

import com.github.aivanovski.keepasstreediff.entity.EntryEntity
import com.github.aivanovski.keepasstreediff.entity.Field
import com.github.aivanovski.keepasstreediff.entity.GroupEntity
import com.github.aivanovski.keepasstreediff.entity.StringField
import com.github.aivanovski.keepasstreediff.utils.getOrThrow
import java.util.UUID

internal fun GroupEntity.modify(
    uuid: UUID = this.uuid,
    newFields: Map<String, String> = emptyMap()
): GroupEntity {
    val mergedFields = fields.toMutableMap()

    for ((key, field) in newFields.entries) {
        mergedFields[key] = StringField(key, field)
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
        mergedFields[key] = StringField(key, field)
    }

    return EntryEntity(
        uuid = uuid,
        fields = mergedFields
    )
}

internal fun EntryEntity.getField(fieldName: String): Field<*> {
    return fields.getOrThrow(fieldName)
}

internal fun GroupEntity.getField(fieldName: String): Field<*> {
    return fields.getOrThrow(fieldName)
}