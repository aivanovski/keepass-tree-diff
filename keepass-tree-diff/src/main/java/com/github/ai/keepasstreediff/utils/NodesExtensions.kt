package com.github.ai.keepasstreediff.utils

import com.github.ai.keepasstreediff.entity.EntryEntity
import com.github.ai.keepasstreediff.entity.GroupEntity
import com.github.ai.keepasstreediff.entity.MutableNode
import com.github.ai.keepasstreediff.entity.MutablePathNode
import com.github.ai.keepasstreediff.entity.TreeNode
import java.util.LinkedList

internal fun TreeNode.traverse(): List<TreeNode> {
    val nodes = LinkedList<TreeNode>()
    nodes.add(this)

    val result = mutableListOf<TreeNode>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val node = nodes.removeFirst()

            result.add(node)

            for (child in node.nodes) {
                nodes.add(child)
            }
        }
    }

    return result
}

internal fun MutablePathNode.traversePathNode(): List<MutablePathNode> {
    val nodes = LinkedList<MutablePathNode>()
    nodes.add(this)

    val result = mutableListOf<MutablePathNode>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val node = nodes.removeFirst()

            result.add(node)

            for (child in node.nodes) {
                nodes.add(child)
            }
        }
    }

    return result
}

internal fun TreeNode.traverseWithParents(): List<Pair<TreeNode?, TreeNode>> {
    val nodes = LinkedList<Pair<TreeNode?, TreeNode>>()
    nodes.add(Pair(null, this))

    val result = mutableListOf<Pair<TreeNode?, TreeNode>>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val (parent, node) = nodes.removeFirst()
            result.add(Pair(parent, node))

            for (childNode in node.nodes) {
                nodes.add(Pair(node, childNode))
            }
        }
    }

    return result
}

internal fun TreeNode.convertToMutablePathNodeTree(): MutablePathNode {
    val nodes = LinkedList<Pair<MutablePathNode?, TreeNode>>()
    nodes.add(Pair(null, this))

    var root: MutablePathNode? = null
    while (nodes.isNotEmpty()) {
        val (parent, node) = nodes.removeFirst()

        val path = if (parent != null) {
            parent.path + "/" + node.entity.name
        } else {
            node.entity.name
        }

        val pathNode = when (node.entity) {
            is GroupEntity -> {
                MutablePathNode(
                    path = path,
                    entity = node.entity
                )
            }

            is EntryEntity -> {
                MutablePathNode(
                    path = path,
                    entity = node.entity
                )
            }

            else -> throw IllegalStateException()
        }

        parent?.nodes?.add(pathNode)
        if (root == null) {
            root = pathNode
        }

        for (nextNode in node.nodes) {
            nodes.add(Pair(pathNode, nextNode))
        }
    }

    return root ?: throw IllegalStateException()
}

internal fun TreeNode.convertToMutableNodeTree(): MutableNode {
    val nodes = LinkedList<Pair<MutableNode?, TreeNode>>()
    nodes.add(Pair(null, this))

    var root: MutableNode? = null
    while (nodes.isNotEmpty()) {
        val (parent, node) = nodes.removeFirst()

        val newNode = when (node.entity) {
            is GroupEntity -> {
                MutableNode(entity = node.entity)
            }

            is EntryEntity -> {
                MutableNode(entity = node.entity)
            }

            else -> throw IllegalStateException()
        }

        parent?.nodes?.add(newNode)
        if (root == null) {
            root = newNode
        }

        for (nextNode in node.nodes) {
            nodes.add(Pair(newNode, nextNode))
        }
    }

    return root ?: throw IllegalStateException()
}