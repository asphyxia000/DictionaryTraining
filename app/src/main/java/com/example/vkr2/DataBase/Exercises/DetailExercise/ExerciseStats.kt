package com.example.vkr2.DataBase.Exercises.DetailExercise

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import java.time.LocalDateTime

@Entity(tableName = "ExercisesStats",
    foreignKeys = [ForeignKey(
        entity = ExercisesEntity::class,
        parentColumns = ["ExercisesId"],
        childColumns = ["exerciseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("exerciseId")]
    )
data class ExerciseStats(
    @PrimaryKey(autoGenerate = true)
    val statid: Int = 0,
    val exerciseId: Int = 0,

    val totalSets: Int = 0,
    val totalResp: Int = 0,
    val totalWeight: Int = 0,
    val totalTrainings: Int = 0,

    val maxWeight: Int = 0,
    val maxResp: Int = 0,
    val maxWorkoutVolume: Int = 0,
    val maxWorkoutVolumeDate: LocalDateTime,

    val estimatedOneRepMax: Double = 0.0,
    val oneRepMaxDate: LocalDateTime
)
