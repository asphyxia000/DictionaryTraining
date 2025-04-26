package com.example.vkr2.DataBase.Exercises.DetailExercise

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.vkr2.DataBase.Exercises.ExercisesEntity


@Entity(tableName = "ExerciseInfo",
    foreignKeys = [ForeignKey(
        entity = ExercisesEntity::class,
        parentColumns = ["ExercisesId"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("exerciseId")]
    )
data class ExerciseInfo (
    @PrimaryKey(autoGenerate = true)
    val infoid:Int=0,
    val exerciseId:Int,
    val description:String,
    val executionTips:String
)