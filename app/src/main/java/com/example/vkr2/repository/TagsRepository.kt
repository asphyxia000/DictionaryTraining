package com.example.vkr2.repository

import com.example.vkr2.DataBase.TagsforExercise.TagsEntity
import kotlinx.coroutines.flow.Flow

interface TagsRepository {
    fun getTagsByMuscleGroup(groupId: Int): Flow<List<TagsEntity>>
}