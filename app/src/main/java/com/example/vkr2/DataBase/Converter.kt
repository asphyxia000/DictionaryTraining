package com.example.vkr2.DataBase

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

class Converter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String {
        return date?.toString() ?: ""
    }
    @TypeConverter
    fun toLocalDate(value:String):LocalDate = LocalDate.parse(value)
    @TypeConverter
    fun fromTimeStamp(value: String?):LocalDateTime?{
        return value?.let { LocalDateTime.parse(it) }
    }
    @TypeConverter
    fun dateToTimeStamp(date: LocalDateTime?): String? {
        return date?.toString() ?: ""
    }
}