package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseInfo
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseStats
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.repository.InfoStatsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ExerciseDetailViewModel(
    private val repository: InfoStatsRepository
):ViewModel(){
    private val _exerciseInfo = MutableLiveData<ExerciseInfo?>()
    val exerciseInfo: LiveData<ExerciseInfo?> get() = _exerciseInfo

    private val _exerciseStats = MutableLiveData<ExerciseStats?>()
    val exerciseStats:LiveData<ExerciseStats?>get()=_exerciseStats

    private val _exerciseImagePath = MutableLiveData<String?>()
    val exerciseImagePath: MutableLiveData<String?> get() = _exerciseImagePath

    private val _exerciseEntity = MutableLiveData<ExercisesEntity?>()
    val exercisesEntity: LiveData<ExercisesEntity?> get ()=_exerciseEntity

    fun loadExerciseInfo(exerciseId:Int){
        viewModelScope.launch {
            val info = repository.getInfo(exerciseId)
            _exerciseInfo.value = info

            val path = repository.getExerciseImage(exerciseId)
            _exerciseImagePath.value = path
        }
    }

    fun calculateStats(exerciseId: Int){
        viewModelScope.launch {
            val sets = repository.getSetsForExercises(exerciseId).first()
            if(sets.isNotEmpty()){
                val totalSets = sets.size
                val totalResp = sets.sumOf { it.reps ?: 0 }
                val totalWeight = sets.sumOf { (it.reps ?: 0) * (it.weight ?: 0) }
                val maxWeight = sets.maxOf { it.weight ?: 0 }
                val maxResp = sets.maxOf { it.reps ?: 0 }
                val maxVolume = sets.maxOf { (it.reps ?: 0) * (it.weight ?: 0) }

                val maxVolumeSet = sets.maxByOrNull { (it.reps ?: 0) * (it.weight ?: 0) }
                val maxVolumeDate = maxVolumeSet?.trainingId?.let {
                    repository.getTrainingbyId(it)?.createdAt
                }?:LocalDateTime.MIN

                val oneRepMaxSet = sets.maxByOrNull {
                    val reps = it.reps ?: 0
                    val weight = it.weight ?: 0
                    weight * (1 + reps / 30.0)
                }
                val oneRepMax = oneRepMaxSet?.let {
                    val reps = it.reps ?: 0
                    val weight = it.weight ?: 0
                    weight * (1 + reps / 30.0)
                } ?: 0.0
                val oneRepMaxDate = oneRepMaxSet?.trainingId?.let {
                    repository.getTrainingbyId(it)?.createdAt
                }?:LocalDateTime.MIN

                val trainingCount = sets.map { it.trainingId }.distinct().size

                val stats = ExerciseStats(
                    exerciseId = exerciseId,
                    totalSets = totalSets,
                    totalResp = totalResp,
                    totalWeight = totalWeight,
                    totalTrainings = trainingCount,
                    maxWeight = maxWeight,
                    maxResp = maxResp,
                    maxWorkoutVolume = maxVolume,
                    maxWorkoutVolumeDate = maxVolumeDate,
                    estimatedOneRepMax = oneRepMax,
                    oneRepMaxDate = oneRepMaxDate
                )
                repository.insertOrUpdateStats(stats)
                _exerciseStats.postValue(stats)
            }
            else {
                // Если подходов вообще нет — отобразить нули
                _exerciseStats.postValue(
                    ExerciseStats(
                        exerciseId = exerciseId,
                        totalSets = 0,
                        totalResp = 0,
                        totalWeight = 0,
                        totalTrainings = 0,
                        maxWeight = 0,
                        maxResp = 0,
                        maxWorkoutVolume = 0,
                        maxWorkoutVolumeDate = LocalDateTime.MIN,
                        estimatedOneRepMax = 0.0,
                        oneRepMaxDate = LocalDateTime.MIN
                    )
                )
            }
        }
    }
    fun loadExercisesStats(exerciseId: Int){
        viewModelScope.launch {
            val stats = repository.getStats(exerciseId)
           if (stats != null){
               _exerciseStats.value = stats
           }else{
               calculateStats(exerciseId)
           }
        }
    }

    fun loadExerciseName(id:Int){
        viewModelScope.launch {
            val entity = repository.getExerciseById(id)
            _exerciseEntity.postValue(entity)
        }
    }
}