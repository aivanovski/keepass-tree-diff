package com.github.aivanovski.keepasstreediff

import com.github.aivanovski.keepasstreediff.entity.BinaryField
import com.github.aivanovski.keepasstreediff.entity.EntryEntity
import com.github.aivanovski.keepasstreediff.entity.Field
import com.github.aivanovski.keepasstreediff.entity.GroupEntity
import com.github.aivanovski.keepasstreediff.entity.StringField
import com.github.aivanovski.keepasstreediff.entity.TimestampField
import com.github.aivanovski.keepasstreediff.testUtils.createUuidFrom
import com.github.aivanovski.keepasstreediff.testUtils.modify
import java.security.MessageDigest
import java.util.Base64

internal object TestData {

    const val FIELD_TITLE = "Title"
    const val FIELD_USERNAME = "UserName"
    const val FIELD_PASSWORD = "Password"
    const val FIELD_URL = "URL"
    const val FIELD_NOTES = "Notes"
    const val FIELD_CUSTOM = "custom-field"

    const val CUSTOM_VALUE = "custom-value"
    private const val BASIC_BINARY_CONTENT = "Dummy binary content %s"

    val ROOT = newGroup('Z', "Root")
    val GROUP_A = newGroup('A')
    val GROUP_B = newGroup('B')
    val GROUP_C = newGroup('C')
    val GROUP_D = newGroup('D')
    val GROUP_E = newGroup('E')

    val GROUP_ENTRY_1 = newGroup('R', "Group Entry 1")
    val GROUP_ENTRY_2 = newGroup('T', "Group Entry 2")

    val GROUP_A_MODIFIED = GROUP_A.modify(
        newFields = mapOf(
            FIELD_TITLE to "Group A Modified"
        )
    )

    val GROUP_A_COPY = GROUP_A.modify(uuid = createUuidFrom(91))
    val GROUP_B_COPY = GROUP_B.modify(uuid = createUuidFrom(92))

    val ENTRY_1 = newEntry(1)
    val ENTRY_2 = newEntry(2)
    val ENTRY_3 = newEntry(3)
    val ENTRY_4 = newEntry(4)
    val ENTRY_5 = newEntry(5)
    val ENTRY_6 = newEntry(6)

    val BINARY_1 = newBinary("1.txt")
    val BINARY_2 = newBinary("2.txt")

    val TIMESTAMP_1 = TimestampField(
        name = "Expiration",
        value = 123L
    )
    val TIMESTAMP_2 = TimestampField(
        name = "Expiration",
        value = 456L
    )

    val ENTRY_1_COPY = ENTRY_1.modify(uuid = createUuidFrom(201))
    val ENTRY_2_COPY = ENTRY_2.modify(uuid = createUuidFrom(202))

    val ENTRY_1_MODIFIED = ENTRY_1.modify(
        newFields = mapOf(
            FIELD_USERNAME to "UserName 1 Modified"
        )
    )

    private fun newGroup(
        char: Char,
        title: String = "Group $char"
    ): GroupEntity {
        return GroupEntity(
            uuid = createUuidFrom(char - 'A'),
            fields = mapOf(
                FIELD_TITLE to StringField(FIELD_TITLE, title)
            )
        )
    }

    fun newEntry(
        index: Int,
        binaries: List<BinaryField> = emptyList(),
        custom: Map<String, Field<*>> = emptyMap()
    ): EntryEntity {
        val fields = mutableMapOf<String, Field<*>>(
            FIELD_TITLE to StringField(FIELD_TITLE, "Title $index"),
            FIELD_USERNAME to StringField(FIELD_USERNAME, "UserName $index"),
            FIELD_PASSWORD to StringField(FIELD_PASSWORD, "Password $index"),
            FIELD_URL to StringField(FIELD_URL, "URL $index"),
            FIELD_NOTES to StringField(FIELD_NOTES, "Notes $index")
        )

        if (binaries.isNotEmpty()) {
            for (binary in binaries) {
                fields[binary.hash] = binary
            }
        }

        fields.putAll(custom)

        return EntryEntity(
            uuid = createUuidFrom(index + 100),
            fields = fields
        )
    }

    private fun newBinary(
        name: String
    ): BinaryField {
        val content = BASIC_BINARY_CONTENT.format(name).toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val sha = digest.digest(content)

        return BinaryField(
            hash = Base64.getEncoder().encodeToString(sha),
            name = name,
            value = content
        )
    }
}