package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentExerciseDetailBinding
import com.example.vkr2.repository.InfoStatsRepositoryImpl
import com.example.vkr2.ui.AdaptersDirectory.ExerciseDetailPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ExerciseDetailFragment: DialogFragment() {
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
        super.onViewCreated(view, savedInstanceState)

        val repository = InfoStatsRepositoryImpl(requireContext().applicationContext, CoroutineScope(Dispatchers.IO))
        val factory = ExercisesDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ExerciseDetailViewModel::class.java]

        val constraintLayout = androidx.constraintlayout.widget.ConstraintLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val backContainer = FrameLayout(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                startToStart = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }
            setPadding(24, 0, 24, 0) // Увеличить паддинг контейнера
            setOnClickListener {
                safeClose()
            }
        }

        val backButton = AppCompatTextView(requireContext()).apply {
            id = View.generateViewId()
            text = "Назад"
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL
            )
        }


        // Заголовок по центру Toolbar
        val titleTextView = AppCompatTextView(requireContext()).apply {
            id = View.generateViewId()
            textSize = 22f
            setTextColor(ContextCompat.getColor(requireContext(),R.color.textforexp1))
            layoutParams = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintSet.PARENT_ID
                endToEnd = ConstraintSet.PARENT_ID
                topToTop = ConstraintSet.PARENT_ID
                bottomToBottom = ConstraintSet.PARENT_ID
            }
            maxLines = 1
            gravity = Gravity.CENTER
        }

        backContainer.addView(backButton)
        constraintLayout.addView(backContainer)
        constraintLayout.addView(titleTextView)

        // Применяем ConstraintSet для правильного позиционирования
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        // Обеспечиваем, чтобы заголовок был по центру, а кнопка "Назад" не мешала
        constraintSet.constrainWidth(titleTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(titleTextView.id, ConstraintSet.WRAP_CONTENT)
        constraintSet.centerHorizontally(titleTextView.id, ConstraintSet.PARENT_ID)
        constraintSet.centerVertically(titleTextView.id, ConstraintSet.PARENT_ID)

        // Кнопка "Назад" остается слева
        constraintSet.connect(backButton.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(backButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(backButton.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        // Применяем настройки
        constraintSet.applyTo(constraintLayout)

        binding.exerciseToolbar.apply {
            navigationIcon = null
            addView(constraintLayout)
            setContentInsetsAbsolute(0, 0)
            setContentInsetsRelative(0, 0)
        }

        viewModel.loadExerciseName(exerciseId)
        viewModel.exercisesEntity.observe(viewLifecycleOwner) { info ->
            info?.let {
                titleTextView.text = it.ExercisesName
            }
        }

        // Остальной код без изменений
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                safeClose()
            }
        })

        val adapter = ExerciseDetailPagerAdapter(this, exerciseId)
        binding.viewPager.adapter = adapter

        val tabTitles = listOf("Информация", "Статистика")
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitles[0]))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitles[1]))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab?.position ?: 0
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawableResource(android.R.color.black)
            setGravity(Gravity.BOTTOM)

            // Убираем отступ сверху
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        )
            }
        }

        // --- Скрываем системный Toolbar, только здесь! ---
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    private fun safeClose (){
        if (dialog != null && dialog?.isShowing == true){
            dismiss()
        }
        else{
            if (findNavController().currentBackStackEntry != null){
                findNavController().popBackStack()
            }else{
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // --- ВОЗВРАЩАЕМ Toolbar обратно ---
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

}