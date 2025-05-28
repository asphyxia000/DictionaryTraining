// ui/dashboard/DashboardViewModel.kt
package com.example.vkr2.ui.dashboard


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Добавим этот импорт
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import com.example.vkr2.repository.BodyMeasurementsRepository
import com.example.vkr2.repository.GeneralTrainingStatsRepository
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.PeriodSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map // Добавим этот импорт
import kotlinx.coroutines.launch // Добавим этот импорт


class DashboardViewModel(
    private val generalTrainingStatsRepository: GeneralTrainingStatsRepository,
    private val bodyMeasurementsRepository: BodyMeasurementsRepository,

    ) : ViewModel() {

    private val _selectedPeriod = MutableLiveData<PeriodSelection?>(PeriodSelection.last30day)
    val selectedPeriod: LiveData<PeriodSelection?> get() = _selectedPeriod


    // ИЗМЕНЕНО: теперь возвращает Flow<List<DashboardItem.BodyMeasurementItem>>
    val bodyMeasurementsUI: Flow<List<DashboardItem.BodyMeasurementItem>> =
        bodyMeasurementsRepository.getAll().map { allMeasurements ->
            val sorted = allMeasurements
                .sortedWith(compareByDescending<BodyMeasurementsEntity> { it.date }.thenByDescending { it.id })

            fun extractLatestAndPrev(selector: (BodyMeasurementsEntity) -> Int?): Pair<Int?, Int?> {
                val filtered = sorted.mapNotNull { selector(it) }
                return Pair(filtered.getOrNull(0), filtered.getOrNull(1))
            }

            val (latestNeck, prevNeck) = extractLatestAndPrev { it.neck }
            val (latestShoulders, prevShoulders) = extractLatestAndPrev { it.shoulders }
            val (latestChest, prevChest) = extractLatestAndPrev { it.chest }
            val (latestWaist, prevWaist) = extractLatestAndPrev { it.waist }
            val (latestPelvis, prevPelvis) = extractLatestAndPrev { it.pelvis }

            val (latestForearmsLeft, prevForearmsLeft) = extractLatestAndPrev { it.forearmsLeft }
            val (latestForearmsRight, prevForearmsRight) = extractLatestAndPrev { it.forearmsRight }
            val (latestBicepsLeft, prevBicepsLeft) = extractLatestAndPrev { it.bicepsLeft }
            val (latestBicepsRight, prevBicepsRight) = extractLatestAndPrev { it.bicepsRight }
            val (latestTricepsLeft, prevTricepsLeft) = extractLatestAndPrev { it.tricepsLeft }
            val (latestTricepsRight, prevTricepsRight) = extractLatestAndPrev { it.tricepsRight }
            val (latestBedroLeft, prevBedroLeft) = extractLatestAndPrev { it.bedroLeft }
            val (latestBegroRight, prevBegroRight) = extractLatestAndPrev { it.begroRight }
            val (latestIkriLeft, prevIkriLeft) = extractLatestAndPrev { it.ikriLeft }
            val (latestIkriRight, prevIkriRight) = extractLatestAndPrev { it.ikriRight }

            listOf(
                DashboardItem.BodyMeasurementItem("Шея", latestNeck, null, prevNeck, null),
                DashboardItem.BodyMeasurementItem("Плечи", latestShoulders, null, prevShoulders, null),
                DashboardItem.BodyMeasurementItem("Грудь", latestChest, null, prevChest, null),
                DashboardItem.BodyMeasurementItem("Талия", latestWaist, null, prevWaist, null),
                DashboardItem.BodyMeasurementItem("Таз", latestPelvis, null, prevPelvis, null),

                DashboardItem.BodyMeasurementItem("Бицепсы", latestBicepsLeft, latestBicepsRight, prevBicepsLeft, prevBicepsRight),
                DashboardItem.BodyMeasurementItem("Трицепсы", latestTricepsLeft, latestTricepsRight, prevTricepsLeft, prevTricepsRight),
                DashboardItem.BodyMeasurementItem("Предплечья", latestForearmsLeft, latestForearmsRight, prevForearmsLeft, prevForearmsRight),
                DashboardItem.BodyMeasurementItem("Бедра", latestBedroLeft, latestBegroRight, prevBedroLeft, prevBegroRight),
                DashboardItem.BodyMeasurementItem("Икры", latestIkriLeft, latestIkriRight, prevIkriLeft, prevIkriRight)
            )
        }




    fun setPeriod(period: PeriodSelection) {
        _selectedPeriod.value = period
    }

    // Этот метод теперь не нужен, так как bodyMeasurementsUI - это Flow
    // suspend fun getBodyMeasurementsUI(): List<DashboardItem.BodyMeasurementItem> { ... }

    fun getGeneralStatsUI(period: PeriodSelection): Flow<List<DashboardItem.GeneralStatItem>> = flow {
        generalTrainingStatsRepository.recalculateGeneralStats(period)
        val latest = generalTrainingStatsRepository.geAllStats().first().maxByOrNull { it.date }
        val list = listOf(
            DashboardItem.GeneralStatItem("Всего тренировок", latest?.totalTrainings.toString()),
            DashboardItem.GeneralStatItem("Дней с тренировками", latest?.trainingDays.toString()),
            DashboardItem.GeneralStatItem("Недель", latest?.trainingWeeks.toString()),
            DashboardItem.GeneralStatItem("Общий тоннаж", "${latest?.totalVolume ?: 0} кг"),
            DashboardItem.GeneralStatItem("Общая дистанция", "${latest?.totalDistance ?: 0f} км"),
            DashboardItem.GeneralStatItem("Всего упражнений", latest?.totalExercises.toString()),
            DashboardItem.GeneralStatItem("Всего подходов", latest?.totalSets.toString()),
            DashboardItem.GeneralStatItem("Всего повторений", latest?.totalReps.toString())
        )
        emit(list)
    }

}