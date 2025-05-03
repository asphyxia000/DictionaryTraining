package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseStats
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentStatsBinding
import com.example.vkr2.repository.InfoStatsRepository
import com.example.vkr2.repository.InfoStatsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExerciseDetailViewModel

    companion object {
        fun newInstance(exerciseId: Int): Fragment {
            val fragment = StatsFragment()
            fragment.arguments = Bundle().apply {
                putInt("exerciseId",exerciseId)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatsBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val exerciseId = requireArguments().getInt("exerciseId")
        val trainingId = requireArguments().getInt("trainingId")

        val repository = InfoStatsRepositoryImpl(requireContext().applicationContext, CoroutineScope(Dispatchers.IO))
        val factory = ExercisesDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ExerciseDetailViewModel::class.java]

        CoroutineScope(Dispatchers.IO).launch {
            repository.recalculate(exerciseId)

            // ✅ получаем подходы по ВСЕМ тренировочным дням
            val allSets = repository.getSetsForExercises(exerciseId).first()

            // ✅ группируем по тренировочным дням
            val groupedByTraining = allSets.groupBy { it.trainingId }

            withContext(Dispatchers.Main) {
                val parent = binding.statsGroupParent
                viewModel.loadExercisesStats(exerciseId)

                groupedByTraining.forEach { (trainingId, sets) ->
                    val groupView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_stats_group, parent, false)

                    val header = groupView.findViewById<LinearLayout>(R.id.headerContainer)
                    val setsContainer = groupView.findViewById<LinearLayout>(R.id.setsContainer)

                    // 🕓 Загружаем название и дату тренировки
                    val training = repository.getTrainingbyId(trainingId)
                    groupView.findViewById<TextView>(R.id.groupTitle).text = training?.name ?: "Нет названия"
                    groupView.findViewById<TextView>(R.id.groupDate).text =
                        training?.createdAt?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "Без даты"

                    // 🏋️ Добавляем подходы
                    sets.forEachIndexed { index, set ->
                        val setView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_set_stats, setsContainer, false)

                        setView.findViewById<TextView>(R.id.setNumber).text = "${index + 1}."
                        setView.findViewById<TextView>(R.id.weight).text = "${set.weight ?: 0}"
                        setView.findViewById<TextView>(R.id.reps).text = "${set.reps ?: 0}"

                        setsContainer.addView(setView)
                    }

                    // 📦 Сворачивание/разворачивание
                    header.setOnClickListener {
                        setsContainer.visibility = if (setsContainer.visibility == View.VISIBLE)
                            View.GONE else View.VISIBLE
                    }

                    parent.addView(groupView)
                }
            }
        }


        viewModel.exerciseStats.observe(viewLifecycleOwner) { stats ->
            val safeStats = stats ?: ExerciseStats(
                statid = 0,
                exerciseId = exerciseId,
                totalSets = 0,
                totalResp = 0,
                totalWeight = 0,
                totalTrainings = 0,
                maxWeight = 0,
                maxResp = 0,
                maxWorkoutVolume = 0,
                maxWorkoutVolumeDate = LocalDateTime.MIN,
                estimatedOneRepMax = 0.0,
                oneRepMaxDate = LocalDateTime.MIN
            )

            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))
            val maxVolumeDateStr = if (safeStats.maxWorkoutVolumeDate != LocalDateTime.MIN)
                safeStats.maxWorkoutVolumeDate.format(formatter)
            else null
            val oneRepMaxDateStr = if (safeStats.oneRepMaxDate != LocalDateTime.MIN)
                safeStats.oneRepMaxDate.format(formatter)
            else null
            val oneRepFormatted = String.format("%.2f",safeStats.estimatedOneRepMax)
            val statsItems = listOf(
                StatsItems("РАССЧЕТНЫЙ РАЗОВЫЙ МАКСИМУМ", oneRepFormatted, " кг", oneRepMaxDateStr),
                StatsItems("МАКС. ОБЪЁМ ТРЕНИРОВКИ", safeStats.maxWorkoutVolume.toString(), " кг", maxVolumeDateStr),
                StatsItems("МАКС. ВЕС", safeStats.maxWeight.toString(), " кг"),
                StatsItems("МАКС. ПОВТОРЕНИЙ", safeStats.maxResp.toString()),
                StatsItems("ВСЕГО ПОВТОРЕНИЙ", safeStats.totalResp.toString()),
                StatsItems("ВСЕГО ПОДХОДОВ", safeStats.totalSets.toString()),
                StatsItems("ОБЩИЙ ВЕС", safeStats.totalWeight.toString(), " кг"),
                StatsItems("ВСЕГО ТРЕНИРОВОК", safeStats.totalTrainings.toString())
            )

            binding.recyclerStats.apply {
                layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
                adapter = StatsAdapter(statsItems)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}