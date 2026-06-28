package com.example.intellidash.ui.media

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.intellidash.data.MediaDao
import com.example.intellidash.data.MediaItem
import com.example.intellidash.utils.RootStorageHelper
import com.example.intellidash.worker.DiscoveryWorker
import com.example.intellidash.worker.MediaIndexerWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MediaIndexerViewModel(
    private val application: Application,
    private val mediaDao: MediaDao,
    private val workManager: WorkManager
) : ViewModel() {

    init {
        refreshDiscovery()
        startIndexing()
    }

    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaItems: StateFlow<List<MediaItem>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                mediaDao.getAll()
            } else {
                mediaDao.search(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCount: StateFlow<Int> = mediaDao.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val indexedCount: StateFlow<Int> = mediaDao.getIndexedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val workInfo: StateFlow<WorkInfo?> = workManager.getWorkInfosForUniqueWorkFlow("MediaIndexerWorker")
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun refreshDiscovery() {
        DiscoveryWorker.enqueue(application)
    }

    fun startIndexing() {
        MediaIndexerWorker.enqueueOneTimeWork(application)
    }

    fun toggleIndexing(enabled: Boolean) {
        if (enabled) {
            MediaIndexerWorker.enqueuePeriodicWork(application)
        } else {
            workManager.cancelUniqueWork("MediaIndexerWorker")
        }
    }

    /**
     * Loads a bitmap for a given path using root access.
     * In a real app, this should be cached or handled by a custom Coil fetcher.
     */
    fun loadBitmap(path: String): Bitmap? {
        return RootStorageHelper.readMediaFile(path)
    }

    class Factory(
        private val application: Application,
        private val mediaDao: MediaDao,
        private val workManager: WorkManager
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MediaIndexerViewModel(application, mediaDao, workManager) as T
        }
    }
}
