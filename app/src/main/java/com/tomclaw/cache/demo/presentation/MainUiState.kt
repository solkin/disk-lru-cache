package com.tomclaw.cache.demo.presentation

import com.tomclaw.cache.demo.domain.model.CacheStats
import com.tomclaw.cache.demo.domain.model.CachedFile

data class MainUiState(
    val stats: CacheStats = CacheStats(
        cacheSize = 0,
        usedSpace = 0,
        freeSpace = 0,
        journalSize = 0,
        filesCount = 0
    ),
    val files: List<CachedFile> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFile: CachedFile? = null,
    val error: String? = null
)

sealed interface MainUiEvent {
    data object AddFile : MainUiEvent
    data object ClearCache : MainUiEvent
    data class SelectFile(val file: CachedFile) : MainUiEvent
    data object DismissFileDetail : MainUiEvent
    data class AccessFile(val key: String) : MainUiEvent
    data class DeleteFile(val key: String) : MainUiEvent
    data object DismissError : MainUiEvent
}
