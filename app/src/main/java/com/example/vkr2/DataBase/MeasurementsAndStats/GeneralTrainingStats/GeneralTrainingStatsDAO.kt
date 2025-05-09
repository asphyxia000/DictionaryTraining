package com.example.vkr2.DataBase.MeasurementsAndStats.GeneralTrainingStats

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneralTrainingStatsDAO {
    @Update
    suspend fun update(statsEntity: GeneralTrainingStatsEntity)
    @Delete
    suspend fun delete(statsEntity: GeneralTrainingStatsEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(statsEntity: GeneralTrainingStatsEntity)

    @Query("Select * from GeneralTrainingStats Order by date")
    fun geAllStats():Flow<List<GeneralTrainingStatsEntity>>
}