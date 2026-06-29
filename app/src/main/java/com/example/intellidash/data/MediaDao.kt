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
        JOIN media_items_fts ON media_items.rowid = media_items_fts.rowid 
        WHERE media_items_fts MATCH :query
    """)
    fun search(query: String): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items ORDER BY rowid DESC")
    fun getAll(): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM media_items")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_items WHERE summary IS NOT NULL")
    fun getIndexedCount(): Flow<Int>

    @Query("""
        SELECT uc.name as name, COUNT(mi.rowid) as count, (SELECT path FROM media_items WHERE collectionName = uc.name LIMIT 1) as coverPath 
        FROM user_collections uc 
        LEFT JOIN media_items mi ON uc.name = mi.collectionName 
        GROUP BY uc.name
        ORDER BY uc.name ASC
    """)
    fun getCollections(): Flow<List<CollectionInfo>>

    @Query("SELECT * FROM media_items WHERE collectionName = :collectionName ORDER BY rowid DESC")
    fun getItemsByCollection(collectionName: String): Flow<List<MediaItem>>

    @Query("UPDATE media_items SET collectionName = :collectionName WHERE rowid = :id")
    suspend fun updateCollection(id: Long, collectionName: String)

    @Query("SELECT * FROM media_items WHERE rowid = :id")
    suspend fun getById(id: Long): MediaItem?
}
