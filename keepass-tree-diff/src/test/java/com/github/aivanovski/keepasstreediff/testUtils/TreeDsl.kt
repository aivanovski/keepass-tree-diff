package com.github.aivanovski.keepasstreediff.testUtils

import com.github.aivanovski.keepasstreediff.entity.EntryEntity
import com.github.aivanovski.keepasstreediff.entity.GroupEntity
import com.github.aivanovski.keepasstreediff.entity.MutableNode
import com.github.aivanovski.keepasstreediff.entity.TreeEntity

internal object TreeDsl {

    fun tree(
        root: GroupEntity,
        content: (TreeBuilder.() -> Unit)? = null
    ): MutableNode {
        val node = TreeBuilder(root)
            .apply {
                content?.invoke(this)
            }
            .build()

        return node
    }

    class TreeBuilder(
        private val root: TreeEntity
    ) {

        private val nodes = mutableListOf<MutableNode>()

        fun group(
            group: GroupEntity,
            content: (TreeBuilder.() -> Unit)? = null
        ) {
            add(group, content)
        }

        fun entry(entry: EntryEntity) {
            add(entry)
        }

        fun build(): MutableNode {
            return MutableNode(root, nodes)
        }

        private fun add(
            value: TreeEntity,
            content: (TreeBuilder.() -> Unit)? = null
        ) {
            val node = TreeBuilder(value)
                .apply {
                    content?.invoke(this)
                }
                .build()

            nodes.add(node)
        }
    }
}