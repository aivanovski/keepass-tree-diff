package com.github.ai.keepasstreediff

import com.github.ai.keepasstreediff.entity.DiffEvent
import com.github.ai.keepasstreediff.entity.Entity
import com.github.ai.keepasstreediff.entity.TreeNode

interface Differ {

    fun diff(
        lhs: TreeNode,
        rhs: TreeNode,
    ): List<DiffEvent<Entity>>
}