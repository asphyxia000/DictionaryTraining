package com.example.vkr2.ui.home.TrainingsDay

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr2.DataBase.Relations.TrainingWithExercises
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.repository.TrainingRepository
import kotlinx.coroutines.launch

class TrainindDetailViewModel(
    private val repository: TrainingRepository
):ViewModel() {
    private val _training = MutableLiveData<TrainingWithExercises>()
    val training: LiveData<TrainingWithExercises> =_training

    fun loadTraining(trainingId:Int){
        viewModelScope.launch { repository.getTrainingWithExercises(trainingId).collect{trainingId->
            _training.postValue(trainingId)
        } }
    }
    fun saveChanges(trainingId: Int, newName: String, newComment: String) {
        viewModelScope.launch {
            val oldTraining = training.value?.training
            if (oldTraining != null) {
                val update = TrainingsEntity(
                    trainingId = oldTraining.trainingId, // <-- это важно!
                    date = oldTraining.date,
                    createdAt = oldTraining.createdAt,
                    name = newName,
                    comment = newComment
                )
                Log.d("TrainindDetailVM", "Обновление: id=${update.trainingId}, name=$newName")
                repository.updateTraining(update)
            } else {
                Log.w("TrainindDetailVM", "Не удалось сохранить: тренировка не загружена")
            }
        }
    }

    fun addSet(set: SetEntity){
        viewModelScope.launch {
            repository.addSet(set)
        }
    }
    fun updateSet(set: SetEntity) {
        viewModelScope.launch {
            repository.updateSet(set)
        }
    }
    fun deleteSet(set: SetEntity){
        viewModelScope.launch {
            repository.deleteSet(set)
        }
    }


}