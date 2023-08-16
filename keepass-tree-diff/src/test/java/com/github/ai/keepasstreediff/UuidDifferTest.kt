package com.github.ai.keepasstreediff

import com.github.ai.keepasstreediff.TestData.CUSTOM_VALUE
import com.github.ai.keepasstreediff.TestData.ENTRY_1
import com.github.ai.keepasstreediff.TestData.ENTRY_1_COPY
import com.github.ai.keepasstreediff.TestData.ENTRY_1_MODIFIED
import com.github.ai.keepasstreediff.TestData.ENTRY_2
import com.github.ai.keepasstreediff.TestData.FIELD_CUSTOM
import com.github.ai.keepasstreediff.TestData.FIELD_TITLE
import com.github.ai.keepasstreediff.TestData.FIELD_USERNAME
import com.github.ai.keepasstreediff.TestData.GROUP_A
import com.github.ai.keepasstreediff.TestData.GROUP_B
import com.github.ai.keepasstreediff.TestData.GROUP_C
import com.github.ai.keepasstreediff.TestData.ROOT
import com.github.ai.keepasstreediff.testUtils.AssertionDsl.shouldBe
import com.github.ai.keepasstreediff.testUtils.TreeDsl.tree
import com.github.ai.keepasstreediff.testUtils.getField
import com.github.ai.keepasstreediff.testUtils.modify
import com.github.ai.keepasstreediff.testUtils.sortForAssertionNew
import org.junit.jupiter.api.Test

class UuidDifferTest {

    // TODO: fix commented tests

    @Test
    fun `diff should detect insertions and deletions for groups and entries`() {
        // arrange
        val lhs = tree(ROOT) {
            group(GROUP_A) {
                entry(ENTRY_1)
            }
            group(GROUP_B)
        }

        val rhs = tree(ROOT) {
            group(GROUP_A) {
                entry(ENTRY_2)
            }
            group(GROUP_C)
        }

        // act
        val diff = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        diff.shouldBe {
            size(4)

            delete(parent = ROOT.uuid, GROUP_B)
            insert(parent = ROOT.uuid, GROUP_C)

            delete(parent = GROUP_A.uuid, ENTRY_1)
            insert(parent = GROUP_A.uuid, ENTRY_2)
        }
    }

    @Test
    fun `diff should detect entry insertion`() {
        // arrange
        val lhs = tree(ROOT) {
            entry(ENTRY_1)
        }
        val rhs = tree(ROOT) {
            entry(ENTRY_1)
            entry(ENTRY_2)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)
            insert(parent = ROOT.uuid, ENTRY_2)
        }
    }

    @Test
    fun `diff should detect entry deletion`() {
        // arrange
        val lhs = tree(ROOT) {
            entry(ENTRY_1)
            entry(ENTRY_2)
        }
        val rhs = tree(ROOT) {
            entry(ENTRY_1)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)
            delete(parent = ROOT.uuid, ENTRY_2)
        }
    }

    @Test
    fun `diff should detect entry update`() {
        // arrange
        val lhs = tree(ROOT) {
            entry(ENTRY_1)
        }
        val rhs = tree(ROOT) {
            entry(ENTRY_1_MODIFIED)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)

            update(
                oldParent = ENTRY_1.uuid,
                newParent = ENTRY_1_MODIFIED.uuid,
                oldEntity = ENTRY_1.getField(FIELD_USERNAME),
                newEntity = ENTRY_1_MODIFIED.getField(FIELD_USERNAME)
            )
        }
    }

