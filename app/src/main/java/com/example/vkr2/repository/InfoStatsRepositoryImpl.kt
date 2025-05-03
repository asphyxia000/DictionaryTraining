package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseInfo
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseStats
import com.example.vkr2.DataBase.Exercises.DetailExercise.InfoStatsDAO
import com.example.vkr2.DataBase.Exercises.ExercisesDAO
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingDAO
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first


class InfoStatsRepositoryImpl (
    context: Context,
    coroutineScope: CoroutineScope
):InfoStatsRepository{

    private val infoStatsDAO: InfoStatsDAO =
        FitnessDatabase.getInstance(context,coroutineScope)?.InfoStatsDAO()
            ?: throw IllegalArgumentException("Database not initialized")
    private val trainingDAO: TrainingDAO=
        FitnessDatabase.getInstance(context,coroutineScope)?.TrainingDAO()
            ?:throw IllegalArgumentException("Database not initialized")
    private val exercisesDAO:ExercisesDAO =
        FitnessDatabase.getInstance(context,coroutineScope)?.ExpDAO()
            ?:throw IllegalArgumentException("Database not initialized")

    override suspend fun getInfo(exerciseId:Int): ExerciseInfo?{
        return infoStatsDAO.getInfo(exerciseId)
    }

    override suspend fun insertInfo(info: ExerciseInfo){
        infoStatsDAO.insertInfo(info)
    }

    override suspend fun getStats(exerciseId: Int):ExerciseStats?{
        return infoStatsDAO.getStats(exerciseId)
    }
    override suspend fun insertOrUpdateStats(stats: ExerciseStats){
        val existing = infoStatsDAO.getStats(stats.exerciseId)
        if (existing == null){
            infoStatsDAO.insertStats(stats)
        }
        else{
            infoStatsDAO.updateStats(stats.copy(statid = existing.statid))
        }
    }

    override suspend fun getExerciseImage(exerciseId: Int): String? {
        return infoStatsDAO.getExerciseImage(exerciseId)
    }

    override suspend fun getSetsForExercises(exerciseId: Int): Flow<List<SetEntity>> {
        return trainingDAO.getSetsForExercises(exerciseId)
    }
    fun getSetsForExercise(exerciseId: Int,trainingId: Int): Flow<List<SetEntity>> {
        return trainingDAO.getSetsforExercise(exerciseId,trainingId)
    }

    override suspend fun getTrainingbyId(training: Int): TrainingsEntity? {
        return trainingDAO.getTrainingbyId(training)
    }
    override suspend fun getExerciseById(id:Int): ExercisesEntity?{
        return exercisesDAO.getExerciseById(id)
    }

    override suspend fun recalculate(exerciseId: Int) {
        val sets = getSetsForExercises(exerciseId).first()
        val previous = getStats(exerciseId)
        val epsilon = 0.0001

        if (sets.isNotEmpty()) {
            val totalSets = sets.size
            val totalResp = sets.sumOf { it.reps ?: 0 }
            val totalWeight = sets.sumOf { (it.reps ?: 0) * (it.weight ?: 0) }
            val maxWeight = sets.maxOf { it.weight ?: 0 }
            val maxResp = sets.maxOf { it.reps ?: 0 }
            val maxVolume = sets.maxOf { (it.reps ?: 0) * (it.weight ?: 0) }

            val trainingCount = sets.map { it.trainingId }.distinct().size

            // Новая логика: считаем, что дата рекорда — это сейчас
            val now = LocalDateTime.now()

            val maxVolumeSet = sets.maxByOrNull { (it.reps ?: 0) * (it.weight ?: 0) }
            val newMaxVolumeDate = if (
                previous == null || maxVolume > previous.maxWorkoutVolume
            ) now else previous.maxWorkoutVolumeDate

            val oneRepMaxSet = sets.maxByOrNull {
                val reps = it.reps ?: 0
                val weight = it.weight ?: 0
                weight * (1 + reps / 30.0)
            }
            val newOneRepMax = oneRepMaxSet?.let {
                val reps = it.reps ?: 0
                val weight = it.weight ?: 0
                weight * (1 + reps / 30.0)
            } ?: 0.0
            val newOneRepMaxDate = if (
                previous == null || kotlin.math.abs(newOneRepMax - previous.estimatedOneRepMax) > epsilon
            ) now else previous.oneRepMaxDate

            val updatedStats = ExerciseStats(
                exerciseId = exerciseId,
                totalSets = totalSets,
                totalResp = totalResp,
                totalWeight = totalWeight,
                totalTrainings = trainingCount,
                maxWeight = maxWeight,
                maxResp = maxResp,
                maxWorkoutVolume = maxVolume,
                maxWorkoutVolumeDate = newMaxVolumeDate,
                estimatedOneRepMax = newOneRepMax,
                oneRepMaxDate = newOneRepMaxDate
            )

            insertOrUpdateStats(updatedStats)
        } else {
            insertOrUpdateStats(
                ExerciseStats(
                    exerciseId = exerciseId,
                    totalSets = 0,
                    totalResp = 0,
                    totalWeight = 0,
                    totalTrainings = 0,
                    maxWeight = 0,
                    maxResp = 0,
                    maxWorkoutVolume = 0,
                    maxWorkoutVolumeDate = LocalDateTime.MIN,
                    estimatedOneRepMax = 0.0,
                    oneRepMaxDate = LocalDateTime.MIN
                )
            )
        }
    }




}