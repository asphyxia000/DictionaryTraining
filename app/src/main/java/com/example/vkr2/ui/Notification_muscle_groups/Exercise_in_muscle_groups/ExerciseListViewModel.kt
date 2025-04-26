package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap // Add this import
import androidx.lifecycle.viewModelScope
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.TagsforExercise.TagsEntity
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.repository.ExercisesRepository
import com.example.vkr2.repository.TagsRepository
import com.example.vkr2.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.w3c.dom.Comment
import java.time.LocalDate

class ExerciseListViewModel(
    private val exercisesRepository: ExercisesRepository,
    private val tagsRepository: TagsRepository,
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val selectedGroupId = MutableLiveData<Int>()
    private val selectedTags = MutableLiveData<List<String>>(emptyList())
    private val searchQuery = MutableLiveData<String>("")

    val exercises = selectedGroupId.switchMap { groupId ->
        selectedTags.switchMap { tags ->
            searchQuery.switchMap { query ->

                val isTagFilterEmpty = tags.isEmpty()

                exercisesRepository
                    .searchExercisesByTagsAndName(
                        groupId = groupId,
                        tagNames = tags,
                        isTagFilterEmpty = isTagFilterEmpty,
                        query = query
                    )
                    .asLiveData()
            }
        }
    }


    fun setGroupId(groupId: Int) {
        selectedGroupId.value = groupId
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleTag(tag: String) {
        val current = selectedTags.value?.toMutableList() ?: mutableListOf()
        if (tag in current) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        selectedTags.value = current
    }

    fun getTagsForGroup(groupId: Int): Flow<List<TagsEntity>> {
        return tagsRepository.getTagsByMuscleGroup(groupId)
    }

    suspend fun createTraining(
        date: LocalDate,
        name: String,
        comment: String,
        exercises:List<ExercisesEntity>
    ){
        val trainingId = trainingRepository.addTraining(
            TrainingsEntity(date=date,name=name,comment=comment)
        )
        exercises.forEach { exercise ->
            trainingRepository.addExerciseToTraining(trainingId.toInt(), exercise.ExercisesId)
        }
    }
}
