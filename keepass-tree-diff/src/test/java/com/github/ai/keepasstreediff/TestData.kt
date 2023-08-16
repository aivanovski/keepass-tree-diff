package com.github.ai.keepasstreediff

import com.github.ai.keepasstreediff.entity.EntryEntity
import com.github.ai.keepasstreediff.entity.FieldEntity
import com.github.ai.keepasstreediff.entity.GroupEntity
import com.github.ai.keepasstreediff.testUtils.createUuidFrom
import com.github.ai.keepasstreediff.testUtils.modify

internal object TestData {

    const val FIELD_TITLE = "Title"
    const val FIELD_USERNAME = "UserName"
    const val FIELD_PASSWORD = "Password"
    const val FIELD_URL = "URL"
    const val FIELD_NOTES = "Notes"
    const val FIELD_CUSTOM = "custom-field"

    const val CUSTOM_VALUE = "custom-value"

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
                FIELD_TITLE to FieldEntity(FIELD_TITLE, title)
            )
        )
    }

    private fun newEntry(index: Int): EntryEntity {
        return EntryEntity(
            uuid = createUuidFrom(index + 100),
            fields = mapOf(
                FIELD_TITLE to FieldEntity(FIELD_TITLE, "Title $index"),
                FIELD_USERNAME to FieldEntity(FIELD_USERNAME, "UserName $index"),
                FIELD_PASSWORD to FieldEntity(FIELD_PASSWORD, "Password $index"),
                FIELD_URL to FieldEntity(FIELD_URL, "URL $index"),
                FIELD_NOTES to FieldEntity(FIELD_NOTES, "Notes $index")
            )
        )
    }
}