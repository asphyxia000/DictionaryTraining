package com.example.vkr2.ui.dashboard


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vkr2.repository.BodyMeasurementsRepository
import com.example.vkr2.repository.GeneralTrainingStatsRepository
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.PeriodSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map


class DashboardViewModel(
    private val generalTrainingStatsRepository: GeneralTrainingStatsRepository,
    private val bodyMeasurementsRepository: BodyMeasurementsRepository,

) : ViewModel() {

    private val _selectedPeriod = MutableLiveData<PeriodSelection?>(PeriodSelection.last30day)
    val selectedPeriod: LiveData<PeriodSelection?> get() = _selectedPeriod

    fun setPeriod(period: PeriodSelection) {
        _selectedPeriod.value = period
    }

    fun getBodyMeasurementsUI(): Flow<List<DashboardItem.BodyMeasurementItem>> =
        bodyMeasurementsRepository.getLatest().map { latest ->
                listOf(
                    DashboardItem.BodyMeasurementItem("Шея", latest?.neck, null),
                    DashboardItem.BodyMeasurementItem("Плечи", latest?.shoulders, null),
                    DashboardItem.BodyMeasurementItem("Грудь", latest?.chest, null),
                    DashboardItem.BodyMeasurementItem("Талия", latest?.waist, null),
                    DashboardItem.BodyMeasurementItem("Таз", latest?.pelvis, null),

                    DashboardItem.BodyMeasurementItem(
                        "Предплечья",
                        latest?.forearmsLeft,
                        latest?.forearmsRight
                    ),
                    DashboardItem.BodyMeasurementItem(
                        "Бицепсы",
                        latest?.bicepsLeft,
                        latest?.bicepsRight
                    ),
                    DashboardItem.BodyMeasurementItem(
                        "Трицепсы",
                        latest?.tricepsLeft,
                        latest?.tricepsRight
                    ),
                    DashboardItem.BodyMeasurementItem(
                        "Бедра",
                        latest?.bedroLeft,
                        latest?.begroRight
                    ),
                    DashboardItem.BodyMeasurementItem(
                        "Икры",
                        latest?.ikriLeft,
                        latest?.ikriRight
                    )
                )
            }
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