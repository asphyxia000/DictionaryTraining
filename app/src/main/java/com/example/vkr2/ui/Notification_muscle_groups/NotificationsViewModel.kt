package com.example.vkr2.ui.Notification_muscle_groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.DataBase.MuscleGroup.MuscleGroupEntity
import com.example.vkr2.repository.MuscleGroupRepository
import com.example.vkr2.repository.MuscleGroupRepositoryImpl
import kotlinx.coroutines.launch

class NotificationsViewModel(private val repository: MuscleGroupRepository) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    val muscleGroups: LiveData<List<MuscleGroupEntity>> =
        repository.getAllMuscleGroup().asLiveData()

    init {
        viewModelScope.launch {
            if (repository is MuscleGroupRepositoryImpl) {
                val db = FitnessDatabase.getInstance(repository.context, viewModelScope)
                db?.let { database ->
                    FitnessDatabase.populateDatabase(database.MGroupDAO(), database.ExpDAO(),database.TagExpDAO(),database.TagDAO(),database.TrainingDAO(),database.InfoStatsDAO())
                }
            }
        }
    }

    fun getAllMuscleGroups() {
        _dataLoading.value = true
        _dataLoading.value = false
    }

    private fun showProgress() {
        _dataLoading.value = true
    }

    private fun hideProgress() {
        _dataLoading.value = false
    }
}
