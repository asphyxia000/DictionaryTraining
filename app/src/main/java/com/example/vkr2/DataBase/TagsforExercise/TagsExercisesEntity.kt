package com.example.vkr2.DataBase.TagsforExercise

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TagsExercises",
    primaryKeys = ["exerciseId", "tagsId"],
    foreignKeys = [
        ForeignKey(
            entity = com.example.vkr2.DataBase.Exercises.ExercisesEntity::class,
            parentColumns = ["ExercisesId"],
            childColumns = ["exerciseId"], // ✅ правильно!
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = com.example.vkr2.DataBase.TagsforExercise.TagsEntity::class,
            parentColumns = ["TagsId"],
            childColumns = ["tagsId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId"), Index("tagsId")] // ✅ исправлено
)
data class TagsExercisesEntity(
    val exerciseId: Int,
    val tagsId: Int
)

