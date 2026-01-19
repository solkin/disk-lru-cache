package com.tomclaw.cache.demo

import android.app.Application
import com.tomclaw.cache.DiskLruCache
import com.tomclaw.cache.demo.data.CacheRepositoryImpl
import com.tomclaw.cache.demo.domain.repository.CacheRepository
import com.tomclaw.cache.demo.domain.usecase.AccessFileUseCase
import com.tomclaw.cache.demo.domain.usecase.AddFileUseCase
import com.tomclaw.cache.demo.domain.usecase.ClearCacheUseCase
import com.tomclaw.cache.demo.domain.usecase.DeleteFileUseCase
import com.tomclaw.cache.demo.domain.usecase.ObserveCacheUseCase
import com.tomclaw.cache.demo.presentation.ViewModelFactory
import java.io.File
import java.io.IOException

class App : Application() {

    lateinit var viewModelFactory: ViewModelFactory
        private set

    override fun onCreate() {
        super.onCreate()

        val cacheDir = File(filesDir, "lru_cache")
        val tempDir = cacheDir

        val cache = try {
            DiskLruCache.create(cacheDir, CACHE_SIZE)
        } catch (e: IOException) {
            throw RuntimeException("Failed to create cache", e)
        }

        val repository: CacheRepository = CacheRepositoryImpl(cache, tempDir)

        val observeCacheUseCase = ObserveCacheUseCase(repository)
        val addFileUseCase = AddFileUseCase(repository)
        val accessFileUseCase = AccessFileUseCase(repository)
        val deleteFileUseCase = DeleteFileUseCase(repository)
        val clearCacheUseCase = ClearCacheUseCase(repository)

        viewModelFactory = ViewModelFactory(
            observeCacheUseCase = observeCacheUseCase,
            addFileUseCase = addFileUseCase,
            accessFileUseCase = accessFileUseCase,
            deleteFileUseCase = deleteFileUseCase,
            clearCacheUseCase = clearCacheUseCase
        )
    }

    companion object {
        private const val CACHE_SIZE = 500L * 1024 // 500 KB
    }
}
