package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentStatsBinding
import com.example.vkr2.repository.InfoStatsRepositoryImpl
import com.example.vkr2.ui.AdaptersDirectory.StatsAdapter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExerciseDetailViewModel
    private lateinit var repository: InfoStatsRepositoryImpl

    companion object {
        fun newInstance(exerciseId: Int): StatsFragment {
            val fragment = StatsFragment()
            fragment.arguments = Bundle().apply {
                putInt("exerciseId", exerciseId)
            }
            return fragment
        }
    }

    private var currentPeriodIndex = 0
    private lateinit var fullPeriodList: List<PeriodSelection>
    private lateinit var fullSpinnerItems: List<String>
    private var isLineChartVisible = false
    // Переменные-члены для хранения данных графика
    private var currentFilteredSets: List<SetEntity> = emptyList()
    private var currentTrainingDatesMap: Map<Int, TrainingsEntity> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val exerciseId = requireArguments().getInt("exerciseId")
        repository = InfoStatsRepositoryImpl(requireContext().applicationContext, CoroutineScope(Dispatchers.IO))
        viewModel = ViewModelProvider(this, ExercisesDetailViewModelFactory(repository))[ExerciseDetailViewModel::class.java]

        CoroutineScope(Dispatchers.IO).launch {
            val allTrainings = repository.getAllTrainings().first()
            val allSets = repository.getSetsForExercises(exerciseId).first()
            val idwithex = allSets.map { it.trainingId }.toSet()

            val specificMonth = allTrainings
                .filter { it.trainingId in idwithex }
                .map { YearMonth.of(it.date.year, it.date.monthValue) }
                .distinct()
                .sortedDescending()

            val specificPeriodList = specificMonth.map { PeriodSelection.SpecificMonth(it.year, it.monthValue) }
            val specificSpinnerItems = specificMonth.map {
                it.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))).replaceFirstChar { c -> c.uppercaseChar() }
            }

            val basePeriods = listOf(
                PeriodSelection.last30day,
                PeriodSelection.last3month,
                PeriodSelection.lass6month,
                PeriodSelection.lastyear,
                PeriodSelection.alltime,
            )
            val baseItems = listOf("30 дней", "3 месяца", "6 месяцев", "1 год", "Всё время")

            val metricsList = listOf(
                "Разовый максимум", "Объём тренировок", "Макс. вес", "Макс. число повторов",
                "Всего повторений", "Всего подходов"
            )

            val metricsAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, metricsList)

            fullPeriodList = basePeriods + specificPeriodList
            fullSpinnerItems = baseItems + specificSpinnerItems

            withContext(Dispatchers.Main) {
                binding.spinMetric.setAdapter(metricsAdapter)
                binding.spinMetric.setText(metricsList.first(), false)

                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, fullSpinnerItems)
                binding.spinperiod.setAdapter(adapter)

                binding.btnbackperiod.setOnClickListener {
                    if (currentPeriodIndex > 0) {
                        currentPeriodIndex--
                        updateSelection(exerciseId)
                    }
                }

                binding.btnnextperiod.setOnClickListener {
                    if (currentPeriodIndex < fullPeriodList.size - 1) {
                        currentPeriodIndex++
                        updateSelection(exerciseId)
                    }
                }

                binding.spinperiod.setOnItemClickListener { _, _, position, _ ->
                    currentPeriodIndex = position
                    updateSelection(exerciseId)
                }

                // Устанавливаем слушатель для переключателя графиков
                binding.chartButton.setOnClickListener {
                    val selectedMetric = binding.spinMetric.text.toString()
                    isLineChartVisible = !isLineChartVisible

                    if (isLineChartVisible) {
                        binding.barChartStats.visibility = View.GONE
                        binding.lineChartStats.visibility = View.VISIBLE
                        updateLineChart(currentFilteredSets, selectedMetric, currentTrainingDatesMap)
                    } else {
                        binding.barChartStats.visibility = View.VISIBLE
                        binding.lineChartStats.visibility = View.GONE
                        updateBarChart(currentFilteredSets, selectedMetric, currentTrainingDatesMap)
                    }
                }

                binding.spinMetric.setOnItemClickListener { _, _, _, _ ->
                    val selected = binding.spinMetric.text.toString()
                    if (isLineChartVisible) {
                        updateLineChart(currentFilteredSets, selected, currentTrainingDatesMap)
                    } else {
                        updateBarChart(currentFilteredSets, selected, currentTrainingDatesMap)
                    }
                }

                currentPeriodIndex = 0
                updateSelection(exerciseId) // Вызываем первый раз для загрузки данных
            }
        }
    }


    private fun updateSelection(exerciseId: Int) {
        val selectedText = fullSpinnerItems[currentPeriodIndex]
        val selectedPeriod = fullPeriodList[currentPeriodIndex]
        binding.spinperiod.setText(selectedText, false)
        viewModel.setPeriod(selectedPeriod)
        reloadStats(exerciseId, selectedPeriod)
    }

    private fun reloadStats(exerciseId: Int, period: PeriodSelection) {
        CoroutineScope(Dispatchers.IO).launch {
            val stats = repository.calculateStatsForPeriod(exerciseId, period)
            val sets = repository.getSetsForExercises(exerciseId).first()
            val trainings = repository.getAllTrainings().first()

            val now = LocalDate.now()
            val nowMonth = YearMonth.from(now)

            val validTrainingIds = when (period) {
                is PeriodSelection.last30day -> trainings.filter { it.date >= now.minusDays(30) }
                is PeriodSelection.last3month -> (0..2).map { nowMonth.minusMonths(it.toLong()) }
                    .let { months -> trainings.filter { YearMonth.from(it.date) in months } }
                is PeriodSelection.lass6month -> (0..5).map { nowMonth.minusMonths(it.toLong()) }
                    .let { months -> trainings.filter { YearMonth.from(it.date) in months } }
                is PeriodSelection.lastyear -> trainings.filter { it.date >= now.minusYears(1) }
                is PeriodSelection.SpecificMonth -> trainings.filter {
                    it.date.year == period.year && it.date.monthValue == period.month
                }
                is PeriodSelection.alltime -> trainings
            }.map { it.trainingId }

            val filteredSets = sets.filter { it.trainingId in validTrainingIds }
            val groupedByTraining = filteredSets.groupBy { it.trainingId }
            val trainingDatesMap = trainings.associateBy { it.trainingId }

            // Обновляем переменные-члены для графиков
            currentFilteredSets = filteredSets
            currentTrainingDatesMap = trainingDatesMap

            withContext(Dispatchers.Main) {
                val parent = binding.statsGroupParent
                parent.removeAllViews()

                if (filteredSets.isNotEmpty()) {
                    groupedByTraining
                        .toList()
                        .sortedByDescending { trainings.find { t -> t.trainingId == it.first }?.date }
                        .forEach { (trainingId, sets) ->
                            val groupView = layoutInflater.inflate(R.layout.item_stats_group, parent, false)
                            val header = groupView.findViewById<LinearLayout>(R.id.headerContainer)
                            val setsContainer = groupView.findViewById<LinearLayout>(R.id.setsContainer)
                            setsContainer.visibility = View.GONE

                            val training = repository.getTrainingbyId(trainingId)
                            groupView.findViewById<TextView>(R.id.groupTitle).text = training?.name ?: "Нет названия"
                            groupView.findViewById<TextView>(R.id.groupDate).text =
                                training?.date?.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))) ?: "Без даты"

                            sets.forEachIndexed { index, set ->
                                val setView = layoutInflater.inflate(R.layout.item_set_stats, setsContainer, false)
                                setView.findViewById<TextView>(R.id.setNumber).text = "${index + 1}."
                                setView.findViewById<TextView>(R.id.weight).text = "${set.weight ?: 0}"
                                setView.findViewById<TextView>(R.id.reps).text = "${set.reps ?: 0}"
                                setsContainer.addView(setView)
                            }

                            header.setOnClickListener {
                                setsContainer.visibility = if (setsContainer.visibility == View.VISIBLE)
                                    View.GONE else View.VISIBLE
                            }

                            parent.addView(groupView)
                        }
                } else {
                    val noDataTextView = TextView(requireContext()).apply {
                        text = "Нет данных за выбранный период"
                        gravity = Gravity.CENTER
                        setPadding(0, 50, 0, 50)
                    }
                    parent.addView(noDataTextView)
                    binding.recyclerStats.adapter = StatsAdapter(emptyList())
                }

                // Отображение верхней мета-инфы
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))
                val maxVolumeDateStr = stats.maxWorkoutVolumeDate.takeIf { it != LocalDateTime.MIN }?.format(formatter)
                val oneRepMaxDateStr = stats.oneRepMaxDate.takeIf { it != LocalDateTime.MIN }?.format(formatter)
                val oneRepFormatted = String.format(Locale.US, "%.2f", stats.estimatedOneRepMax)

                val statsItems = listOf(
                    StatsItems("РАСЧЕТНЫЙ РАЗОВЫЙ МАКСИМУМ", oneRepFormatted, " кг", oneRepMaxDateStr),
                    StatsItems("МАКС. ОБЪЁМ ТРЕНИРОВКИ", stats.maxWorkoutVolume.toString(), " кг", maxVolumeDateStr),
                    StatsItems("МАКС. ВЕС", stats.maxWeight.toString(), " кг"),
                    StatsItems("МАКС. ПОВТОРЕНИЙ", stats.maxResp.toString()),
                    StatsItems("ВСЕГО ПОВТОРЕНИЙ", stats.totalResp.toString()),
                    StatsItems("ВСЕГО ПОДХОДОВ", stats.totalSets.toString()),
                    StatsItems("ОБЩИЙ ВЕС", stats.totalWeight.toString(), " кг"),
                    StatsItems("ВСЕГО ТРЕНИРОВОК", stats.totalTrainings.toString())
                )

                binding.recyclerStats.apply {
                    layoutManager = GridLayoutManager(requireContext(), 2)
                    adapter = StatsAdapter(statsItems)
                }

                // Обновление графика в зависимости от isLineChartVisible
                val selectedMetric = binding.spinMetric.text.toString()
                if (isLineChartVisible) {
                    updateLineChart(currentFilteredSets, selectedMetric, currentTrainingDatesMap)
                } else {
                    updateBarChart(currentFilteredSets, selectedMetric, currentTrainingDatesMap)
                }
            }
        }
    }


    fun updateBarChart(
        sets: List<SetEntity>,
        metric: String,
        trainingDatesMap: Map<Int, TrainingsEntity>
    ) {
        if (sets.isEmpty()) {
            binding.barChartStats.clear()
            binding.barChartStats.invalidate()
            return
        }

        val grouped = sets
            .groupBy { trainingDatesMap[it.trainingId]?.date }
            .filterKeys { it != null }
            .toSortedMap(compareBy { it }) // явное указание компаратора по дате

        val dateLabels = grouped.keys.map { it!! } // Список только тех дат, что есть
        val entries = grouped.entries.mapIndexed { index, (_, daySets) ->
            val y = when (metric) {
                "Разовый максимум" -> daySets.maxOfOrNull {
                    val reps = it.reps ?: 0
                    val weight = it.weight ?: 0
                    weight * (1 + reps / 30f)
                } ?: 0f
                "Объём тренировок" -> daySets.sumOf { (it.weight ?: 0) * (it.reps ?: 0) }.toFloat()
                "Макс. вес" -> daySets.maxOfOrNull { it.weight ?: 0 }?.toFloat() ?: 0f
                "Макс. число повторов" -> daySets.maxOfOrNull { it.reps ?: 0 }?.toFloat() ?: 0f
                "Всего повторений" -> daySets.sumOf { it.reps ?: 0 }.toFloat()
                "Всего подходов" -> daySets.size.toFloat()
                else -> 0f
            }
            BarEntry(index.toFloat(), y)
        }

        val dataSet = BarDataSet(entries, metric).apply {
            color = Color.parseColor("#2196F3")
            valueTextColor = Color.WHITE // Цвет значений
            valueTextSize = 10f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.2f
        }

        binding.barChartStats.apply {
            data = barData
            setFitBars(true)
            animateY(800)

            description.isEnabled = false
            legend.isEnabled = false
            setScaleEnabled(false)
            setTouchEnabled(true)
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.WHITE // Цвет меток
                labelRotationAngle = 0f // Поворот для лучшей читаемости
                axisMinimum = -0.5f
                axisMaximum = (entries.size - 1) + 0.5f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < dateLabels.size) { // Проверка границ
                            dateLabels[index].format(DateTimeFormatter.ofPattern("d.MM"))
                        } else ""
                    }
                }
                labelCount = if (dateLabels.size > 7) 7 else dateLabels.size // Ограничить кол-во меток
            }

            axisLeft.apply {
                textColor = Color.WHITE // Цвет оси Y
                setDrawGridLines(true) // Можно включить сетку
                gridColor = Color.GRAY // Цвет сетки
                axisMinimum = 0f // Начинать с нуля
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (metric) {
                            "Разовый максимум", "Объём тренировок", "Макс. вес" -> "${value.toInt()} кг"
                            else -> value.toInt().toString()
                        }
                    }
                }
            }

            invalidate()
        }
    }
    fun updateLineChart(
        sets: List<SetEntity>,
        metric: String,
        trainingDatesMap: Map<Int, TrainingsEntity>
    ) {
        if (sets.isEmpty()) {
            binding.lineChartStats.clear()
            binding.lineChartStats.invalidate()
            return
        }

        val grouped = sets
            .groupBy { trainingDatesMap[it.trainingId]?.date }
            .filterKeys { it != null }
            .toSortedMap(compareBy { it })

        val dateLabels = grouped.keys.map { it!! }
        val entries = grouped.entries.mapIndexed { index, (_, daySets) ->
            val y = when (metric) {
                "Разовый максимум" -> daySets.maxOfOrNull {
                    val reps = it.reps ?: 0
                    val weight = it.weight ?: 0
                    weight * (1 + reps / 30f)
                } ?: 0f
                "Объём тренировок" -> daySets.sumOf { (it.weight ?: 0) * (it.reps ?: 0) }.toFloat()
                "Макс. вес" -> daySets.maxOfOrNull { it.weight ?: 0 }?.toFloat() ?: 0f
                "Макс. число повторов" -> daySets.maxOfOrNull { it.reps ?: 0 }?.toFloat() ?: 0f
                "Всего повторений" -> daySets.sumOf { it.reps ?: 0 }.toFloat()
                "Всего подходов" -> daySets.size.toFloat()
                else -> 0f
            }
            Entry(index.toFloat(), y)
        }

        val dataSet = LineDataSet(entries, metric).apply {
            color = Color.parseColor("#2196F3")
            valueTextColor = Color.WHITE // Цвет значений
            valueTextSize = 10f
            setCircleColor(Color.parseColor("#2196F3"))
            setDrawFilled(true)
            fillColor = Color.parseColor("#90CAF9") // Цвет заливки
            fillAlpha = 60
            mode = LineDataSet.Mode.CUBIC_BEZIER // Сглаживание линии
        }

        val lineData = LineData(dataSet)

        binding.lineChartStats.apply {
            data = lineData
            animateY(800)
            description.isEnabled = false
            legend.isEnabled = false
            setScaleEnabled(false)
            setTouchEnabled(true)
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.WHITE // Цвет меток
                labelRotationAngle = 0f // Поворот
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < dateLabels.size) { // Проверка границ
                            dateLabels[index].format(DateTimeFormatter.ofPattern("d.MM"))
                        } else ""
                    }
                }
                labelCount = if (dateLabels.size > 7) 7 else dateLabels.size // Ограничить кол-во меток
            }

            axisLeft.apply {
                textColor = Color.WHITE // Цвет оси Y
                setDrawGridLines(true) // Можно включить сетку
                gridColor = Color.GRAY // Цвет сетки
                axisMinimum = 0f // Начинать с нуля
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (metric) {
                            "Разовый максимум", "Объём тренировок", "Макс. вес" -> "${value.toInt()} кг"
                            else -> value.toInt().toString()
                        }
                    }
                }
            }

            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}