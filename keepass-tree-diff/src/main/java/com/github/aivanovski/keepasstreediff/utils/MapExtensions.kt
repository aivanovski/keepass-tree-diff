package com.github.aivanovski.keepasstreediff.utils

internal fun <K, V> Map<K, V>.getOrThrow(key: K): V {
    return this[key] ?: throw NoSuchElementException("Key $key not found in map")
}