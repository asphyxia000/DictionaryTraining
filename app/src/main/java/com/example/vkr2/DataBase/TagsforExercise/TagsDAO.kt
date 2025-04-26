package com.example.vkr2.DataBase.TagsforExercise

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagsDAO {

    @Query("SELECT * FROM Tags")
    fun getAllTags():  Flow<List<TagsEntity>>

    @Query("SELECT * FROM Tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagsEntity?

    @Query("""
    SELECT DISTINCT t.* FROM Tags t
    INNER JOIN TagsExercises te ON t.TagsId = te.tagsId
    INNER JOIN Exercises e ON e.ExercisesId = te.exerciseId
    WHERE e.muscleGroupID = :groupId
""")
    fun getTagsByMuscleGroup(groupId: Int): Flow<List<TagsEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTag(tag: TagsEntity)
}