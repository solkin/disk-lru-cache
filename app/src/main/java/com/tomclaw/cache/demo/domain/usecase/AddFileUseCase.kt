package com.tomclaw.cache.demo.domain.usecase

import com.tomclaw.cache.demo.domain.repository.CacheRepository

class AddFileUseCase(
    private val repository: CacheRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val file = repository.createRandomFile()
        val key = generateKey()
        repository.addFile(key, file)
    }

    private fun generateKey(): String {
        val chars = ('a'..'z') + ('0'..'9')
        return (1..16).map { chars.random() }.joinToString("")
    }
}
