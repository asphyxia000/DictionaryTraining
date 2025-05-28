package com.example.vkr2.repository

import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface BodyMeasurementsRepository {
    fun getAll(): Flow<List<BodyMeasurementsEntity>>
    fun getLatest():Flow<BodyMeasurementsEntity?>
    suspend fun getMeasurementsByDate(date: LocalDate): BodyMeasurementsEntity? // Если нужно будет получать замеры за конкретную дату
    suspend fun insertOrUpdate(measurementsEntity: BodyMeasurementsEntity)
    suspend fun deleteMeasurement(measurementsEntity: BodyMeasurementsEntity)
    suspend fun getUniqueMeasurementDates(): Flow<List<LocalDate>>
    suspend fun update(measurementsEntity: BodyMeasurementsEntity)
}