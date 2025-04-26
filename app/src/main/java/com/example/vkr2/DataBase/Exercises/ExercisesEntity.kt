package com.example.vkr2.DataBase.Exercises

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Exercises",
    foreignKeys = [
        ForeignKey(
            entity = com.example.vkr2.DataBase.MuscleGroup.MuscleGroupEntity::class,
            parentColumns = ["MuscleGroupsID"],
            childColumns = ["muscleGroupID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["muscleGroupID"])]

    )
data class ExercisesEntity(
    @PrimaryKey(autoGenerate = true)
    val ExercisesId:Int=0,

    val ExercisesName: String,

    val muscleGroupID:Int,

    val imagePath: String
)