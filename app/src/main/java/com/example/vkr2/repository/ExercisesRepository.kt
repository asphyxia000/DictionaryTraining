package com.example.vkr2.repository

import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import kotlinx.coroutines.flow.Flow

interface ExercisesRepository {

    fun getAllExercises(muscleGroupID: Int):Flow<List<ExercisesEntity>>
    fun searchExercisesByTagsAndName(groupId: Int, tagNames: List<String>,isTagFilterEmpty: Boolean, query: String?): Flow<List<ExercisesEntity>>

}