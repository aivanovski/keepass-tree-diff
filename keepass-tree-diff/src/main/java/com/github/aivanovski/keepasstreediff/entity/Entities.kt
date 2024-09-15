package com.github.aivanovski.keepasstreediff.entity

import com.github.aivanovski.keepasstreediff.utils.Fields.FIELD_TITLE
import com.github.aivanovski.keepasstreediff.utils.getOrThrow
import java.util.UUID

sealed interface Entity {
    val name: String
}

sealed interface TreeEntity : Entity {
    val uuid: UUID
}

sealed interface Field<T> : Entity {
    val value: T
}

data class GroupEntity(
    override val uuid: UUID,
    val fields: Map<String, Field<*>>
) : TreeEntity {
    override val name: String = fields.getOrThrow(FIELD_TITLE).value.toString()
}

data class EntryEntity(
    override val uuid: UUID,
    val fields: Map<String, Field<*>>
) : TreeEntity {
    override val name: String = fields.getOrThrow(FIELD_TITLE).value.toString()
}

data class StringField(
    override val name: String,
    override val value: String
) : Field<String>

data class TimestampField(
    override val name: String,
    override val value: Long
) : Field<Long>

data class UUIDField(
    override val name: String,
    override val value: UUID
) : Field<UUID>

data class BinaryField(
    val hash: String,
    override val name: String,
    override val value: ByteArray
) : Field<ByteArray> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinaryField

        if (name != other.name) return false
        if (hash != other.hash) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}

internal data class InternalFieldEntity(
    override val uuid: UUID,
    override val name: String,
    val source: Field<*>
) : TreeEntity