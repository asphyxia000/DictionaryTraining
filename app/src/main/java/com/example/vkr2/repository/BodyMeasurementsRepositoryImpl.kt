// repository/BodyMeasurementsRepositoryImpl.kt
package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import com.example.vkr2.DataBase.MeasurementsAndStats.Measurements.BodyMeasurementsDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BodyMeasurementsRepositoryImpl(
    context: Context,
    coroutineScope: CoroutineScope
) : BodyMeasurementsRepository {

    private val bodyMeasurementsDAO: BodyMeasurementsDAO =
        FitnessDatabase.getInstance(context, coroutineScope)?.BodyMeasurementsDAO()
            ?: throw IllegalArgumentException("Database not initialized or BodyMeasurementsDAO not available")

    override fun getAll(): Flow<List<BodyMeasurementsEntity>> {
        return bodyMeasurementsDAO.getAll()
    }

    override suspend fun update(measurementsEntity: BodyMeasurementsEntity) {
        withContext(Dispatchers.IO) {
            bodyMeasurementsDAO.update(measurementsEntity)
        }
    }

    override fun getLatest(): Flow<BodyMeasurementsEntity?> =
        bodyMeasurementsDAO.getLatest()

    override suspend fun getMeasurementsByDate(date: LocalDate): BodyMeasurementsEntity? {
        // Этот метод, возможно, не понадобится, если вы всегда добавляете новые записи
        // но оставим его на всякий случай.
        return bodyMeasurementsDAO.getByDate(date)
    }

    override suspend fun insertOrUpdate(measurement: BodyMeasurementsEntity) {
        withContext(Dispatchers.IO) {
            bodyMeasurementsDAO.insertOrUpdate(measurement)
        }
    }

    override suspend fun deleteMeasurement(measurement: BodyMeasurementsEntity) {
        withContext(Dispatchers.IO) {
            bodyMeasurementsDAO.delete(measurement)
        }
    }

    override suspend fun getUniqueMeasurementDates(): Flow<List<LocalDate>> {
        return bodyMeasurementsDAO.getAll().map { measurements ->
            measurements.map { it.date }.distinct().sortedDescending()
        }
    }
}