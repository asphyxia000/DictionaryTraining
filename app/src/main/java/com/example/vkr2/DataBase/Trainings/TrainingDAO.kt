package com.example.vkr2.DataBase.Trainings

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.vkr2.DataBase.Relations.TrainingWithExercises
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TrainingDAO {
    @Insert suspend fun insertTraining(training: TrainingsEntity):Long
    @Update suspend fun updateTraining(training: TrainingsEntity)
    @Delete suspend fun deleteTraining(training: TrainingsEntity)

    @Insert suspend fun insertTrainingExercise(crossRef: TrainingExerciseCrossRef)
    @Query("DELETE FROM TrainingExercises WHERE trainingId = :trainingId AND exerciseId = :exerciseId")
    suspend fun deleteExercisesfromTraining(trainingId:Int,exerciseId:Int)

    @Insert suspend fun insertSet(set: SetEntity)
    @Delete suspend fun deleteSet(set: SetEntity)
    @Update suspend fun updateSet(set: SetEntity)

    @Transaction
    @Query("Select * from Trainings where date = :date")
    fun getTrainingsByDate(date:LocalDate): Flow<List<TrainingWithExercises>>

    @Transaction
    @Query("Select * from Trainings ORDER BY date DESC,createdAt DESC")
    fun getAllTrainings():Flow<List<TrainingsEntity>>

    @Transaction
    @Query("Select * from Sets where trainingId = :trainingId and exerciseId = :exerciseId Order by 'order' ASC")
    fun getSetsforExercise(trainingId: Int,exerciseId: Int):Flow<List<SetEntity>>

    @Transaction
    @Query("SELECT * FROM Trainings WHERE trainingId = :trainingId")
    fun getTrainingWithExercises(trainingId: Int): Flow <TrainingWithExercises>

    @Transaction
    @Query("Select * from Trainings where trainingId = :id")
    suspend fun getTrainingbyId(id:Int): TrainingsEntity?

    @Transaction
    @Query("SELECT * FROM Sets WHERE exerciseId = :exerciseId")
    fun getSetsForExercises(exerciseId: Int): Flow<List<SetEntity>>

}