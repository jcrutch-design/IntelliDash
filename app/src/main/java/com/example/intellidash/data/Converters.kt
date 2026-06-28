package com.example.intellidash.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split("|").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString("|")
    }
}
