package com.github.aivanovski.keepasstreediff

import com.github.aivanovski.keepasstreediff.entity.DiffEvent
import com.github.aivanovski.keepasstreediff.entity.Entity
import com.github.aivanovski.keepasstreediff.entity.EntryEntity
import com.github.aivanovski.keepasstreediff.entity.GroupEntity
import com.github.aivanovski.keepasstreediff.entity.InternalFieldEntity
import com.github.aivanovski.keepasstreediff.entity.TreeNode
import com.github.aivanovski.keepasstreediff.entity.UUIDField
import com.github.aivanovski.keepasstreediff.utils.Fields.FIELD_UUID
import com.github.aivanovski.keepasstreediff.utils.getEntity
import com.github.aivanovski.keepasstreediff.utils.toInternalFieldEntity
import com.github.aivanovski.keepasstreediff.utils.traverseWithParents
import java.util.UUID

abstract class BaseDiffer<NodeType : TreeNode, NodeKey> : Differ {

    internal abstract fun diff(
        lhsRoots: List<NodeType>,
        rhsRoots: List<NodeType>,
        lhsNodeToParentMap: Map<UUID, UUID>,
        rhsNodeToParentMap: Map<UUID, UUID>,
        visited: MutableSet<NodeKey>
    ): List<DiffEvent<Entity>>

    internal fun List<DiffEvent<Entity>>.substituteUpdateEventsWithFieldDiff(
        nodeFactory: (field: InternalFieldEntity) -> NodeType
    ): List<DiffEvent<Entity>> {
        val result = mutableListOf<DiffEvent<Entity>>()

        for (event in this) {
            when {
                event.isGroupUpdate() -> {
                    result.addAll(
                        splitGroupUpdateIntoFieldDiff(
                            source = event,
                            nodeFactory = nodeFactory
                        )
                    )
                }

                event.isEntryUpdate() -> {
                    result.addAll(
                        splitEntryUpdateWithFieldDiff(
                            source = event,
                            nodeFactory = nodeFactory
                        )
                    )
                }

                else -> {
                    result.add(event)
                }
            }
        }

        return result
    }

    private fun splitGroupUpdateIntoFieldDiff(
        source: DiffEvent<Entity>,
        nodeFactory: ((field: InternalFieldEntity) -> NodeType)
    ): List<DiffEvent<Entity>> {
        val event = source as DiffEvent.Update<GroupEntity>

        val oldGroup = event.oldEntity
        val newGroup = event.newEntity

        // left data
        val lhsFields = oldGroup.fields.values.map { field ->
            field.toInternalFieldEntity()
        }

        val lhsNodeToParentMap = lhsFields.associate { field ->
            field.uuid to oldGroup.uuid
        }

        val lshFieldTree = lhsFields.map { field ->
            nodeFactory.invoke(field)
        }

        // right data
        val rhsFields = newGroup.fields.values.map { field ->
            field.toInternalFieldEntity()
        }

        val rhsNodeToParentMap = rhsFields.associate { field ->
            field.uuid to newGroup.uuid
        }

        val rhsFieldTree = rhsFields.map { field ->
            nodeFactory.invoke(field)
        }

        val groupDiff = diff(
            lhsRoots = lshFieldTree,
            rhsRoots = rhsFieldTree,
            lhsNodeToParentMap = lhsNodeToParentMap,
            rhsNodeToParentMap = rhsNodeToParentMap,
            visited = HashSet()
        )

        return when {
            groupDiff.isNotEmpty() -> groupDiff.substituteInternalEntities()

            oldGroup.uuid != newGroup.uuid -> {
                listOf(
                    DiffEvent.Update(
                        oldParentUuid = oldGroup.uuid,
                        newParentUuid = newGroup.uuid,
                        oldEntity = UUIDField(
                            name = FIELD_UUID,
                            value = oldGroup.uuid
                        ),
                        newEntity = UUIDField(
                            name = FIELD_UUID,
                            value = newGroup.uuid
                        )
                    )
                )
            }

            else -> emptyList()
        }
    }

