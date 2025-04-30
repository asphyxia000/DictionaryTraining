package com.example.vkr2.ui.Notification_muscle_groups

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.R
import com.example.vkr2.SharedSelection
import com.example.vkr2.databinding.FragmentNotificationsBinding
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.ExerciseDialogListFragment

class NotificationsDialogFragment : DialogFragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels {
        NotificationListViewModelFactory(requireContext())
    }
    private val sharedViewModel: SharedSelection by activityViewModels()

    private lateinit var adapter: NoExpAdapter

    private var trainingId: Int = -1

    companion object {
        fun newInstance(trainingId: Int): NotificationsDialogFragment {
            val fragment = NotificationsDialogFragment()
            fragment.arguments = bundleOf("trainingId" to trainingId)
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.ScreenDialog)
        trainingId = arguments?.getInt("trainingId") ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dialogToolbar.apply {
            navigationIcon = null
            title = null

            // Основной контейнер ConstraintLayout
            val constraintLayout = ConstraintLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Контейнер для кнопки "Назад"
            val backContainer = FrameLayout(context).apply {
                id = View.generateViewId()
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    startToStart = ConstraintSet.PARENT_ID
                    topToTop = ConstraintSet.PARENT_ID
                    bottomToBottom = ConstraintSet.PARENT_ID
                }
                setPadding(24, 0, 24, 0)
                setOnClickListener { dismiss() }
            }

            // Кнопка "Назад"
            val backButton = AppCompatTextView(context).apply {
                id = View.generateViewId()
                text = "Назад"
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.blue_500))
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL
                )
            }

            // Заголовок
            val titleTextView = AppCompatTextView(context).apply {
                id = View.generateViewId()
                text = "Упражнения"
                textSize = 22f
                setTextAppearance(context, R.style.ToolbarTitleBold) // Используем setTextAppearance
                setTextColor(ContextCompat.getColor(context, R.color.textforexp1))
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER
                maxLines = 1
            }

            // Собираем иерархию
            backContainer.addView(backButton)
            constraintLayout.addView(backContainer)
            constraintLayout.addView(titleTextView)

            // Настройка ConstraintSet
            val constraintSet = ConstraintSet().apply {
                clone(constraintLayout)

                // Позиционирование заголовка
                constrainWidth(titleTextView.id, ConstraintSet.WRAP_CONTENT)
                constrainHeight(titleTextView.id, ConstraintSet.WRAP_CONTENT)
                centerHorizontally(titleTextView.id, ConstraintSet.PARENT_ID)
                centerVertically(titleTextView.id, ConstraintSet.PARENT_ID)

                // Позиционирование кнопки "Назад"
                connect(
                    backButton.id,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
                connect(
                    backButton.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                connect(
                    backButton.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )

                applyTo(constraintLayout)
            }

            // Очистка и добавление кастомного вида
            removeAllViews()
            addView(constraintLayout)
            setContentInsetsAbsolute(0, 0)
            setContentInsetsRelative(0, 0)
        }

        setupAdapter()
        setupObservers()
        setupButtons()
    }

    private fun setupAdapter() {
        adapter = NoExpAdapter(emptyList()) { muscleGroup ->
            val bundle = bundleOf(
                "selectedGroupID" to muscleGroup.MuscleGroupsID,
                "selectedGroupName" to muscleGroup.NameMuscleGroups,
                "trainingId" to trainingId
            )
            val dialog = ExerciseDialogListFragment.newInstance(bundle)
            dialog.show(childFragmentManager, "ExerciseListDialog")
        }
        binding.view3.layoutManager = LinearLayoutManager(requireContext())
        binding.view3.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.text.observe(viewLifecycleOwner) {
            binding.textforexp1.text = "Выберите группу"
            binding.textViewdesc.text = "Для добавления упражнений"
        }
        viewModel.muscleGroups.observe(viewLifecycleOwner) { groups ->
            adapter.updateData(groups)
        }
    }

//    private fun setupCustomToolbar(title:String){
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

    private fun setupButtons() {
        binding.cancelBtn.setOnClickListener {
            sharedViewModel.clearSelection()
            dismiss()
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
        binding.buttonGroup.visibility = View.VISIBLE
        binding.btnAdd.alpha = 0f
        binding.btnAdd.translationX = 50f
        binding.btnAdd.animate().translationX(0f).alpha(1f).setDuration(200).start()
    }

    private fun hideAddBtn() {
        binding.buttonGroup.animate().alpha(0f).setDuration(200).withEndAction {
            binding.buttonGroup.visibility = View.GONE
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


    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
