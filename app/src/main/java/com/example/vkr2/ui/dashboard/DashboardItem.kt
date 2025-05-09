package com.example.vkr2.ui.dashboard

sealed class DashboardItem {
    data class BodyMeasurementItem(val label: String, val left: Int?, val right: Int?) : DashboardItem()
    data class GeneralStatItem(val label: String, val value: String) : DashboardItem()
}