    private fun splitEntryUpdateWithFieldDiff(
        source: DiffEvent<Entity>,
        nodeFactory: ((field: InternalFieldEntity) -> NodeType)
    ): List<DiffEvent<Entity>> {
        val event = source as DiffEvent.Update<EntryEntity>

        val oldEntry = event.oldEntity
        val newEntry = event.newEntity

        // Prepare left data for diffing
        val lhsFields = oldEntry.fields.values.map { field ->
            field.toInternalFieldEntity()
        }

        val lhsNodeToParentMap = lhsFields.associate { field ->
            field.uuid to oldEntry.uuid
        }

        val lhsFieldTree = lhsFields.map { field ->
            nodeFactory.invoke(field)
        }

        // Prepare right data for diffing
        val rhsFields = newEntry.fields.values.map { field ->
            field.toInternalFieldEntity()
        }

        val rhsNodeToParentMap = rhsFields.associate { field ->
            field.uuid to newEntry.uuid
        }

        val rhsFieldTree = rhsFields.map { field ->
            nodeFactory.invoke(field)
        }

        val entryDiff = diff(
            lhsRoots = lhsFieldTree,
            rhsRoots = rhsFieldTree,
            lhsNodeToParentMap = lhsNodeToParentMap,
            rhsNodeToParentMap = rhsNodeToParentMap,
            visited = HashSet()
        )

        return when {
            entryDiff.isNotEmpty() -> {
                entryDiff.substituteInternalEntities()
            }

            oldEntry.uuid != newEntry.uuid -> {
                listOf(
                    DiffEvent.Update(
                        oldParentUuid = oldEntry.uuid,
                        newParentUuid = newEntry.uuid,
                        oldEntity = UUIDField(
                            name = FIELD_UUID,
                            value = oldEntry.uuid
                        ),
                        newEntity = UUIDField(
                            name = FIELD_UUID,
                            value = newEntry.uuid
                        )
                    )
                )
            }

            else -> {
                emptyList()
            }
        }
    }

    protected fun buildUuidToParentMap(root: TreeNode): Map<UUID, UUID> {
        val result = HashMap<UUID, UUID>()

        val parentToNodePairs = root.traverseWithParents()
        for ((parentNode, node) in parentToNodePairs) {
            val nodeUuid = node.entity.uuid
            val parentUuid = parentNode?.entity?.uuid ?: continue

            result[nodeUuid] = parentUuid
        }

        return result
    }

    private fun DiffEvent<Entity>.isInternalFieldUpdate(): Boolean {
        return this.getEntity() is InternalFieldEntity
    }

    private fun DiffEvent<Entity>.toFieldEntityUpdate(): DiffEvent<Entity> {
        return when (this) {
            is DiffEvent.Update -> {
                val event = this as DiffEvent.Update<InternalFieldEntity>

                DiffEvent.Update(
                    oldParentUuid = event.oldParentUuid,
                    newParentUuid = event.newParentUuid,
                    oldEntity = event.oldEntity.source,
                    newEntity = event.newEntity.source
                )
            }

            is DiffEvent.Insert -> {
                val event = this as DiffEvent.Insert<InternalFieldEntity>

                DiffEvent.Insert(
                    parentUuid = event.parentUuid,
                    entity = event.entity.source
                )
            }

            is DiffEvent.Delete -> {
                val event = this as DiffEvent.Delete<InternalFieldEntity>

                DiffEvent.Delete(
                    parentUuid = event.parentUuid,
                    entity = event.entity.source
                )
            }
        }
    }

    private fun List<DiffEvent<Entity>>.substituteInternalEntities():
        List<DiffEvent<Entity>> {
        return this.map { event ->
            when {
                event.isInternalFieldUpdate() -> event.toFieldEntityUpdate()
                else -> event
            }
        }
    }

    private fun DiffEvent<Entity>.isEntryUpdate(): Boolean {
        return (
            this is DiffEvent.Update &&
                this.oldEntity is EntryEntity &&
                this.newEntity is EntryEntity
            )
    }

    private fun DiffEvent<Entity>.isGroupUpdate(): Boolean {
        return (
            this is DiffEvent.Update &&
                this.oldEntity is GroupEntity &&
                this.newEntity is GroupEntity
            )
    }
}