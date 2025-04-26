package com.example.vkr2.DataBase.Exercises.DetailExercise

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface InfoStatsDAO {

    @Query("Select * from ExerciseInfo where exerciseId =:exerciseId Limit 1")
    suspend fun getInfo(exerciseId: Int):ExerciseInfo?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfo(info: ExerciseInfo)

    @Query("Select imagePath From Exercises Where ExercisesId = :exerciseId Limit 1")
    suspend fun getExerciseImage(exerciseId: Int):String?

    @Query("Select * from ExercisesStats where exerciseId = :exerciseId Limit 1")
    suspend fun getStats(exerciseId:Int): ExerciseStats?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: ExerciseStats):Long
    @Update
    suspend fun updateStats(stats: ExerciseStats)
}