//    @Test
//    fun `diff should detect entry insertion and deletion`() {
//        // arrange
//        val lhs = tree(ROOT) {
//            group(GROUP_A) {
//                entry(ENTRY_1)
//            }
//            group(GROUP_B)
//        }
//        val rhs = tree(ROOT) {
//            group(GROUP_A)
//            group(GROUP_B) {
//                entry(ENTRY_1)
//            }
//        }
//
//        // act
//        val events = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        events.shouldBe {
//            size(2)
//
//            delete(parent = GROUP_A.uuid, ENTRY_1)
//            insert(parent = GROUP_B.uuid, ENTRY_1)
//        }
//    }

    @Test
    fun `diff should detect field insertion`() {
        // arrange
        val modifiedEntry = ENTRY_1.modify(
            newFields = mapOf(
                FIELD_CUSTOM to CUSTOM_VALUE
            )
        )
        val lhs = tree(ROOT) {
            entry(ENTRY_1)
        }
        val rhs = tree(ROOT) {
            entry(modifiedEntry)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)

            insert(parent = ENTRY_1.uuid, modifiedEntry.getField(FIELD_CUSTOM))
        }
    }

    @Test
    fun `diff should detect field deletion`() {
        // arrange
        val modifiedEntry = ENTRY_1.modify(
            newFields = mapOf(
                FIELD_CUSTOM to CUSTOM_VALUE
            )
        )
        val lhs = tree(ROOT) {
            entry(modifiedEntry)
        }
        val rhs = tree(ROOT) {
            entry(ENTRY_1)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)

            delete(parent = modifiedEntry.uuid, modifiedEntry.getField(FIELD_CUSTOM))
        }
    }

    @Test
    fun `diff should detect field update`() {
        // arrange
        val modifiedEntry = ENTRY_1.modify(
            newFields = mapOf(
                FIELD_USERNAME to "modified-username"
            )
        )
        val lhs = tree(ROOT) {
            entry(ENTRY_1)
        }
        val rhs = tree(ROOT) {
            entry(modifiedEntry)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)

            update(
                oldParent = ENTRY_1.uuid,
                newParent = modifiedEntry.uuid,
                oldEntity = ENTRY_1.getField(FIELD_USERNAME),
                newEntity = modifiedEntry.getField(FIELD_USERNAME)
            )
        }
    }

    @Test
    fun `diff should detect group insertion`() {
        // arrange
        val lhs = tree(ROOT)
        val rhs = tree(ROOT) {
            group(GROUP_A)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)

            insert(parent = ROOT.uuid, GROUP_A)
        }
    }

    @Test
    fun `diff should detect group deletion`() {
        // arrange
        val lhs = tree(ROOT) {
            group(GROUP_A)
        }
        val rhs = tree(ROOT) {
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)

            delete(parent = ROOT.uuid, GROUP_A)
        }
    }

//    @Test
//    fun `diff should detect uuid update for entry`() {
//        // arrange
//        val modifiedEntry = ENTRY_1.modify(
//            uuid = createUuidFrom(ENTRY_2.uuid)
//        )
//        val lhs = tree(ROOT) {
//            entry(ENTRY_1)
//        }
//        val rhs = tree(ROOT) {
//            entry(modifiedEntry)
//        }
//
//        // act
//        val events = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        events.shouldBe {
//            size(1)
//
//            update(
//                oldParent = ENTRY_1.uuid,
//                newParent = modifiedEntry.uuid,
//                oldEntity = ENTRY_1.getUuidField(),
//                newEntity = modifiedEntry.getUuidField()
//            )
//        }
//    }

//    @Test
//    fun `diff should detect uuid update for group`() {
//        // arrange
//        val modifiedGroup = GROUP_A.modify(
//            uuid = GROUP_B.uuid
//        )
//        val lhs = tree(ROOT) {
//            group(GROUP_A)
//        }
//        val rhs = tree(ROOT) {
//            group(modifiedGroup)
//        }
//
//        // act
//        val events = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        events.shouldBe {
//            size(1)
//
//            update(
//                oldParent = GROUP_A.uuid,
//                newParent = modifiedGroup.uuid,
//                oldEntity = GROUP_A.getUuidField(),
//                newEntity = modifiedGroup.getUuidField()
//            )
//        }
//    }

