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

    private val _selectedPeriod = MutableLiveData<PeriodSelection?>(PeriodSelection.last30day)
    val selectedPeriod: LiveData<PeriodSelection?>get()=_selectedPeriod

    private val _exerciseEntity = MutableLiveData<ExercisesEntity?>()
    val exercisesEntity: LiveData<ExercisesEntity?> get ()=_exerciseEntity

    fun setPeriod(period:PeriodSelection){
        _selectedPeriod.value=period
    }

    fun loadExerciseInfo(exerciseId:Int){
        viewModelScope.launch {
            val info = repository.getInfo(exerciseId)
            _exerciseInfo.value = info

            val path = repository.getExerciseImage(exerciseId)
            _exerciseImagePath.value = path
        }
    }

    fun loadExercisesStats(exerciseId: Int, periodSelection: PeriodSelection) {
        viewModelScope.launch {
            val stats = repository.calculateStatsForPeriod(exerciseId, periodSelection)
            _exerciseStats.value = stats
        }
    }

    fun loadExerciseName(id:Int){
        viewModelScope.launch {
            val entity = repository.getExerciseById(id)
            _exerciseEntity.postValue(entity)
        }
    }
}