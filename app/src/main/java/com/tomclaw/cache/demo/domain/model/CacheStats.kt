package com.tomclaw.cache.demo.domain.model

data class CacheStats(
    val cacheSize: Long,
    val usedSpace: Long,
    val freeSpace: Long,
    val journalSize: Long,
    val filesCount: Int
) {
    val usagePercent: Float
        get() = if (cacheSize > 0) usedSpace.toFloat() / cacheSize else 0f

    val isHealthy: Boolean
        get() = usagePercent < 0.7f

    val isWarning: Boolean
        get() = usagePercent in 0.7f..0.9f

    val isCritical: Boolean
        get() = usagePercent > 0.9f
}
