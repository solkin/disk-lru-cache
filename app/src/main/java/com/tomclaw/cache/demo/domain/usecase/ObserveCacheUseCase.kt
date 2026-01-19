package com.tomclaw.cache.demo.domain.usecase

import com.tomclaw.cache.demo.domain.repository.CacheRepository
import kotlinx.coroutines.flow.Flow

class ObserveCacheUseCase(
    private val repository: CacheRepository
) {
    operator fun invoke(): Flow<CacheRepository.CacheState> {
        return repository.observeCache()
    }
}
