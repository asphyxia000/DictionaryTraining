package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.vkr2.DataBase.Exercises.DetailExercise.ExerciseStats
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentStatsBinding
import com.example.vkr2.repository.InfoStatsRepository
import com.example.vkr2.repository.InfoStatsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

        val repository = InfoStatsRepositoryImpl(requireContext().applicationContext, CoroutineScope(Dispatchers.IO))
        val factory = ExercisesDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ExerciseDetailViewModel::class.java]

        viewModel.loadExercisesStats(exerciseId)

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