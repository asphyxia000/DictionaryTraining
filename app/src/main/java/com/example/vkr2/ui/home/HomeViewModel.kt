package com.example.vkr2.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.repository.TrainingRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


class HomeViewModel(
    private val repository: TrainingRepository
) : ViewModel() {

    private val _trainings = MutableLiveData<List<TrainingsEntity>>()
    val trainings: LiveData<List<TrainingsEntity>> get() = _trainings

    private val _trainingsDate = MutableLiveData<Set<LocalDate>>()
    val trainingsDate: LiveData<Set<LocalDate>> get()=_trainingsDate

    private val _trainingDateCounts = MutableLiveData<Map<LocalDate, Int>>()
    val trainingDateCounts: LiveData<Map<LocalDate, Int>> get() = _trainingDateCounts

    private val _weeks=MutableLiveData<List<WeekGroup>>()
    val weeks: LiveData<List<WeekGroup>>get()=_weeks


    fun loadAllTrainings() {
        viewModelScope.launch {
            repository.getAllTrainings().collect{
                trainings->
                _trainings.postValue(trainings.sortedWith(
                    compareByDescending<TrainingsEntity> { it.date }
                        .thenByDescending { it.createdAt }
                ))
                _trainingsDate.postValue(trainings.map { it.date }.toSet())

                val grouped = trainings.groupingBy { it.date }.eachCount()
                _trainingDateCounts.postValue(grouped)
            }
        }
    }
    fun updateTraining(training: TrainingsEntity){
        viewModelScope.launch {
            repository.updateTraining(training)
            loadAllTrainings()
        }
    }
    fun deleteTraining(training: TrainingsEntity){
        viewModelScope.launch {
            repository.deleteTraining(training)
            loadAllTrainings()
        }
    }

    fun loadTrainingsForDate(date: LocalDate) {
        viewModelScope.launch {
            repository.getTrainingsByDate(date).collect { trainingWithExercises ->
                val onlyEntities = trainingWithExercises.map { it.training }
                _trainings.postValue(onlyEntities)

            }
        }
    }
    fun loadTrainingsGroupedByWeek() {
        viewModelScope.launch {
            repository.getAllTrainings().collect { trainings ->

                val grouped = trainings
                    .groupBy { it.date.with(DayOfWeek.MONDAY) }
                    .map { (weekStart, list) ->
                        val weekEnd = weekStart.plusDays(6)
                        val title = when {
                            weekStart == LocalDate.now().with(DayOfWeek.MONDAY) -> "На этой неделе"
                            else -> "${weekStart.dayOfMonth}–${weekEnd.dayOfMonth} ${
                                weekEnd.month.getDisplayName(
                                    TextStyle.FULL, Locale("ru")
                                )
                            }"
                        }

                        WeekGroup(
                            weekStart = weekStart,
                            weekEnd = weekEnd,
                            title = title,
                            trainings = list.sortedByDescending { it.date }
                        )
                    }
                    .sortedByDescending { it.weekStart }

                _weeks.postValue(grouped)
            }
        }
    }
}
