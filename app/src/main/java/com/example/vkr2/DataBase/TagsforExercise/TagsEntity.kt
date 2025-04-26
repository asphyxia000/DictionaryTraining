package com.example.vkr2.DataBase.TagsforExercise

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Tags")
data class TagsEntity (
    @PrimaryKey(autoGenerate = true)
    val TagsId: Int = 0,
    val name: String
)