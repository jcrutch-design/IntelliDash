package com.example.intellidash.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_collections")
data class UserCollection(
    @PrimaryKey val name: String,
    val isUserDefined: Boolean = true
)
