package com.example.vkr2.DataBase.Exercises

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExercisesDAO {

    @Query("Select * From Exercises Where muscleGroupID = :muscleGroupID Order by ExercisesName ASC")
    fun getExpByMG(muscleGroupID: Int): Flow<List<ExercisesEntity>>

    @Query("DELETE FROM Exercises")
    suspend fun clearTable()

    @Query("SELECT * FROM Exercises WHERE ExercisesName = :name LIMIT 1")
    suspend fun getExerciseByName(name: String): ExercisesEntity?

    @Query("SELECT * FROM Exercises WHERE ExercisesId = :id LIMIT 1")
    suspend fun getExerciseById(id: Int): ExercisesEntity?

    @Query("""
    SELECT e.* FROM Exercises e
    JOIN TagsExercises te ON e.ExercisesId = te.exerciseId
    JOIN Tags t ON te.tagsId = t.TagsId
    WHERE e.muscleGroupID = :groupId
    AND (:isTagFilterEmpty OR e.ExercisesId IN (
        SELECT te.exerciseId
        FROM TagsExercises te
        JOIN Tags t ON te.tagsId = t.TagsId
        WHERE t.name IN (:tagNames)
        GROUP BY te.exerciseId
        HAVING COUNT(DISTINCT t.name) = :tagCount
    ))
    AND (:query IS NULL OR e.ExercisesName LIKE '%' || :query || '%')
    GROUP BY e.ExercisesId
""")
    fun searchExercisesByTagsAndName(
        groupId: Int,
        tagNames: List<String>,
        isTagFilterEmpty: Boolean,
        tagCount: Int,
        query: String?
    ): Flow<List<ExercisesEntity>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: ExercisesEntity):Long

    @Update
    suspend fun updateExercise(exercise: ExercisesEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExercisesEntity)

}