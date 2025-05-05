package com.example.vkr2.repository

import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseInfo
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseStats
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.PeriodSelection
import kotlinx.coroutines.flow.Flow

interface InfoStatsRepository {
    suspend fun getInfo(exerciseId:Int): ExerciseInfo?
    suspend fun insertInfo(info: ExerciseInfo)

    suspend fun getStats(exerciseId: Int):ExerciseStats?
    suspend fun insertOrUpdateStats(stats: ExerciseStats)

    suspend fun getExerciseImage(exerciseId: Int):String?
    suspend fun getSetsForExercises(exerciseId: Int):Flow<List<SetEntity>>

    suspend fun getTrainingbyId(training:Int): TrainingsEntity?

    suspend fun getExerciseById(id: Int): ExercisesEntity?

    suspend fun recalculate(exerciseId: Int)
    suspend fun calculateStatsForPeriod(exerciseId: Int, periodSelection: PeriodSelection):ExerciseStats?
    suspend fun getAllTrainings():Flow<List<TrainingsEntity>>

}