package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.MuscleGroup.MuscleGroupEntity
import kotlinx.coroutines.flow.Flow


interface MuscleGroupRepository {
    val context: Context
    fun getAllMuscleGroup():Flow<List<MuscleGroupEntity>>
    suspend fun insert(mg: MuscleGroupEntity)
    suspend fun update(mg: MuscleGroupEntity)
    suspend fun delete(mg: MuscleGroupEntity)

}