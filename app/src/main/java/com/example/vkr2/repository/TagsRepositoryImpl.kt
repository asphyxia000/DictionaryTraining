package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.TagsforExercise.TagsDAO
import com.example.vkr2.DataBase.TagsforExercise.TagsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class TagsRepositoryImpl(
    context: Context,
    coroutineScope: CoroutineScope
):TagsRepository {
    private val tagsDAO:TagsDAO =
        FitnessDatabase.getInstance(context, coroutineScope)?.TagDAO()
            ?: throw IllegalArgumentException("Database not initialized")

    override fun getTagsByMuscleGroup(groupId: Int): Flow<List<TagsEntity>> {
        return tagsDAO.getTagsByMuscleGroup(groupId)
    }
}