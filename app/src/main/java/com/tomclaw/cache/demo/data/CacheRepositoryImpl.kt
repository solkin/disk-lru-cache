package com.tomclaw.cache.demo.data

import com.tomclaw.cache.DiskLruCache
import com.tomclaw.cache.demo.domain.model.CacheStats
import com.tomclaw.cache.demo.domain.model.CachedFile
import com.tomclaw.cache.demo.domain.repository.CacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

class CacheRepositoryImpl(
    private val cache: DiskLruCache,
    private val tempDir: File
) : CacheRepository {

    private val _cacheState = MutableStateFlow(buildCacheState())

    override fun observeCache(): Flow<CacheRepository.CacheState> = _cacheState.asStateFlow()

    override suspend fun addFile(key: String, file: File): File = withContext(Dispatchers.IO) {
        val result = cache.put(key, file)
        refreshState()
        result
    }

    override suspend fun getFile(key: String): File? = withContext(Dispatchers.IO) {
        val result = cache.get(key)
        refreshState()
        result
    }

    override suspend fun deleteFile(key: String) = withContext(Dispatchers.IO) {
        cache.delete(key)
        refreshState()
    }

    override suspend fun clearCache() = withContext(Dispatchers.IO) {
        cache.clearCache()
        refreshState()
    }

    override suspend fun createRandomFile(): File = withContext(Dispatchers.IO) {
        val extensions = listOf("jpg", "png", "dat", "bin", "tmp", "cache")
        val extension = extensions.random()
        val file = File.createTempFile("cache_", ".$extension", tempDir)

        DataOutputStream(FileOutputStream(file)).use { stream ->
            val blocks = 2000 + Random.nextInt(6000)
            repeat(blocks) {
                stream.writeLong(Random.nextLong())
            }
            stream.flush()
        }

        file
    }

    private fun refreshState() {
        _cacheState.value = buildCacheState()
    }

    private fun buildCacheState(): CacheRepository.CacheState {
        val stats = CacheStats(
            cacheSize = cache.cacheSize,
            usedSpace = cache.usedSpace,
            freeSpace = cache.freeSpace,
            journalSize = cache.journalSize,
            filesCount = cache.keySet().size
        )

        // Use the new API to get records sorted by LRU order
        val recordsInfo = cache.recordsInfo
        
        val files = recordsInfo.mapIndexed { index, info ->
            val file = File(tempDir, info.fileName)
            val priority = when {
                recordsInfo.size <= 1 -> CachedFile.Priority.HIGH
                index < recordsInfo.size / 3 -> CachedFile.Priority.HIGH
                index < recordsInfo.size * 2 / 3 -> CachedFile.Priority.MEDIUM
                else -> CachedFile.Priority.LOW
            }
            CachedFile(
                key = info.key,
                file = file,
                size = info.size,
                lastAccessed = info.lastAccessed,
                priority = priority
            )
        }

        return CacheRepository.CacheState(
            stats = stats,
            files = files
        )
    }
}
