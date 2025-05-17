package com.example.vkr2.ui.settings

import android.os.Build
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingExerciseCrossRef
import com.example.vkr2.DataBase.Trainings.TrainingsEntity

data class BackupData(
    val timestamp: Long = System.currentTimeMillis(),
    val device: String = Build.MODEL,
    val trainings: List<TrainingsEntity>,
    val sets: List<SetEntity>,
    val measurements: List<BodyMeasurementsEntity>,
    val crossRef: List<TrainingExerciseCrossRef>
)
