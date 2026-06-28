package com.example.intellidash.data

import androidx.room.*

@Entity(tableName = "media_items", indices = [Index(value = ["path"], unique = true)])
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val path: String,
    val summary: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val indexedAt: Long = 0
)

@Fts4(contentEntity = MediaItem::class)
@Entity(tableName = "media_items_fts")
data class MediaItemFts(
    val summary: String?,
    val category: String?,
    val tags: String // Flattened tags for searching
)
