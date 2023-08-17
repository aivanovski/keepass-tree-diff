package com.github.aivanovski.keepasstreediff

import com.github.aivanovski.keepasstreediff.entity.DiffEvent
import com.github.aivanovski.keepasstreediff.entity.Entity
import com.github.aivanovski.keepasstreediff.entity.MutableNode
import com.github.aivanovski.keepasstreediff.entity.TreeNode
import com.github.aivanovski.keepasstreediff.utils.convertToMutableNodeTree
import com.github.aivanovski.keepasstreediff.utils.traverse
import java.util.UUID

class UuidDiffer : BaseDiffer<MutableNode, UUID>() {

    override fun diff(
        lhs: TreeNode,
        rhs: TreeNode
    ): List<DiffEvent<Entity>> {
        val lhsConverted = lhs.convertToMutableNodeTree()
        val rhsConverted = rhs.convertToMutableNodeTree()

        val lhsNodeToParentMap = buildUuidToParentMap(lhs)
        val rhsNodeToParentMap = buildUuidToParentMap(rhs)

        return diff(
            lhsRoots = listOf(lhsConverted),
            rhsRoots = listOf(rhsConverted),
            lhsNodeToParentMap = lhsNodeToParentMap,
            rhsNodeToParentMap = rhsNodeToParentMap,
            visited = HashSet()
        )
            .substituteUpdateEventsWithFieldDiff(
                nodeFactory = { field -> MutableNode(field) }
            )
    }

    override fun diff(
        lhsRoots: List<MutableNode>,
        rhsRoots: List<MutableNode>,
        lhsNodeToParentMap: Map<UUID, UUID>,
        rhsNodeToParentMap: Map<UUID, UUID>,
        visited: MutableSet<UUID>
    ): List<DiffEvent<Entity>> {
        val lhsNodes = lhsRoots.flatMap { node -> node.traverse() }
        val rhsNodes = rhsRoots.flatMap { node -> node.traverse() }

        val lhsNodesMap = lhsNodes.associateBy { node -> node.entity.uuid }
        val rhsNodesMap = rhsNodes.associateBy { node -> node.entity.uuid }

        val uuids = HashSet<UUID>()
            .apply {
                addAll(lhsNodesMap.keys)
                addAll(rhsNodesMap.keys)
            }

        val patchList = mutableListOf<DiffEvent<out Entity>>()

        for (uuid in uuids) {
            val lhs = lhsNodesMap[uuid]
            val rhs = rhsNodesMap[uuid]

            val isLhsVisited = lhs?.entity?.uuid in visited
            val isRhsVisited = rhs?.entity?.uuid in visited

            when {
                lhs != null && rhs != null && !isLhsVisited && !isRhsVisited -> {
                    if (lhs.entity != rhs.entity) {
                        patchList.add(
                            DiffEvent.Update(
                                oldParentUuid = lhsNodeToParentMap[lhs.entity.uuid],
                                newParentUuid = rhsNodeToParentMap[rhs.entity.uuid],
                                oldEntity = lhs.entity,
                                newEntity = rhs.entity
                            )
                        )
                    }

                    patchList.addAll(
                        diff(
                            lhsRoots = lhs.nodes as List<MutableNode>,
                            rhsRoots = rhs.nodes as List<MutableNode>,
                            lhsNodeToParentMap = lhsNodeToParentMap,
                            rhsNodeToParentMap = rhsNodeToParentMap,
                            visited = visited
                        )
                    )
                }

                // item was removed
                lhs != null && rhs == null && !isLhsVisited -> {
                    patchList.add(
                        DiffEvent.Delete(
                            parentUuid = lhsNodeToParentMap[lhs.entity.uuid],
                            entity = lhs.entity
                        )
                    )
                }

                // item was added
                lhs == null && rhs != null && !isRhsVisited -> {
                    patchList.add(
                        DiffEvent.Insert(
                            parentUuid = rhsNodeToParentMap[rhs.entity.uuid],
                            entity = rhs.entity
                        )
                    )
                }
            }

            lhs?.entity?.uuid?.let { lhsUuid -> visited.add(lhsUuid) }
            rhs?.entity?.uuid?.let { rhsUuid -> visited.add(rhsUuid) }
        }

        @Suppress("UNCHECKED_CAST")
        return patchList as List<DiffEvent<Entity>>
    }
}