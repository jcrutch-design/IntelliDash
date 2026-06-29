package com.example.intellidash.ui.media

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.intellidash.data.CollectionInfo
import com.example.intellidash.data.MediaDao
import com.example.intellidash.data.MediaItem
import com.example.intellidash.data.UserCollection
import com.example.intellidash.data.UserCollectionDao
import com.example.intellidash.utils.RootStorageHelper
import com.example.intellidash.worker.DiscoveryWorker
import com.example.intellidash.worker.MediaIndexerWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaIndexerViewModel(
    private val application: Application,
    private val mediaDao: MediaDao,
    private val userCollectionDao: UserCollectionDao,
    private val workManager: WorkManager
) : ViewModel() {

    init {
        refreshDiscovery()
        startIndexing()
    }

    private val searchQuery = MutableStateFlow("")
    private val _selectedCollection = MutableStateFlow<String?>(null)
    val selectedCollection: StateFlow<String?> = _selectedCollection.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaItems: StateFlow<List<MediaItem>> = combine(searchQuery, _selectedCollection) { query, collection ->
        query to collection
    }
        .flatMapLatest { (query, collection) ->
            when {
                query.isNotBlank() -> mediaDao.search(query)
                collection != null -> mediaDao.getItemsByCollection(collection)
                else -> mediaDao.getAll()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val collections: StateFlow<List<CollectionInfo>> = mediaDao.getCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCollections: StateFlow<List<UserCollection>> = userCollectionDao.getAllCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCount: StateFlow<Int> = mediaDao.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val indexedCount: StateFlow<Int> = mediaDao.getIndexedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val workInfo: StateFlow<WorkInfo?> = combine(
        workManager.getWorkInfosForUniqueWorkFlow("MediaIndexerWorker"),
        workManager.getWorkInfosForUniqueWorkFlow("MediaIndexerWorker_OneTime"),
        workManager.getWorkInfosForUniqueWorkFlow("DiscoveryWorker")
    ) { periodic, oneTime, discovery ->
        // Priority: Discovery -> OneTime Indexing -> Periodic Indexing
        discovery.firstOrNull() ?: oneTime.firstOrNull() ?: periodic.firstOrNull()
    }.onEach { info ->
        if (info?.state == WorkInfo.State.FAILED) {
            _statusMessage.value = info.outputData.getString("error") ?: "Operation failed"
        } else if (info?.state == WorkInfo.State.RUNNING) {
            if (info.tags.contains("DiscoveryWorker")) {
                _statusMessage.value = "Scanning for new media..."
            } else {
                _statusMessage.value = "Scanning & Indexing..."
            }
        } else if (info?.state == WorkInfo.State.SUCCEEDED) {
            _statusMessage.value = null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        if (query.isNotBlank()) _selectedCollection.value = null
    }

    fun selectCollection(collectionName: String?) {
        _selectedCollection.value = collectionName
        if (collectionName != null) searchQuery.value = ""
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

    fun createCollection(name: String) {
        viewModelScope.launch {
            userCollectionDao.insert(UserCollection(name = name, isUserDefined = true))
            _statusMessage.value = "Collection '$name' created"
            // Re-trigger indexing to let AI "see" the new collection
            startIndexing()
        }
    }

    fun addMediaToCollection(mediaId: Long, collection: String) {
        viewModelScope.launch {
            mediaDao.updateCollection(mediaId, collection)
            _statusMessage.value = "Added to $collection"
        }
    }

    fun importMedia(uri: Uri, collection: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val inputStream = application.contentResolver.openInputStream(uri) ?: return@withContext
                    val bytes = inputStream.readBytes()
                    val fileName = "imported_${System.currentTimeMillis()}.jpg"
                    val tmpFile = java.io.File(application.cacheDir, fileName)
                    tmpFile.writeBytes(bytes)

                    val destinationDir = "/sdcard/Pictures/IntelliDash"
                    val destinationPath = "$destinationDir/$fileName"

                    Runtime.getRuntime().exec(arrayOf("su", "-c", "mkdir -p $destinationDir")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "cp ${tmpFile.absolutePath} $destinationPath")).waitFor()
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "rm ${tmpFile.absolutePath}")).waitFor()

                    mediaDao.insert(MediaItem(path = destinationPath, collectionName = collection))
                }
                startIndexing()
                _statusMessage.value = "Imported to $collection"
            } catch (e: Exception) {
                _statusMessage.value = "Import failed: ${e.message}"
            }
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
        private val userCollectionDao: UserCollectionDao,
        private val workManager: WorkManager
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MediaIndexerViewModel(application, mediaDao, userCollectionDao, workManager) as T
        }
    }
}
