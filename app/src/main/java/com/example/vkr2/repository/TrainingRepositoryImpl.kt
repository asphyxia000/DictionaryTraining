package com.example.vkr2.repository

import android.content.Context
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.Relations.TrainingWithExercises
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingDAO
import com.example.vkr2.DataBase.Trainings.TrainingExerciseCrossRef
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TrainingRepositoryImpl(
    context: Context,
    coroutineScope: CoroutineScope
): TrainingRepository {

    private val trainingDAO:TrainingDAO=
        FitnessDatabase.getInstance(context,coroutineScope)?.TrainingDAO()
            ?: throw IllegalArgumentException("Database not initialized")

    override suspend fun addTraining(training: TrainingsEntity): Long {
        return trainingDAO.insertTraining(training)
    }

    override suspend fun updateTraining(training: TrainingsEntity) {
        trainingDAO.updateTraining(training)
    }

    override suspend fun deleteTraining(training: TrainingsEntity) {
        trainingDAO.deleteTraining(training)
    }

    override suspend fun addExerciseToTraining(trainingId: Int, exerciseId: Int) {
        trainingDAO.insertTrainingExercise(
            TrainingExerciseCrossRef(trainingId,exerciseId)
        )
    }

    override suspend fun removeExerciseFromTraining(trainingId: Int, exerciseId: Int) {
        trainingDAO.deleteExercisesfromTraining(trainingId,exerciseId)
    }

    override suspend fun addSet(set: SetEntity) {
        trainingDAO.insertSet(set)
    }

    override suspend fun updateSet(set: SetEntity) {
        trainingDAO.updateSet(set)
    }

    override suspend fun deleteSet(set: SetEntity) {
        trainingDAO.deleteSet(set)
    }

    override fun getAllTrainings(): Flow<List<TrainingsEntity>>{
        return trainingDAO.getAllTrainings()
    }

    override fun getTrainingsByDate(date: LocalDate): Flow<List<TrainingWithExercises>> {
        return trainingDAO.getTrainingsByDate(date)
    }

    override fun getSetsForExercise(trainingId: Int, exerciseId: Int): Flow<List<SetEntity>> {
        return trainingDAO.getSetsforExercise(trainingId,exerciseId)
    }

    override fun getTrainingWithExercises(trainingId: Int): Flow<TrainingWithExercises> {
        return trainingDAO.getTrainingWithExercises(trainingId)
    }

}