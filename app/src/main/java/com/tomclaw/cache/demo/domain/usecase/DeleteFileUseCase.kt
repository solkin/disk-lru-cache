package com.tomclaw.cache.demo.domain.usecase

import com.tomclaw.cache.demo.domain.repository.CacheRepository

class DeleteFileUseCase(
    private val repository: CacheRepository
) {
    suspend operator fun invoke(key: String): Result<Unit> = runCatching {
        repository.deleteFile(key)
    }
}
