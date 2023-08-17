package com.github.aivanovski.keepasstreediff

import com.github.aivanovski.keepasstreediff.entity.DiffEvent
import com.github.aivanovski.keepasstreediff.entity.Entity
import com.github.aivanovski.keepasstreediff.entity.TreeNode

interface Differ {

    fun diff(
        lhs: TreeNode,
        rhs: TreeNode
    ): List<DiffEvent<Entity>>
}