package com.example.vkr2.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentDashboardBinding
import com.example.vkr2.repository.BodyMeasurementsRepositoryImpl
import com.example.vkr2.repository.GeneralTrainingStatsRepositoryImpl
import com.example.vkr2.ui.AdaptersDirectory.DashboardAdapter
import com.example.vkr2.ui.AdaptersDirectory.ZamersAdapter
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.PeriodSelection
import kotlinx.coroutines.flow.first

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var selectedPeriod: PeriodSelection = PeriodSelection.alltime
    private lateinit var viewModel: DashboardViewModel
    private lateinit var zamerAdapter: ZamersAdapter
    private lateinit var statsAdapter: DashboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        // Получаем контекст и скоуп для создания репозиториев
        val context = requireContext().applicationContext
        val scope = viewLifecycleOwner.lifecycleScope

        val factory = DashboardViewModelFactory(
            generalTrainingStatsRepository = GeneralTrainingStatsRepositoryImpl(context, scope),
            bodyMeasurementsRepository = BodyMeasurementsRepositoryImpl(context, scope)
        )

        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        zamerAdapter = ZamersAdapter(emptyList()) { bodyPart, isLeft ->
            BSDZamers.newInstance(bodyPart, isLeft).show(parentFragmentManager, "zamer_dialog")
        }

        statsAdapter = DashboardAdapter(emptyList())
        // Настройка LayoutManager для замеров и статистики
        binding.view3.layoutManager = LinearLayoutManager(requireContext())
        binding.viewall.layoutManager = LinearLayoutManager(requireContext())
        binding.view3.adapter = zamerAdapter
        binding.viewall.adapter = statsAdapter

        collectData()
        setupPeriodSelector()
        collectStatsForPeriod()

        return binding.root
    }


    private fun collectData() {
        lifecycleScope.launchWhenStarted {
            viewModel.getBodyMeasurementsUI().collect {
                zamerAdapter.updateData(it)
            }
        }
    }

    private fun setupPeriodSelector(){
        val items= listOf(
            "30 дней" to PeriodSelection.last30day,
            "3 месяца" to PeriodSelection.last3month,
            "6 месяцев" to PeriodSelection.lass6month,
            "Год" to PeriodSelection.lastyear,
            "Всё время" to PeriodSelection.alltime
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, items.map { it.first })
        binding.spinperiod.setAdapter(adapter)

        binding.spinperiod.setOnItemClickListener(){_, _, position, _->
            selectedPeriod = items[position].second
            collectStatsForPeriod()
        }
        binding.spinperiod.setText("Всё время", false)
    }

    private fun collectStatsForPeriod(){
        lifecycleScope.launchWhenStarted {
            val stats=viewModel.getGeneralStatsUI(selectedPeriod).first()
            statsAdapter.updateData(stats)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