//    @Test
//    fun `diff should detect group insertion and deletion`() {
//        // arrange
//        val lhs = tree(ROOT) {
//            group(GROUP_A) {
//                group(GROUP_C)
//            }
//            group(GROUP_B)
//        }
//        val rhs = tree(ROOT) {
//            group(GROUP_A)
//            group(GROUP_B) {
//                group(GROUP_C)
//            }
//        }
//
//        // act
//        val events = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        events.shouldBe {
//            size(2)
//
//            delete(parent = GROUP_A.uuid, GROUP_C)
//            insert(parent = GROUP_B.uuid, GROUP_C)
//        }
//    }

    @Test
    fun `diff should detect group field update`() {
        // arrange
        val modifiedGroup = GROUP_A.modify(
            newFields = mapOf(
                FIELD_TITLE to GROUP_A.name + " modified"
            )
        )
        val lhs = tree(ROOT) {
            group(GROUP_A) {
                group(GROUP_B) {
                    group(GROUP_C)
                }
            }
        }
        val rhs = tree(ROOT) {
            group(modifiedGroup) {
                group(GROUP_B) {
                    group(GROUP_C)
                }
            }
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(1)

            update(
                oldParent = GROUP_A.uuid,
                newParent = modifiedGroup.uuid,
                oldEntity = GROUP_A.getField(FIELD_TITLE),
                newEntity = modifiedGroup.getField(FIELD_TITLE)
            )
        }
    }

    @Test
    fun `diff should include entries inside group`() {
        // arrange
        val lhs = tree(ROOT) {
            group(GROUP_A) {
                entry(ENTRY_1)
            }
        }
        val rhs = tree(ROOT) {
            group(GROUP_B) {
                entry(ENTRY_2)
            }
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(4)

            delete(parent = ROOT.uuid, GROUP_A)
            insert(parent = ROOT.uuid, GROUP_B)
            delete(parent = GROUP_A.uuid, ENTRY_1)
            insert(parent = GROUP_B.uuid, ENTRY_2)
        }
    }

    @Test
    fun `diff should work for entries with similar names`() {
        // arrange
        val lhs = tree(ROOT) {
            entry(ENTRY_1)
            entry(ENTRY_1_COPY)
            entry(ENTRY_2)
        }
        val rhs = tree(ROOT) {
            entry(ENTRY_1)
            entry(ENTRY_2)
            entry(TestData.ENTRY_2_COPY)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(2)

            delete(parent = ROOT.uuid, ENTRY_1_COPY)
            insert(parent = ROOT.uuid, TestData.ENTRY_2_COPY)
        }
    }

    @Test
    fun `diff should work for groups with similar names`() {
        // arrange
        val lhs = tree(ROOT) {
            group(GROUP_A)
            group(TestData.GROUP_A_COPY)
            group(GROUP_B)
        }
        val rhs = tree(ROOT) {
            group(GROUP_A)
            group(GROUP_B)
            group(TestData.GROUP_B_COPY)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(2)

            delete(parent = ROOT.uuid, TestData.GROUP_A_COPY)
            insert(parent = ROOT.uuid, TestData.GROUP_B_COPY)
        }
    }

    @Test
    fun `diff should work if group and entry has similar names`() {
        // arrange
        val lhs = tree(ROOT) {
            group(TestData.GROUP_ENTRY_1)
            entry(ENTRY_1)
            group(TestData.GROUP_ENTRY_2)
        }
        val rhs = tree(ROOT) {
            group(TestData.GROUP_ENTRY_1)
            group(TestData.GROUP_ENTRY_2)
            entry(ENTRY_2)
        }

        // act
        val events = UuidDiffer().diff(lhs, rhs).sortForAssertionNew()

        // assert
        events.shouldBe {
            size(2)

            delete(parent = ROOT.uuid, ENTRY_1)
            insert(parent = ROOT.uuid, ENTRY_2)
        }
    }

//    @Test
//    fun `diff should work with simple structure`() {
//        // arrange
//        val lhs = tree(ROOT) {
//            group(GROUP_A) {
//                entry(ENTRY_1)
//                entry(ENTRY_2)
//            }
//            group(GROUP_B) {
//                entry(TestData.ENTRY_3)
//                entry(TestData.ENTRY_4)
//            }
//        }
//        val rhs = tree(ROOT) {
//            group(GROUP_A) {
//                entry(ENTRY_1)
//                entry(ENTRY_2)
//                entry(TestData.ENTRY_5)
//            }
//            group(GROUP_B) {
//                entry(TestData.ENTRY_4)
//            }
//            group(GROUP_D) {
//                entry(TestData.ENTRY_3)
//            }
//        }
//
//        // act
//        val diff = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        diff.shouldBe {
//            size(4)
//
//            insert(parent = ROOT.uuid, GROUP_D)
//            delete(parent = GROUP_B.uuid, TestData.ENTRY_3)
//            insert(parent = GROUP_D.uuid, TestData.ENTRY_3)
//            insert(parent = GROUP_A.uuid, TestData.ENTRY_5)
//        }
//    }

