package com.tomclaw.cache.demo.domain.usecase

import com.tomclaw.cache.demo.domain.repository.CacheRepository
import java.io.File

class AccessFileUseCase(
    private val repository: CacheRepository
) {
    suspend operator fun invoke(key: String): Result<File?> = runCatching {
        repository.getFile(key)
    }
}
