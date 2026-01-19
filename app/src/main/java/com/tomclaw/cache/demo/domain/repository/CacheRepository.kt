package com.tomclaw.cache.demo.domain.repository

import com.tomclaw.cache.demo.domain.model.CacheStats
import com.tomclaw.cache.demo.domain.model.CachedFile
import kotlinx.coroutines.flow.Flow
import java.io.File

interface CacheRepository {

    fun observeCache(): Flow<CacheState>

    suspend fun addFile(key: String, file: File): File

    suspend fun getFile(key: String): File?

    suspend fun deleteFile(key: String)

    suspend fun clearCache()

    suspend fun createRandomFile(): File

    data class CacheState(
        val stats: CacheStats,
        val files: List<CachedFile>
    )
}
