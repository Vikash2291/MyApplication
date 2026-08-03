package com.example.myapplication.data.local.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochMilliseconds(it, TimeZone.currentSystemDefault()) }
    }

    @TypeConverter
    fun toTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochMilliseconds(TimeZone.currentSystemDefault())
    }

    @TypeConverter
    fun fromJson(json: String?): Any? {
        return json?.let { Json.decodeFromString(Any::class, it) }
    }

    @TypeConverter
    fun toJson(obj: Any?): String? {
        return obj?.let { Json.encodeToString(it) }
    }
}