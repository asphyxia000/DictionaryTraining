package com.example.vkr2.DataBase.MuscleGroup

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MuscleGroupDAO{
    //CRUD

    @Query("SELECT * FROM MuscleGroup")
    fun getMG():Flow<List<MuscleGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMG(MGroupEntity: MuscleGroupEntity):Long

    @Update
    suspend fun updateMG(MGroupEntity: MuscleGroupEntity):Int

    @Delete
    suspend fun deleteMG(MGroupEntity: MuscleGroupEntity):Int
}