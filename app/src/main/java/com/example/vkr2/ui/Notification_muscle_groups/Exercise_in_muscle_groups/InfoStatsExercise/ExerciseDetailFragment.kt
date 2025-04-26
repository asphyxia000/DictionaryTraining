package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentExerciseDetailBinding
import com.example.vkr2.repository.InfoStatsRepositoryImpl
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ExerciseDetailFragment:Fragment() {
    private var _binding:FragmentExerciseDetailBinding?=null
    private val binding get() = _binding!!
    private lateinit var viewModel: ExerciseDetailViewModel
    private var exerciseId: Int = -1

    companion object{
        fun newInstance(exerciseId: Int):ExerciseDetailFragment{
            val fragment = ExerciseDetailFragment()
            fragment.arguments = Bundle().apply {
                putInt("exerciseId",exerciseId)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseId = arguments?.getInt("exerciseId")?:-1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val repository = InfoStatsRepositoryImpl(requireContext().applicationContext, CoroutineScope(Dispatchers.IO))
        val factory = ExercisesDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this,factory)[ExerciseDetailViewModel::class.java]

        viewModel.loadExerciseName(exerciseId)
        viewModel.exercisesEntity.observe(viewLifecycleOwner){info->
            info?.let {
                (requireActivity() as AppCompatActivity).supportActionBar?.title = it.ExercisesName
            }
        }

        (requireActivity()as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        requireActivity().addMenuProvider(object :MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId){
                    android.R.id.home->{
                        findNavController().popBackStack()
                        true
                    }
                    else -> false
                }
            }
        },viewLifecycleOwner, Lifecycle.State.RESUMED)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

        val adapter = ExerciseDetailPagerAdapter(this,exerciseId)
        binding.viewPager.adapter = adapter
        val tabTitles= listOf("Информация","Статистика")
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitles[0]))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitles[1]))

        binding.tabLayout.addOnTabSelectedListener(object:
        TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab?.position?:0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.viewPager.registerOnPageChangeCallback(object:
        ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}