//    @Test
//    fun `diff should work with complex structure`() {
//        // arrange
//        val lhs = tree(ROOT) {
//            group(GROUP_A) {
//                entry(ENTRY_1)
//                entry(ENTRY_2)
//                group(GROUP_B) {
//                    entry(TestData.ENTRY_3)
//                    entry(TestData.ENTRY_4)
//                }
//                group(GROUP_C) {
//                    entry(TestData.ENTRY_5)
//                    entry(ENTRY_6)
//                }
//                group(GROUP_D)
//            }
//        }
//        val rhs = tree(ROOT) {
//            group(GROUP_A) {
//                entry(ENTRY_1_MODIFIED)
//                entry(ENTRY_1_COPY)
//                entry(ENTRY_2)
//                group(GROUP_B) {
//                    entry(TestData.ENTRY_3)
//                    entry(TestData.ENTRY_4)
//                    group(GROUP_C) {
//                        entry(TestData.ENTRY_5)
//                    }
//                    group(GROUP_E)
//                }
//            }
//        }
//
//        // act
//        val events = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        events.shouldBe {
//            size(7)
//
//            delete(parent = GROUP_A.uuid, GROUP_C)
//            delete(parent = GROUP_A.uuid, GROUP_D)
//            insert(parent = GROUP_B.uuid, GROUP_C)
//            insert(parent = GROUP_B.uuid, GROUP_E)
//            delete(parent = GROUP_C.uuid, ENTRY_6)
//            insert(parent = GROUP_A.uuid, ENTRY_1_COPY)
//            update(
//                oldParent = ENTRY_1.uuid,
//                newParent = ENTRY_1_MODIFIED.uuid,
//                oldEntity = ENTRY_1.getField(FIELD_USERNAME),
//                newEntity = ENTRY_1_MODIFIED.getField(FIELD_USERNAME)
//            )
//        }
//    }

//    @Test
//    fun `diff should detect insertions and deletions for entries and groups`() {
//        // arrange
//        val lhs = tree(ROOT) {
//            group(GROUP_A) {
//                entry(ENTRY_1)
//            }
//            group(GROUP_B)
//        }
//
//        val rhs = tree(ROOT) {
//            group(GROUP_A) {
//                entry(ENTRY_2)
//            }
//            group(GROUP_C)
//        }
//
//        // act
//        val diff = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        diff.shouldBe {
//            size(4)
//
//            delete(parent = ROOT.uuid, GROUP_B)
//            insert(parent = ROOT.uuid, GROUP_C)
//
//            delete(parent = GROUP_A.uuid, ENTRY_1)
//            insert(parent = GROUP_A.uuid, ENTRY_2)
//        }
//    }
//
//    @Test
//    fun `diff should detect field update in group`() {
//        // arrange
//        val lhs = tree(ROOT) {
//            group(GROUP_A)
//        }
//        val rhs = tree(ROOT) {
//            group(GROUP_A_MODIFIED)
//        }
//
//        // act
//        val diff = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        diff.shouldBe {
//            size(1)
//
//            update(
//                oldParent = GROUP_A.uuid,
//                newParent = GROUP_A.uuid,
//                oldEntity = GROUP_A.getField(FIELD_TITLE),
//                newEntity = GROUP_A_MODIFIED.getField(FIELD_TITLE)
//            )
//        }
//    }
//
//    @Test
//    fun `diff should detect field update in entry`() {
//        // arrange
//        val lhs = tree(ROOT) {
//            entry(ENTRY_1)
//        }
//        val rhs = tree(ROOT) {
//            entry(ENTRY_1_MODIFIED)
//        }
//
//        // act
//        val diff = NewUuidDiffer().diff(lhs, rhs).sortForAssertionNew()
//
//        // assert
//        diff.shouldBe {
//            size(1)
//
//            update(
//                oldParent = ENTRY_1.uuid,
//                newParent = ENTRY_1.uuid,
//                oldEntity = ENTRY_1.getField(FIELD_USERNAME),
//                newEntity = ENTRY_1_MODIFIED.getField(FIELD_USERNAME)
//            )
//        }
//    }
}