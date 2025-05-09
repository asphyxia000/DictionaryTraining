package com.example.vkr2.DataBase.MeasurementsAndStats.GeneralTrainingStats

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "GeneralTrainingStats")
data class GeneralTrainingStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val totalTrainings: Int,        // Всего тренировок
    val trainingDays: Int,          // Дней с тренировками
    val trainingWeeks: Int,         // Уникальных недель
    val totalVolume: Int,           // Общий тоннаж (вес)
    val totalDistance: Int,       // Общая дистанция (если есть кардио)
    val totalExercises: Int,        // Всего упражнений
    val totalSets: Int,             // Всего подходов
    val totalReps: Int              // Всего повторений
)
