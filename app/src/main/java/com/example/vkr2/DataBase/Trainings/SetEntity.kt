package com.example.vkr2.DataBase.Trainings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.vkr2.DataBase.Exercises.ExercisesEntity

@Entity(
    tableName = "Sets",
    foreignKeys = [
        ForeignKey(entity = TrainingsEntity::class, parentColumns = ["trainingId"], childColumns = ["trainingId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ExercisesEntity::class, parentColumns = ["ExercisesId"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("trainingId"),Index("exerciseId")]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
    val setId:Int=0,
    val trainingId:Int,
    val exerciseId:Int,
    var reps:Int?,
    var weight:Int?,
    var duration:Int?,
    var exerciseOrder:Int,
    val minutes: Int? = null,
    val seconds: Int? = null,
    val distanceKm: Float? = null
)