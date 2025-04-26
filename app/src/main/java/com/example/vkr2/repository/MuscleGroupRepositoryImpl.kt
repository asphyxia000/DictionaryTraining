package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.MuscleGroup.MuscleGroupDAO
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.MuscleGroup.MuscleGroupEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MuscleGroupRepositoryImpl(
    override val context: Context,
    scope: CoroutineScope,
    private val backgroundDispatcher: CoroutineDispatcher
) : MuscleGroupRepository {

    private val mgroupDAO: MuscleGroupDAO

    init {
        val database = FitnessDatabase.getInstance(context, scope)
        mgroupDAO = database!!.MGroupDAO()
    }


    override fun getAllMuscleGroup(): Flow<List<MuscleGroupEntity>> {
        return mgroupDAO.getMG()
    }

    override suspend fun insert(mg: MuscleGroupEntity) {
        withContext(backgroundDispatcher) {
            mgroupDAO.insertMG(mg)
        }
    }

    override suspend fun update(mg: MuscleGroupEntity) {
        withContext(backgroundDispatcher) {
            mgroupDAO.updateMG(mg)
        }
    }

    override suspend fun delete(mg: MuscleGroupEntity) {
        withContext(backgroundDispatcher) {
            mgroupDAO.deleteMG(mg)
        }
    }
}