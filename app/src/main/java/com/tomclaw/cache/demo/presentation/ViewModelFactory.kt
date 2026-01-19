package com.tomclaw.cache.demo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tomclaw.cache.demo.domain.usecase.AccessFileUseCase
import com.tomclaw.cache.demo.domain.usecase.AddFileUseCase
import com.tomclaw.cache.demo.domain.usecase.ClearCacheUseCase
import com.tomclaw.cache.demo.domain.usecase.DeleteFileUseCase
import com.tomclaw.cache.demo.domain.usecase.ObserveCacheUseCase

class ViewModelFactory(
    private val observeCacheUseCase: ObserveCacheUseCase,
    private val addFileUseCase: AddFileUseCase,
    private val accessFileUseCase: AccessFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase,
    private val clearCacheUseCase: ClearCacheUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                observeCacheUseCase = observeCacheUseCase,
                addFileUseCase = addFileUseCase,
                accessFileUseCase = accessFileUseCase,
                deleteFileUseCase = deleteFileUseCase,
                clearCacheUseCase = clearCacheUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
