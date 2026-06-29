package com.example.intellidash.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCollectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: UserCollection)

    @Delete
    suspend fun delete(collection: UserCollection)

    @Query("SELECT * FROM user_collections ORDER BY name ASC")
    fun getAllCollections(): Flow<List<UserCollection>>

    @Query("SELECT name FROM user_collections")
    suspend fun getCollectionNames(): List<String>
}
