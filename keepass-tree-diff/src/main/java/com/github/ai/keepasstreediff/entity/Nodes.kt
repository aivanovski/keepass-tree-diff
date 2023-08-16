package com.github.ai.keepasstreediff.entity

sealed interface TreeNode {
    val entity: TreeEntity
    val nodes: List<TreeNode>
}

class MutableNode(
    override val entity: TreeEntity,
    override val nodes: MutableList<MutableNode> = mutableListOf()
) : TreeNode

class MutablePathNode(
    val path: String,
    override val entity: TreeEntity,
    override val nodes: MutableList<MutablePathNode> = mutableListOf()
) : TreeNode