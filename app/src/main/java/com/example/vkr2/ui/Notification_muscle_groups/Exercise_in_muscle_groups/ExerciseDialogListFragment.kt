package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.R
import com.example.vkr2.SharedSelection
import com.example.vkr2.databinding.FragmentExerciseListBinding
import com.example.vkr2.repository.ExercisesRepositoryImpl
import com.example.vkr2.repository.TagsRepositoryImpl
import com.example.vkr2.repository.TrainingRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels

class ExerciseDialogListFragment : DialogFragment() {

    private var _binding: FragmentExerciseListBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedSelection by activityViewModels()
    private val viewModel: ExerciseListViewModel by viewModels {
        ExerciseListViewModelFactory(
            ExercisesRepositoryImpl(requireContext(), CoroutineScope(Dispatchers.IO)),
            TagsRepositoryImpl(requireContext(), CoroutineScope(Dispatchers.IO)),
            TrainingRepositoryImpl(requireContext(), CoroutineScope(Dispatchers.IO))
        )
    }

    private var trainingId: Int = -1
    private lateinit var adapter: ExerciseAdapter

    companion object {
        fun newInstance(bundle: Bundle): ExerciseDialogListFragment {
            val fragment = ExerciseDialogListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trainingId = arguments?.getInt("trainingId") ?: -1
        setStyle(STYLE_NORMAL,R.style.ScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExerciseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupName = arguments?.getString("selectedGroupName") ?: "Выбор упражнений"
        val groupID = arguments?.getInt("selectedGroupID") ?: return

        // Настройка кастомного Toolbar
        binding.dialogToolbar.apply {
            // Очищаем стандартные элементы
            title = null
            navigationIcon = null

            // Создаем ConstraintLayout как корневой контейнер
            val toolbarLayout = ConstraintLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Кнопка "Назад"
            val backButton = AppCompatTextView(context).apply {
                id = View.generateViewId()
                text = "Назад"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.blue_500))
                setPadding(24, 0, 24, 0)
                setOnClickListener { dismiss() }
            }

            // Заголовок (берем из groupName)
            val titleView = AppCompatTextView(context).apply {
                id = View.generateViewId()
                text = groupName
                textSize = 20f
                setTextAppearance(context, R.style.ToolbarTitleBold) // Используем setTextAppearance
                setTextColor(ContextCompat.getColor(context, R.color.textforexp1))
                maxLines = 1
            }

            // Добавляем элементы в layout
            toolbarLayout.addView(backButton)
            toolbarLayout.addView(titleView)

            // Настройка ConstraintSet
            ConstraintSet().apply {
                clone(toolbarLayout)

                // Позиционирование кнопки "Назад"
                connect(backButton.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(backButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(backButton.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                // Позиционирование заголовка по центру
                connect(titleView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(titleView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                connect(titleView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(titleView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

                applyTo(toolbarLayout)
            }

            // Очищаем Toolbar и добавляем наш кастомный layout
            removeAllViews()
            addView(toolbarLayout)
            setContentInsetsAbsolute(0, 0)
            setContentInsetsRelative(0, 0)
        }

        // Остальная логика
        viewModel.setGroupId(groupID)
        setupAdapter()
        setupButtons()

        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            adapter.updateData(exercises.sortedBy { it.ExercisesName })
        }
    }

//    private fun setupCustomToolbar(title: String) {
//        val constraintLayout = androidx.constraintlayout.widget.ConstraintLayout(requireContext()).apply {
//            layoutParams = ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT
//            )
//        }
//
//        val backContainer = FrameLayout(requireContext()).apply {
//            id = View.generateViewId()
//            layoutParams = ConstraintLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.MATCH_PARENT
//            ).apply {
//                startToStart = ConstraintSet.PARENT_ID
//                topToTop = ConstraintSet.PARENT_ID
//                bottomToBottom = ConstraintSet.PARENT_ID
//            }
//            setPadding(24, 0, 24, 0)
//            setOnClickListener {
//                dismiss()
//            }
//        }
//
//        val backButton = AppCompatTextView(requireContext()).apply {
//            id = View.generateViewId()
//            text = "Назад"
//            textSize = 14f
//            setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
//            layoutParams = FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                Gravity.CENTER_VERTICAL
//            )
//        }
//
//        val titleTextView = AppCompatTextView(requireContext()).apply {
//            id = View.generateViewId()
//            text = title
//            textSize = 22f
//            setTextColor(ContextCompat.getColor(requireContext(), R.color.textforexp1))
//            layoutParams = ConstraintLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//            gravity = Gravity.CENTER
//            maxLines = 1
//        }
//
//        backContainer.addView(backButton)
//        constraintLayout.addView(backContainer)
//        constraintLayout.addView(titleTextView)
//
//        val constraintSet = ConstraintSet()
//        constraintSet.clone(constraintLayout)
//
//        constraintSet.constrainWidth(titleTextView.id, ConstraintSet.WRAP_CONTENT)
//        constraintSet.constrainHeight(titleTextView.id, ConstraintSet.WRAP_CONTENT)
//        constraintSet.centerHorizontally(titleTextView.id, ConstraintSet.PARENT_ID)
//        constraintSet.centerVertically(titleTextView.id, ConstraintSet.PARENT_ID)
//
//        constraintSet.connect(backButton.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//        constraintSet.connect(backButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//        constraintSet.connect(backButton.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//
//        constraintSet.applyTo(constraintLayout)
//
//        binding.dialogToolbar.apply {
//            visibility = View.VISIBLE
//            navigationIcon = null
//            addView(constraintLayout)
//            setContentInsetsAbsolute(0, 0)
//            setContentInsetsRelative(0, 0)
//        }
//    }

    private fun setupAdapter() {
        adapter = ExerciseAdapter(emptyList()) { exercise, isSelected, _ ->
            sharedViewModel.toggleSelection(exercise)
            updateAddButton()
        }
        binding.recyclerViewExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewExercises.adapter = adapter
    }

    private fun setupButtons() {
        binding.cancelBtn.setOnClickListener {
            sharedViewModel.clearSelection()
            (parentFragment as? DialogFragment)?.dismiss()
            dismiss()
        }

        binding.btnAdd.setOnClickListener {
            val selectedExercises = sharedViewModel.getAllSelected()
            if (selectedExercises.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.addExercisesToExistingTraining(trainingId, selectedExercises)
                    sharedViewModel.clearSelection()
                    (parentFragment as? DialogFragment)?.dismiss()
                    dismiss()
                }
            }
        }
        updateAddButton()
    }

    private fun updateAddButton() {
        val count = sharedViewModel.getSelectedCount()
        if (count > 0) {
            showAddBtn(count)
        } else {
            hideAddBtn()
        }
    }

    private fun showAddBtn(count: Int) {
        binding.btnAdd.text = "Добавить • $count"
        binding.btnAdd.visibility = View.VISIBLE
        binding.btnAdd.alpha = 0f
        binding.btnAdd.translationX = 50f
        binding.btnAdd.animate().translationX(0f).alpha(1f).setDuration(200).start()
    }

    private fun hideAddBtn() {
        binding.btnAdd.animate().translationX(50f).alpha(0f).setDuration(200).withEndAction {
            binding.btnAdd.visibility = View.GONE
        }.start()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val height = dpToPx(810)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
            setGravity(Gravity.BOTTOM)
            setWindowAnimations(R.style.DialogSlideAnimation)
        }
    }


//    private fun dismissWithAnim() {
//        val contentView = dialog?.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)
//        contentView?.animate()
//            ?.translationY(contentView.height.toFloat())
//            ?.setDuration(300)
//            ?.withEndAction {
//                if (isAdded) dismissAllowingStateLoss()
//            }
//            ?.start()
//    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
