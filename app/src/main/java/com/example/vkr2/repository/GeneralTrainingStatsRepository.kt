package com.example.vkr2.repository

import com.example.vkr2.DataBase.MeasurementsAndStats.GeneralTrainingStats.GeneralTrainingStatsEntity
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.PeriodSelection
import kotlinx.coroutines.flow.Flow

interface GeneralTrainingStatsRepository {
    suspend fun recalculateGeneralStats(period: PeriodSelection)
    suspend fun insertOrUpdate(stats: GeneralTrainingStatsEntity)
    suspend fun geAllStats():Flow<List<GeneralTrainingStatsEntity>>
}