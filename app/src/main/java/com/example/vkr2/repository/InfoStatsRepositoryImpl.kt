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
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.PeriodSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import java.time.LocalDate


class InfoStatsRepositoryImpl (
    context: Context,
    coroutineScope: CoroutineScope
): InfoStatsRepository {

    private val infoStatsDAO: InfoStatsDAO =
        FitnessDatabase.getInstance(context, coroutineScope)?.InfoStatsDAO()
            ?: throw IllegalArgumentException("Database not initialized")
    private val trainingDAO: TrainingDAO =
        FitnessDatabase.getInstance(context, coroutineScope)?.TrainingDAO()
            ?: throw IllegalArgumentException("Database not initialized")
    private val exercisesDAO: ExercisesDAO =
        FitnessDatabase.getInstance(context, coroutineScope)?.ExpDAO()
            ?: throw IllegalArgumentException("Database not initialized")

    override suspend fun getInfo(exerciseId: Int) = infoStatsDAO.getInfo(exerciseId)
    override suspend fun insertInfo(info: ExerciseInfo) = infoStatsDAO.insertInfo(info)
    override suspend fun getStats(exerciseId: Int) = infoStatsDAO.getStats(exerciseId)
    override suspend fun getExerciseImage(exerciseId: Int) = infoStatsDAO.getExerciseImage(exerciseId)
    override suspend fun getTrainingbyId(training: Int) = trainingDAO.getTrainingbyId(training)
    override suspend fun getAllTrainings() = trainingDAO.getAllTrainings()
    override suspend fun getExerciseById(id: Int) = exercisesDAO.getExerciseById(id)

    override suspend fun getSetsForExercises(exerciseId: Int): Flow<List<SetEntity>> {
        return trainingDAO.getSetsForExercises(exerciseId)
    }

    fun getSetsForExercise(exerciseId: Int, trainingId: Int): Flow<List<SetEntity>> {
        return trainingDAO.getSetsforExercise(exerciseId, trainingId)
    }

    override suspend fun insertOrUpdateStats(stats: ExerciseStats) {
        val existing = infoStatsDAO.getStats(stats.exerciseId)
        if (existing == null) {
            infoStatsDAO.insertStats(stats)
        } else {
            infoStatsDAO.updateStats(stats.copy(statid = existing.statid))
        }
    }

    override suspend fun recalculate(exerciseId: Int) {
        val stats = calculateStatsForPeriod(exerciseId, PeriodSelection.alltime)
        insertOrUpdateStats(stats)
    }

    override suspend fun calculateStatsForPeriod(exerciseId: Int, periodSelection: PeriodSelection): ExerciseStats {
        val allSets = getSetsForExercises(exerciseId).first()
        val allTrainings = getAllTrainings().first()

        val nowe = LocalDate.now()
        val validTrainingIds = when (periodSelection) {
            is PeriodSelection.last30day -> allTrainings.filter {
                it.date.month == nowe.month && it.date.year == nowe.year
            }.map { it.trainingId }

            is PeriodSelection.last3month -> {
                val months = (0..2).map { nowe.minusMonths(it.toLong()) }.map { it.month to it.year }
                allTrainings.filter { training ->
                    months.any { (month, year) ->
                        training.date.month == month && training.date.year == year
                    }
                }.map { it.trainingId }
            }

            is PeriodSelection.lass6month -> {
                val months = (0..5).map { nowe.minusMonths(it.toLong()) }.map { it.month to it.year }
                allTrainings.filter { training ->
                    months.any { (month, year) ->
                        training.date.month == month && training.date.year == year
                    }
                }.map { it.trainingId }
            }

            is PeriodSelection.lastyear -> {
                val months = (0..11).map { nowe.minusMonths(it.toLong()) }.map { it.month to it.year }
                allTrainings.filter { training ->
                    months.any { (month, year) ->
                        training.date.month == month && training.date.year == year
                    }
                }.map { it.trainingId }
            }

            is PeriodSelection.alltime -> allTrainings.map { it.trainingId }

            is PeriodSelection.SpecificMonth -> allTrainings.filter {
                it.date.year == periodSelection.year && it.date.monthValue == periodSelection.month
            }.map { it.trainingId }
        }


        val sets = allSets.filter { it.trainingId in validTrainingIds }

        if (sets.isEmpty()) {
            return ExerciseStats(
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
        }

        val now = LocalDateTime.now()
        val maxVolumeSet = sets.maxByOrNull { (it.reps ?: 0) * (it.weight ?: 0) }
        val oneRepMaxSet = sets.maxByOrNull {
            val reps = it.reps ?: 0
            val weight = it.weight ?: 0
            weight * (1 + reps / 30.0)
        }

        val oneRepMax = oneRepMaxSet?.let {
            val reps = it.reps ?: 0
            val weight = it.weight ?: 0
            weight * (1 + reps / 30.0)
        } ?: 0.0

        return ExerciseStats(
            exerciseId = exerciseId,
            totalSets = sets.size,
            totalResp = sets.sumOf { it.reps ?: 0 },
            totalWeight = sets.sumOf { (it.reps ?: 0) * (it.weight ?: 0) },
            totalTrainings = sets.map { it.trainingId }.distinct().size,
            maxWeight = sets.maxOf { it.weight ?: 0 },
            maxResp = sets.maxOf { it.reps ?: 0 },
            maxWorkoutVolume = sets.maxOf { (it.reps ?: 0) * (it.weight ?: 0) },
            maxWorkoutVolumeDate = maxVolumeSet?.let { now } ?: LocalDateTime.MIN,
            estimatedOneRepMax = oneRepMax,
            oneRepMaxDate = oneRepMaxSet?.let { now } ?: LocalDateTime.MIN
        )
    }
}
