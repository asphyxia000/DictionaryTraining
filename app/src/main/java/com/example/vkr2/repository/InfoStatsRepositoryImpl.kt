package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseInfo
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseStats
import com.example.vkr2.DataBase.Exercises.DetailExercise.InfoStatsDAO
import com.example.vkr2.DataBase.Exercises.ExercisesDAO
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingDAO
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class InfoStatsRepositoryImpl (
    context: Context,
    coroutineScope: CoroutineScope
):InfoStatsRepository{

    private val infoStatsDAO: InfoStatsDAO =
        FitnessDatabase.getInstance(context,coroutineScope)?.InfoStatsDAO()
            ?: throw IllegalArgumentException("Database not initialized")
    private val trainingDAO: TrainingDAO=
        FitnessDatabase.getInstance(context,coroutineScope)?.TrainingDAO()
            ?:throw IllegalArgumentException("Database not initialized")
    private val exercisesDAO:ExercisesDAO =
        FitnessDatabase.getInstance(context,coroutineScope)?.ExpDAO()
            ?:throw IllegalArgumentException("Database not initialized")

    override suspend fun getInfo(exerciseId:Int): ExerciseInfo?{
        return infoStatsDAO.getInfo(exerciseId)
    }

    override suspend fun insertInfo(info: ExerciseInfo){
        infoStatsDAO.insertInfo(info)
    }

    override suspend fun getStats(exerciseId: Int):ExerciseStats?{
        return infoStatsDAO.getStats(exerciseId)
    }
    override suspend fun insertOrUpdateStats(stats: ExerciseStats){
        val existing = infoStatsDAO.getStats(stats.exerciseId)
        if (existing == null){
            infoStatsDAO.insertStats(stats)
        }
        else{
            infoStatsDAO.updateStats(stats.copy(statid = existing.statid))
        }
    }

    override suspend fun getExerciseImage(exerciseId: Int): String? {
        return infoStatsDAO.getExerciseImage(exerciseId)
    }

    override suspend fun getSetsForExercises(exerciseId: Int): Flow<List<SetEntity>> {
        return trainingDAO.getSetsForExercises(exerciseId)
    }

    override suspend fun getTrainingbyId(training: Int): TrainingsEntity? {
        return trainingDAO.getTrainingbyId(training)
    }
    override suspend fun getExerciseById(id:Int): ExercisesEntity?{
        return exercisesDAO.getExerciseById(id)
    }

}