package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import com.example.vkr2.DataBase.MeasurementsAndStats.Measurements.BodyMeasurementsDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BodyMeasurementsRepositoryImpl(
    context: Context,
    coroutineScope: CoroutineScope
) : BodyMeasurementsRepository {

    private val bodyMeasurementsDAO: BodyMeasurementsDAO =
        FitnessDatabase.getInstance(context, coroutineScope)?.BodyMeasurementsDAO() // Убедитесь, что в FitnessDatabase есть метод bodyMeasurementsDAO()
            ?: throw IllegalArgumentException("Database not initialized or BodyMeasurementsDAO not available")

    override suspend fun getAll(): Flow<List<BodyMeasurementsEntity>> {
        return bodyMeasurementsDAO.getAll() // Убедитесь, что getAll() возвращает Flow
    }

    override fun getLatest(): Flow<BodyMeasurementsEntity?> =
        bodyMeasurementsDAO.getLatest()

    override suspend fun getMeasurementsByDate(date: LocalDate): BodyMeasurementsEntity? {
        return bodyMeasurementsDAO.getByDate(date)
    }
    
   override suspend fun insertOrUpdate(measurement: BodyMeasurementsEntity) {
       withContext(Dispatchers.IO) {
           val existingMeasurement = bodyMeasurementsDAO.getAll()
               .map { list -> list.find { it.date == measurement.date } }
               .distinctUntilChanged()
               .first()

           if (existingMeasurement != null) {
               bodyMeasurementsDAO.update(measurement.copy(id = existingMeasurement.id))
           } else {
               bodyMeasurementsDAO.insert(measurement)
           }
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