package com.example.vkr2.ui.dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkr2.repository.BodyMeasurementsRepository
import com.example.vkr2.repository.GeneralTrainingStatsRepository

class DashboardViewModelFactory(
    private val generalTrainingStatsRepository: GeneralTrainingStatsRepository,
    private val bodyMeasurementsRepository: BodyMeasurementsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(generalTrainingStatsRepository, bodyMeasurementsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}