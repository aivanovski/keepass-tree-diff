package com.github.aivanovski.keepasstreediff

import com.github.aivanovski.keepasstreediff.entity.DiffEvent
import com.github.aivanovski.keepasstreediff.entity.Entity
import com.github.aivanovski.keepasstreediff.entity.EntryEntity
import com.github.aivanovski.keepasstreediff.entity.GroupEntity
import com.github.aivanovski.keepasstreediff.entity.MutablePathNode
import com.github.aivanovski.keepasstreediff.entity.TreeEntity
import com.github.aivanovski.keepasstreediff.entity.TreeNode
import com.github.aivanovski.keepasstreediff.utils.convertToMutableNodeTree
import com.github.aivanovski.keepasstreediff.utils.convertToMutablePathNodeTree
import com.github.aivanovski.keepasstreediff.utils.getEntity
import com.github.aivanovski.keepasstreediff.utils.traversePathNode
import java.util.UUID

class PathDiffer : BaseDiffer<MutablePathNode, String>() {

    private val uuidDiffer = UuidDiffer()

    override fun diff(
        lhs: TreeNode,
        rhs: TreeNode
    ): List<DiffEvent<Entity>> {
        val lhsNodeToParentMap = buildUuidToParentMap(lhs)
        val rhsNodeToParentMap = buildUuidToParentMap(rhs)

        return diff(
            lhsRoots = listOf(lhs.convertToMutablePathNodeTree()),
            rhsRoots = listOf(rhs.convertToMutablePathNodeTree()),
            lhsNodeToParentMap = lhsNodeToParentMap,
            rhsNodeToParentMap = rhsNodeToParentMap,
            visited = HashSet()
        )
            .substituteUpdateEventsWithFieldDiff(
                nodeFactory = { field ->
                    MutablePathNode(
                        path = field.name,
                        entity = field
                    )
                }
            )
    }

    override fun diff(
        lhsRoots: List<MutablePathNode>,
        rhsRoots: List<MutablePathNode>,
        lhsNodeToParentMap: Map<UUID, UUID>,
        rhsNodeToParentMap: Map<UUID, UUID>,
        visited: MutableSet<String>
    ): List<DiffEvent<Entity>> {
        val allLhsNodes = lhsRoots.flatMap { node -> node.traversePathNode() }
        val allRhsNodes = rhsRoots.flatMap { node -> node.traversePathNode() }

        val lhsNodesMap = allLhsNodes.groupNodesByPath()
        val rhsNodesMap = allRhsNodes.groupNodesByPath()

        val allPath = HashSet<String>()
            .apply {
                addAll(lhsNodesMap.keys)
                addAll(rhsNodesMap.keys)
            }

        val events = mutableListOf<DiffEvent<out Entity>>()

        for (path in allPath) {
            val lhsNodes = lhsNodesMap[path] ?: emptyList()
            val rhsNodes = rhsNodesMap[path] ?: emptyList()

            // Process case when there are several nodes with the same path
            if (lhsNodes.size > 1 || rhsNodes.size > 1) {
                if (path !in visited) {
                    val visitedUuids = HashSet<UUID>()
                    val diff = uuidDiffer.diff(
                        lhsRoots = lhsNodes.map { node -> node.convertToMutableNodeTree() },
                        rhsRoots = rhsNodes.map { node -> node.convertToMutableNodeTree() },
                        lhsNodeToParentMap = lhsNodeToParentMap,
                        rhsNodeToParentMap = rhsNodeToParentMap,
                        visited = visitedUuids
                    )

                    // TODO: some child nodes may not be marked as visited
                    visited.add(path)
                    events.addAll(diff)
                }

                continue
            }

            val lhs = lhsNodes.firstOrNull()
            val rhs = rhsNodes.firstOrNull()

            val isLhsVisited = lhs?.path in visited
            val isRhsVisited = rhs?.path in visited

            when {
                lhs != null && rhs != null && !isLhsVisited && !isRhsVisited -> {
                    if (lhs.entity != rhs.entity) {
                        events.add(
                            DiffEvent.Update(
                                oldParentUuid = lhsNodeToParentMap[lhs.entity.uuid],
                                newParentUuid = rhsNodeToParentMap[rhs.entity.uuid],
                                oldEntity = lhs.entity,
                                newEntity = rhs.entity
                            )
                        )
                    }

                    events.addAll(
                        diff(
                            lhsRoots = lhs.nodes,
                            rhsRoots = rhs.nodes,
                            lhsNodeToParentMap = lhsNodeToParentMap,
                            rhsNodeToParentMap = rhsNodeToParentMap,
                            visited = visited
                        )
                    )
                }

                // item was removed
                lhs != null && rhs == null && !isLhsVisited -> {
                    events.add(
                        DiffEvent.Delete(
                            parentUuid = lhsNodeToParentMap[lhs.entity.uuid],
                            entity = lhs.entity
                        )
                    )
                }

                // item was added
                lhs == null && rhs != null && !isRhsVisited -> {
                    events.add(
                        DiffEvent.Insert(
                            parentUuid = rhsNodeToParentMap[rhs.entity.uuid],
                            entity = rhs.entity
                        )
                    )
                }
            }

            lhs?.let { node -> visited.add(node.path) }
            rhs?.let { node -> visited.add(node.path) }
        }

        return (events as List<DiffEvent<Entity>>)
            .substituteInsertAndDeleterWithUpdate(
                lhsNodeToParentMap = lhsNodeToParentMap,
                rhsNodeToParentMap = rhsNodeToParentMap
            )
    }

