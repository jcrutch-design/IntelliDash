package com.example.intellidash.data

import androidx.room.*

@Entity(tableName = "media_items", indices = [Index(value = ["path"], unique = true)])
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val id: Long = 0,
    val path: String,
    val summary: String? = null,
    val category: String? = null,
    val collectionName: String? = null,
    val tags: List<String> = emptyList(),
    val indexedAt: Long = 0
)

@Fts4(contentEntity = MediaItem::class)
@Entity(tableName = "media_items_fts")
data class MediaItemFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowid: Long,
    val path: String,
    val summary: String?,
    val category: String?,
    val collectionName: String?,
    val tags: List<String>
)

data class CollectionInfo(
    val name: String,
    val count: Int,
    val coverPath: String?
)
