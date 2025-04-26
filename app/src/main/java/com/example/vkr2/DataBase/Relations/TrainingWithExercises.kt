package com.example.vkr2.DataBase.Relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingExerciseCrossRef
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import kotlinx.coroutines.flow.Flow

data class TrainingWithExercises(
    @Embedded val training: TrainingsEntity,
    @Relation(
        entity = ExercisesEntity::class,
        parentColumn = "trainingId",
        entityColumn = "ExercisesId", // ← это из ExercisesEntity
        associateBy = Junction(
            value = TrainingExerciseCrossRef::class,
            parentColumn = "trainingId",
            entityColumn = "exerciseId" // ← это из CrossRef
        )
    )
    val exercises: List<ExerciseWithSets>
)

data class ExerciseWithSets(
    @Embedded val exercise: ExercisesEntity,
    @Relation(
        parentColumn = "ExercisesId",
        entityColumn = "exerciseId"
    )
    val sets: List<SetEntity>
)
