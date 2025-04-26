package com.example.vkr2.DataBase.Trainings

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.vkr2.DataBase.Exercises.ExercisesEntity

@Entity(
    tableName = "TrainingExercises",
    primaryKeys = ["trainingId","exerciseId"],
    foreignKeys = [
        ForeignKey(entity = TrainingsEntity::class, parentColumns = ["trainingId"], childColumns = ["trainingId"],onDelete=ForeignKey.CASCADE),
        ForeignKey(entity = ExercisesEntity::class, parentColumns = ["ExercisesId"], childColumns = ["exerciseId"],onDelete=ForeignKey.CASCADE)
    ],
    indices = [Index("trainingId"),Index("exerciseId")]
)
data class TrainingExerciseCrossRef (
    val trainingId:Int,
    val exerciseId: Int
)