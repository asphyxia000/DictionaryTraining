package com.example.vkr2.DataBase.TagsforExercise

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagsExercisesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExerciseTag(exerciseTag: TagsExercisesEntity)

    @Query("""
    SELECT Exercises.* FROM Exercises
    INNER JOIN TagsExercises ON Exercises.ExercisesId = TagsExercises.exerciseId
    INNER JOIN Tags ON TagsExercises.tagsId = Tags.TagsId
    WHERE Tags.name = :tagName
""")
    fun getExercisesByTag(tagName: String): Flow<List<ExercisesEntity>>

}