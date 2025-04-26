package com.example.vkr2.ui.home

import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import java.time.LocalDate

data class WeekGroup(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val title: String,
    val trainings: List<TrainingsEntity>,
    var isExpanded: Boolean=true
)
