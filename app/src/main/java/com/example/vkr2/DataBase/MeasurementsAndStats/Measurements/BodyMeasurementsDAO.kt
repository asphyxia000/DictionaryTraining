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

@Dao
interface BodyMeasurementsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurementsEntity: BodyMeasurementsEntity)

    @Update
    suspend fun update(measurementsEntity: BodyMeasurementsEntity)

    @Delete
    suspend fun delete(measurementsEntity: BodyMeasurementsEntity)
    @Query("DELETE FROM BodyMeasurements")
    suspend fun deleteAll()

    @Query ("Select * from BodyMeasurements order by date DESC  Limit 1")
     fun getLatest(): Flow<BodyMeasurementsEntity?>

    @Query("SELECT * FROM BodyMeasurements WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: LocalDate): BodyMeasurementsEntity?

    @Query("Select * from BodyMeasurements order by date")
    fun getAll(): Flow<List<BodyMeasurementsEntity>>

    @Query("SELECT * FROM BodyMeasurements")
    suspend fun getAllDirect(): List<BodyMeasurementsEntity>
}