package com.tomclaw.cache.demo.domain.usecase

import com.tomclaw.cache.demo.domain.repository.CacheRepository

class ClearCacheUseCase(
    private val repository: CacheRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        repository.clearCache()
    }
}
