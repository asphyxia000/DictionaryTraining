package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

sealed class PeriodSelection {
    object last30day : PeriodSelection()
    object last3month : PeriodSelection()
    object lass6month : PeriodSelection()
    object lastyear : PeriodSelection()
    object alltime : PeriodSelection()
    data class SpecificMonth(val year: Int, val month: Int) : PeriodSelection()
}