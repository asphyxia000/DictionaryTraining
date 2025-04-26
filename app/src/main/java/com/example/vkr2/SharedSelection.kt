package com.example.vkr2

import androidx.lifecycle.ViewModel
import com.example.vkr2.DataBase.Exercises.ExercisesEntity

class SharedSelection : ViewModel() {
    private val selectedExercises = mutableMapOf<Int, ExercisesEntity>()

    fun toggleSelection(exercise: ExercisesEntity) {
        if (selectedExercises.containsKey(exercise.ExercisesId)) {
            selectedExercises.remove(exercise.ExercisesId)
        } else {
            selectedExercises[exercise.ExercisesId] = exercise
        }
    }

    fun isSelected(id: Int): Boolean {
        return selectedExercises.containsKey(id)
    }

    fun getAllSelected(): List<ExercisesEntity> {
        return selectedExercises.values.toList()
    }

    fun clearSelection() {
        selectedExercises.clear()
    }

    fun getSelectedCount(): Int = selectedExercises.size
}
