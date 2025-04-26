package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.Exercises.ExercisesDAO
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.FitnessDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class ExercisesRepositoryImpl(
    context: Context,
    scope: CoroutineScope
):ExercisesRepository {

        private val exerciseDAO:ExercisesDAO

        init {
            val database = FitnessDatabase.getInstance(context,scope)
            exerciseDAO = database!!.ExpDAO()
        }

    override fun getAllExercises(muscleGroupID: Int): Flow<List<ExercisesEntity>> {
        return exerciseDAO.getExpByMG(muscleGroupID)
    }
    override fun searchExercisesByTagsAndName(
        groupId: Int,
        tagNames: List<String>,
        isTagFilterEmpty: Boolean,
        query: String?
    ): Flow<List<ExercisesEntity>> {
        return exerciseDAO.searchExercisesByTagsAndName(groupId, tagNames, isTagFilterEmpty, tagNames.size ,query)
    }

}