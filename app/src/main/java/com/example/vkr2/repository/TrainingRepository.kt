package com.example.vkr2.repository

import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.Relations.TrainingWithExercises
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TrainingRepository {
    suspend fun addTraining(training: TrainingsEntity): Long
    suspend fun updateTraining(training: TrainingsEntity)
    suspend fun deleteTraining(training: TrainingsEntity)

    suspend fun addExerciseToTraining(trainingId: Int, exerciseId: Int)
    suspend fun removeExerciseFromTraining(trainingId: Int, exerciseId: Int)

    suspend fun addSet(set: SetEntity)
    suspend fun updateSet(set: SetEntity)
    suspend fun deleteSet(set: SetEntity)

    fun getAllTrainings(): Flow<List<TrainingsEntity>>
    fun getTrainingsByDate(date: LocalDate): Flow<List<TrainingWithExercises>>
    fun getSetsForExercise(trainingId: Int, exerciseId: Int): Flow<List<SetEntity>>
    fun getTrainingWithExercises(trainingId: Int): Flow<TrainingWithExercises>

}