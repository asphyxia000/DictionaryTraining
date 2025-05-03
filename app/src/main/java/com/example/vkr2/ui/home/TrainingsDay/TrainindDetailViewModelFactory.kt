package com.example.vkr2.ui.home.TrainingsDay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkr2.repository.InfoStatsRepository
import com.example.vkr2.repository.TrainingRepository
import com.example.vkr2.repository.TrainingRepositoryImpl

class TrainindDetailViewModelFactory(
    private val trainingRepository: TrainingRepository,
    private val infoStatsRepository: InfoStatsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainindDetailViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return TrainindDetailViewModel(trainingRepository,infoStatsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
        return super.create(modelClass)
    }
}
