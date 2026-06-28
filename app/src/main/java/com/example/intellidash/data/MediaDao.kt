package com.example.intellidash.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: MediaItem): Long

    @Update
    suspend fun update(item: MediaItem)

    @Query("SELECT * FROM media_items WHERE summary IS NULL LIMIT 1")
    suspend fun getNextUnindexedItem(): MediaItem?

    @Query("SELECT EXISTS(SELECT 1 FROM media_items WHERE path = :path)")
    suspend fun exists(path: String): Boolean

    @Query("""
        SELECT media_items.* FROM media_items 
        JOIN media_items_fts ON media_items.id = media_items_fts.rowid 
        WHERE media_items_fts MATCH :query
    """)
    fun search(query: String): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items ORDER BY id DESC")
    fun getAll(): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_items WHERE summary IS NOT NULL")
    fun getIndexedCount(): Flow<Int>
}
