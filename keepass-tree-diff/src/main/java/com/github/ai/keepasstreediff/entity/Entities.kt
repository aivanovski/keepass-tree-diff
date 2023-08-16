package com.github.ai.keepasstreediff.entity

import com.github.ai.keepasstreediff.utils.Fields.FIELD_TITLE
import com.github.ai.keepasstreediff.utils.getOrThrow
import java.util.UUID

sealed interface Entity {
    val name: String
}

sealed interface TreeEntity : Entity {
    val uuid: UUID
}

data class GroupEntity(
    override val uuid: UUID,
    val fields: Map<String, FieldEntity>,
) : TreeEntity {

    override val name: String = fields.getOrThrow(FIELD_TITLE).value
}

data class EntryEntity(
    override val uuid: UUID,
    val fields: Map<String, FieldEntity>,
) : TreeEntity {

    override val name: String = fields.getOrThrow(FIELD_TITLE).value
}

data class FieldEntity(
    override val name: String,
    val value: String
) : Entity

internal data class InternalFieldEntity(
    override val uuid: UUID,
    override val name: String,
    val source: FieldEntity,
) : TreeEntity
