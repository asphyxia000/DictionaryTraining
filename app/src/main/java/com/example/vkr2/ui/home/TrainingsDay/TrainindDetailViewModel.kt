package com.example.vkr2.ui.home.TrainingsDay

import android.util.Log
import androidx.lifecycle.*
import com.example.vkr2.DataBase.Relations.TrainingWithExercises
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.repository.InfoStatsRepository
import com.example.vkr2.repository.TrainingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TrainindDetailViewModel(
    private val trainingRepository: TrainingRepository,
    private val infoStatsRepository: InfoStatsRepository
) : ViewModel() {

    private val _training = MutableLiveData<TrainingWithExercises>()
    val training: LiveData<TrainingWithExercises> = _training

    fun loadTraining(trainingId: Int) {
        viewModelScope.launch {
            trainingRepository.getTrainingWithExercises(trainingId).collect {
                _training.postValue(it)
            }
        }
    }
    fun deleteExercise(trainingId: Int, exerciseId: Int) {
        viewModelScope.launch {
            trainingRepository.removeExerciseFromTraining(trainingId, exerciseId)
            loadTraining(trainingId)
        }
    }

    fun saveChanges(trainingId: Int, newName: String, newComment: String) {
        viewModelScope.launch {
            val oldTraining = training.value?.training
            if (oldTraining != null) {
                val update = TrainingsEntity(
                    trainingId = oldTraining.trainingId,
                    date = oldTraining.date,
                    createdAt = oldTraining.createdAt,
                    name = newName,
                    comment = newComment
                )
                Log.d("TrainindDetailVM", "Обновление: id=${update.trainingId}, name=$newName")
                trainingRepository.updateTraining(update)
            } else {
                Log.w("TrainindDetailVM", "Не удалось сохранить: тренировка не загружена")
            }
        }
    }

    fun addSet(set: SetEntity) {
        viewModelScope.launch {
            trainingRepository.addSet(set)
            refreshStats(set.exerciseId)
        }
    }

    fun updateSet(set: SetEntity) {
        viewModelScope.launch {
            trainingRepository.updateSet(set)
            refreshStats(set.exerciseId)
        }
    }

    fun deleteSet(set: SetEntity) {
        viewModelScope.launch {
            trainingRepository.deleteSet(set)
            refreshStats(set.exerciseId)
        }
    }

    suspend fun getSetsForExercise(trainingId: Int, exerciseId: Int): List<SetEntity> {
        return trainingRepository.getSetsForExercise(trainingId, exerciseId).first()
    }

    private fun refreshStats(exerciseId: Int) {
        viewModelScope.launch {
            // Обновляем только глобальную статистику
            infoStatsRepository.recalculate(exerciseId)
        }
    }
}
