package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseStats
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentStatsBinding
import com.example.vkr2.repository.InfoStatsRepositoryImpl
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
    private val periodlist = listOf(
        PeriodSelection.last30day,
        PeriodSelection.last3month,
        PeriodSelection.lass6month,
        PeriodSelection.lastyear,
        PeriodSelection.alltime,
    )

    private val spinnerItems = listOf("30 дней", "3 месяца", "6 месяцев", "1 год", "Всё время")

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

//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.spinperiod.adapter = adapter
//        binding.spinperiod.setSelection(currentPeriodIndex)

        binding.btnbackperiod.setOnClickListener {
            if (currentPeriodIndex > 0) {
                currentPeriodIndex--
                binding.spinperiod.setText(spinnerItems[currentPeriodIndex], false)
                val period = periodlist[currentPeriodIndex]
                viewModel.setPeriod(period)
                reloadStats(exerciseId, period)
            }
        }

        binding.btnnextperiod.setOnClickListener {
            if (currentPeriodIndex < periodlist.size - 1) {
                currentPeriodIndex++
                binding.spinperiod.setText(spinnerItems[currentPeriodIndex], false)
                val period = periodlist[currentPeriodIndex]
                viewModel.setPeriod(period)
                reloadStats(exerciseId, period)
            }
        }


        val items = listOf("30 дней", "3 месяца", "6 месяцев", "1 год", "Всё время")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, items)
        binding.spinperiod.setAdapter(adapter)

        binding.spinperiod.setOnItemClickListener { _, _, position, _ ->
            currentPeriodIndex = position
            val period = periodlist.getOrElse(position) { PeriodSelection.alltime }
            viewModel.setPeriod(period)
            reloadStats(exerciseId, period)
        }

        binding.spinperiod.setText(items[currentPeriodIndex], false)
        val initialPeriod = periodlist[currentPeriodIndex]
        viewModel.setPeriod(initialPeriod)
        reloadStats(exerciseId, initialPeriod)

    }

    private fun reloadStats(exerciseId: Int, period: PeriodSelection) {
        CoroutineScope(Dispatchers.IO).launch {
            val stats = repository.calculateStatsForPeriod(exerciseId, period)
            val sets = repository.getSetsForExercises(exerciseId).first()
            val trainings = repository.getAllTrainings().first()

            val now = LocalDate.now()
            val nowMonth = YearMonth.from(now)

            val validTrainingIds = when (period) {
                is PeriodSelection.last30day -> {
                    val start = now.withDayOfMonth(1) // с начала месяца
                    trainings.filter { it.date >= start }
                }
                is PeriodSelection.last3month -> {
                    val targetMonths = (0..2).map { nowMonth.minusMonths(it.toLong()) }
                    trainings.filter { training ->
                        val trainingMonth = YearMonth.from(training.date)
                        trainingMonth in targetMonths
                    }
                }
                is PeriodSelection.lass6month -> {
                    val targetMonths = (0..5).map { nowMonth.minusMonths(it.toLong()) }
                    trainings.filter { training ->
                        val trainingMonth = YearMonth.from(training.date)
                        trainingMonth in targetMonths
                    }
                }
                is PeriodSelection.lastyear -> {
                    val oneYearAgo = now.minusYears(1)
                    trainings.filter { it.date >= oneYearAgo }
                }
                is PeriodSelection.SpecificMonth -> {
                    trainings.filter {
                        it.date.year == period.year && it.date.monthValue == period.month
                    }
                }
                is PeriodSelection.alltime -> trainings
            }.map { it.trainingId }


            val filteredSets = sets.filter { it.trainingId in validTrainingIds }
            val groupedByTraining = filteredSets.groupBy { it.trainingId }

            withContext(Dispatchers.Main) {
                val parent = binding.statsGroupParent
                parent.removeAllViews()

                // Показать карточки с подходами
                if (filteredSets.isNotEmpty()) {
                    groupedByTraining
                        .toList()
                        .sortedByDescending  { pair ->
                            val training = trainings.find { it.trainingId == pair.first }
                            training?.date
                        }
                        .forEach { (trainingId, sets) ->
                        val groupView = layoutInflater.inflate(R.layout.item_stats_group, parent, false)
                        val header = groupView.findViewById<LinearLayout>(R.id.headerContainer)
                        val setsContainer = groupView.findViewById<LinearLayout>(R.id.setsContainer)
                        setsContainer.visibility = View.GONE

                        val training = repository.getTrainingbyId(trainingId)
                        groupView.findViewById<TextView>(R.id.groupTitle).text = training?.name ?: "Нет названия"
                        groupView.findViewById<TextView>(R.id.groupDate).text =
                            training?.date?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "Без даты"

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
                    // Очищаем и показываем пустую статистику
                    binding.recyclerStats.adapter = StatsAdapter(emptyList())
                }

                // Загружаем статистику
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))
                val maxVolumeDateStr = if (stats.maxWorkoutVolumeDate != LocalDateTime.MIN)
                    stats.maxWorkoutVolumeDate.format(formatter) else null
                val oneRepMaxDateStr = if (stats.oneRepMaxDate != LocalDateTime.MIN)
                    stats.oneRepMaxDate.format(formatter) else null
                val oneRepFormatted = String.format("%.2f", stats.estimatedOneRepMax)

                val statsItems = listOf(
                    StatsItems("РАССЧЕТНЫЙ РАЗОВЫЙ МАКСИМУМ", oneRepFormatted, " кг", oneRepMaxDateStr),
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
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