    private fun List<DiffEvent<Entity>>.substituteInsertAndDeleterWithUpdate(
        lhsNodeToParentMap: Map<UUID, UUID>,
        rhsNodeToParentMap: Map<UUID, UUID>
    ): List<DiffEvent<Entity>> {
        val uidToEventsMap = this.groupEventsByUid()
        val result = mutableListOf<DiffEvent<Entity>>()

        for ((_, eventsByUid) in uidToEventsMap) {
            if (eventsByUid.size == 2) {
                val first = eventsByUid[0]
                val second = eventsByUid[1]

                val (deleteEvent, insertEvent) = when {
                    first is DiffEvent.Insert && second is DiffEvent.Delete -> {
                        second to first
                    }

                    first is DiffEvent.Delete && second is DiffEvent.Insert -> {
                        first to second
                    }

                    else -> {
                        result.addAll(eventsByUid)
                        continue
                    }
                }

                val deletedEntity = deleteEvent.getEntity()
                val insertedEntity = insertEvent.getEntity()

                val deletedEntityUuid = when (deletedEntity) {
                    is TreeEntity -> deletedEntity.uuid
                    else -> throw IllegalStateException()
                }

                val insertedEntityUuid = when (insertedEntity) {
                    is TreeEntity -> insertedEntity.uuid
                    else -> throw IllegalStateException()
                }

                val oldParent = lhsNodeToParentMap[deletedEntityUuid]
                val newParent = rhsNodeToParentMap[insertedEntityUuid]

                val isParentSame = (oldParent == newParent)
                val isEntryOrGroup =
                    (deletedEntity is GroupEntity || deletedEntity is EntryEntity)

                when {
                    isParentSame && isEntryOrGroup && deletedEntity == insertedEntity -> {
                        // Some of the parents were changed, but the entity is the same
                        continue
                    }

                    isEntryOrGroup && deletedEntity == insertedEntity -> {
                        result.addAll(eventsByUid)
                    }

                    else -> {
                        result.add(
                            DiffEvent.Update(
                                oldParentUuid = oldParent,
                                newParentUuid = newParent,
                                oldEntity = deletedEntity,
                                newEntity = insertedEntity
                            )
                        )
                    }
                }
            } else {
                result.addAll(eventsByUid)
            }
        }

        return result
    }

    private fun List<MutablePathNode>.groupNodesByPath():
        Map<String, List<MutablePathNode>> {
        val pathToNodesMap = HashMap<String, MutableList<MutablePathNode>>()

        for (node in this) {
            pathToNodesMap[node.path] = pathToNodesMap.getOrDefault(node.path, mutableListOf())
                .apply {
                    add(node)
                }
        }

        return pathToNodesMap
    }

    private fun List<DiffEvent<Entity>>.groupEventsByUid():
        Map<UUID, List<DiffEvent<Entity>>> {
        val uidToEventsMap = HashMap<UUID, MutableList<DiffEvent<Entity>>>()

        for (event in this) {
            val nodeUuid = when (val entity = event.getEntity()) {
                is TreeEntity -> entity.uuid
                else -> throw IllegalStateException()
            }

            val eventsByUid = uidToEventsMap.getOrDefault(nodeUuid, mutableListOf())
            eventsByUid.add(event)

            uidToEventsMap[nodeUuid] = eventsByUid
        }

        return uidToEventsMap
    }
}