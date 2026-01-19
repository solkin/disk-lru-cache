package com.tomclaw.cache.demo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomclaw.cache.demo.domain.usecase.AccessFileUseCase
import com.tomclaw.cache.demo.domain.usecase.AddFileUseCase
import com.tomclaw.cache.demo.domain.usecase.ClearCacheUseCase
import com.tomclaw.cache.demo.domain.usecase.DeleteFileUseCase
import com.tomclaw.cache.demo.domain.usecase.ObserveCacheUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val observeCacheUseCase: ObserveCacheUseCase,
    private val addFileUseCase: AddFileUseCase,
    private val accessFileUseCase: AccessFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val clearCacheUseCase: ClearCacheUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeCache()
    }

    private fun observeCache() {
        viewModelScope.launch {
            observeCacheUseCase().collect { cacheState ->
                _uiState.update { state ->
                    state.copy(
                        stats = cacheState.stats,
                        files = cacheState.files,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.AddFile -> addFile()
            is MainUiEvent.ClearCache -> clearCache()
            is MainUiEvent.SelectFile -> selectFile(event.file)
            is MainUiEvent.DismissFileDetail -> dismissFileDetail()
            is MainUiEvent.AccessFile -> accessFile(event.key)
            is MainUiEvent.DeleteFile -> deleteFile(event.key)
            is MainUiEvent.DismissError -> dismissError()
        }
    }

    private fun addFile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            addFileUseCase()
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to add file"
                        )
                    }
                }
        }
    }

    private fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            clearCacheUseCase()
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to clear cache"
                        )
                    }
                }
        }
    }

    private fun selectFile(file: com.tomclaw.cache.demo.domain.model.CachedFile) {
        _uiState.update { it.copy(selectedFile = file) }
    }

    private fun dismissFileDetail() {
        _uiState.update { it.copy(selectedFile = null) }
    }

    private fun accessFile(key: String) {
        viewModelScope.launch {
            accessFileUseCase(key)
                .onSuccess {
                    _uiState.update { it.copy(selectedFile = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to access file")
                    }
                }
        }
    }

    private fun deleteFile(key: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedFile = null) }
            deleteFileUseCase(key)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to delete file")
                    }
                }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
