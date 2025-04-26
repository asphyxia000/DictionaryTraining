package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkr2.repository.ExercisesRepository
import com.example.vkr2.repository.TagsRepository
import com.example.vkr2.repository.TrainingRepository
import kotlinx.coroutines.CoroutineDispatcher

class ExerciseListViewModelFactory(
    private val exercisesRepository: ExercisesRepository,
    private val tagsRepository: TagsRepository,
    private val trainingRepository: TrainingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseListViewModel(exercisesRepository, tagsRepository,trainingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
