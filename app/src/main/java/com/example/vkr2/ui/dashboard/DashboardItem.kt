package com.example.vkr2.ui.dashboard

sealed class DashboardItem {
    data class BodyMeasurementItem(
        val label: String,
        val left: Int? = null,
        val right: Int? = null,
        val prevLeft: Int? = null,
        val prevRight: Int? = null,
    ) : DashboardItem()

    data class GeneralStatItem(val label: String, val value: String) : DashboardItem()
}
