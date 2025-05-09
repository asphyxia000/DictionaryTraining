package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.Exercises.DetailExercise.InfoStatsDAO
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.MeasurementsAndStats.GeneralTrainingStats.GeneralTrainingStatsDAO
import com.example.vkr2.DataBase.MeasurementsAndStats.GeneralTrainingStats.GeneralTrainingStatsEntity
import com.example.vkr2.DataBase.Trainings.TrainingDAO
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.PeriodSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class GeneralTrainingStatsRepositoryImpl(
    context: Context,
    coroutineScope: CoroutineScope
):GeneralTrainingStatsRepository {

    private val trainingDao: TrainingDAO =
        FitnessDatabase.getInstance(context,coroutineScope)?.TrainingDAO()
            ?: throw IllegalArgumentException("Database not initialized")

    private val generalTrainingStatsDAO: GeneralTrainingStatsDAO=
        FitnessDatabase.getInstance(context,coroutineScope)?.GeneralStatsDAO()
            ?:throw IllegalArgumentException("Database not initialized")

    override suspend fun geAllStats(): Flow<List<GeneralTrainingStatsEntity>> {
        return generalTrainingStatsDAO.geAllStats()
    }

    override suspend fun recalculateGeneralStats(period: PeriodSelection) {
        val stats = calculateStatsForPeriod(period)
        insertOrUpdate(stats)
    }

    override suspend fun insertOrUpdate(stats: GeneralTrainingStatsEntity) {
        val existing = generalTrainingStatsDAO.geAllStats().first()
            .find { it.date == stats.date }
        if (existing != null) {
            generalTrainingStatsDAO.update(stats.copy(id = existing.id))
        } else {
            generalTrainingStatsDAO.insert(stats)
        }
    }

    suspend fun calculateStatsForPeriod(period: PeriodSelection): GeneralTrainingStatsEntity {
        val allTrainings = trainingDao.getAllTrainings().first()
        val allSets = trainingDao.getAllSets()

        val now = LocalDate.now()
        val validTrainingIds = when (period) {
            is PeriodSelection.last30day -> allTrainings.filter {
                it.date.month == now.month && it.date.year == now.year
            }.map { it.trainingId }

            is PeriodSelection.last3month -> {
                val months = (0..2).map { now.minusMonths(it.toLong()) }.map { it.month to it.year }
                allTrainings.filter { training ->
                    months.any { (month, year) ->
                        training.date.month == month && training.date.year == year
                    }
                }.map { it.trainingId }
            }

            is PeriodSelection.lass6month -> {
                val months = (0..5).map { now.minusMonths(it.toLong()) }.map { it.month to it.year }
                allTrainings.filter { training ->
                    months.any { (month, year) ->
                        training.date.month == month && training.date.year == year
                    }
                }.map { it.trainingId }
            }

            is PeriodSelection.lastyear -> {
                val months = (0..11).map { now.minusMonths(it.toLong()) }.map { it.month to it.year }
                allTrainings.filter { training ->
                    months.any { (month, year) ->
                        training.date.month == month && training.date.year == year
                    }
                }.map { it.trainingId }
            }

            is PeriodSelection.SpecificMonth -> allTrainings.filter {
                it.date.year == period.year && it.date.monthValue == period.month
            }.map { it.trainingId }

            is PeriodSelection.alltime -> allTrainings.map { it.trainingId }
        }

        val sets = allSets.filter { it.trainingId in validTrainingIds }
        val trainings = allTrainings.filter { it.trainingId in validTrainingIds }

        val totalDays = trainings.map { it.date }.toSet()
        val totalWeeks = trainings.map { it.date.with(java.time.DayOfWeek.MONDAY) }.toSet()
        val totalExercises = sets.map { it.exerciseId }.toSet()

        val totalResp = sets.sumOf { it.reps ?: 0 }
        val totalVolume = sets.sumOf { (it.reps ?: 0) * (it.weight ?: 0) }
        val totalDistance = 0

        return GeneralTrainingStatsEntity(
            date = now,
            totalTrainings = trainings.size,
            trainingWeeks = totalWeeks.size,
            trainingDays = totalDays.size,
            totalExercises = totalExercises.size,
            totalVolume = totalVolume,
            totalReps = totalResp,
            totalSets = sets.size,
            totalDistance = totalDistance
        )
    }


}