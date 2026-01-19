package com.tomclaw.cache.demo.domain.model

import java.io.File

data class CachedFile(
    val key: String,
    val file: File,
    val size: Long,
    val lastAccessed: Long,
    val priority: Priority
) {
    enum class Priority {
        HIGH,
        MEDIUM,
        LOW
    }
}
