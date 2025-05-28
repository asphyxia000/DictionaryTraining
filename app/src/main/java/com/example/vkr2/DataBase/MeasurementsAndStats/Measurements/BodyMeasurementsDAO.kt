package com.example.vkr2.DataBase.MeasurementsAndStats.Measurements

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// DataBase/MeasurementsAndStats/Measurements/BodyMeasurementsDAO.kt
@Dao
interface BodyMeasurementsDAO {
    @Insert // Удаляем onConflict = OnConflictStrategy.REPLACE
    suspend fun insert(measurementsEntity: BodyMeasurementsEntity)

    @Update
    suspend fun update(measurementsEntity: BodyMeasurementsEntity)

    @Delete
    suspend fun delete(measurementsEntity: BodyMeasurementsEntity)
    @Query("DELETE FROM BodyMeasurements")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(measurement: BodyMeasurementsEntity)

    @Query ("Select * from BodyMeasurements order by date DESC  Limit 1")
    fun getLatest(): Flow<BodyMeasurementsEntity?>

    @Query("SELECT * FROM BodyMeasurements WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: LocalDate): BodyMeasurementsEntity?

    @Query("Select * from BodyMeasurements order by date DESC") // Сортируем по убыванию даты для получения всех замеров
    fun getAll(): Flow<List<BodyMeasurementsEntity>>

    @Query("SELECT * FROM BodyMeasurements")
    suspend fun getAllDirect(): List<BodyMeasurementsEntity>
}