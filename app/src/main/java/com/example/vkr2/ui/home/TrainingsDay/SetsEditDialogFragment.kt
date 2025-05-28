package com.example.vkr2.ui.home.TrainingsDay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.DialogAddSetBinding
import com.example.vkr2.ui.AdaptersDirectory.SetsAdapter
import com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise.ExerciseDetailFragment

class SetsEditDialogFragment(
    private val trainingId: Int,
    private val exerciseId: Int,
    private val exerciseName: String,
    private val exerciseImagePath: String,
    private val exerciseType: ExerciseType, // Добавляем тип упражнения
    private val sets: MutableList<SetEntity>,
    private val onAddSet: (SetEntity) -> Unit,
    private val onUpdateSet: (SetEntity) -> Unit,
    private val onDeleteSet: (SetEntity) -> Unit,
    ) : DialogFragment() {

    private var _binding: DialogAddSetBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SetsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.exerciseTitle.text = exerciseName

        val context = requireContext()
        val resId = context.resources.getIdentifier(
            exerciseImagePath.substringBeforeLast('.'),"drawable", context.packageName
        )
        if (resId !=0){
            Glide.with(context)
                .load(resId)
                .into(binding.exercisePictures)
        }
        else{
            binding.exercisePictures.setImageResource(R.drawable.biceps)
        }

        if (sets.isEmpty()) {
            val dummy = createNewSet(0) // Создаем dummy с учетом типа
            sets.add(dummy)
            onAddSet(dummy)
        }
        adapter = SetsAdapter(
            sets = sets,
            exerciseType = exerciseType, // Передаем тип в адаптер
            onUpdateSet = { updatedSet -> onUpdateSet(updatedSet) },
            onDeleteSet = { removed ->
                sets.remove(removed)
                onDeleteSet(removed)
                if (sets.isEmpty()) {
                    val fallback = createNewSet(0) // Создаем dummy с учетом типа
                    sets.add(fallback)
                    onAddSet(fallback)
                }
            }
        )

        binding.setsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.setsRecyclerView.adapter = adapter

        binding.buttonAddSet.setOnClickListener {
            val newSet = createNewSet(sets.size) // Создаем новый сет с учетом типа
            adapter.addSet(newSet)
            onAddSet(newSet)
        }
        binding.buttonCheckSets.setOnClickListener {
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            binding.setsRecyclerView.clearFocus()
            binding.root.clearFocus()

            binding.setsRecyclerView.postDelayed({
                val updatedSets = adapter.getCurrentSets()
                    .filter { isSetValid(it) } // Используем новую функцию фильтрации

                updatedSets.forEachIndexed { index, set ->
                    set.exerciseOrder = index
                    onUpdateSet(set)
                }

                // Удаляем невалидные сеты, если нужно (кроме последнего пустого)
                val setsToDelete = adapter.getCurrentSets().filterNot { isSetValid(it) }
                setsToDelete.forEach { onDeleteSet(it) }


                dismiss()
            }, 100)
        }


        binding.closeButton.setOnClickListener(){
            dismiss()
        }

        binding.exercisePicturesContainer.setOnClickListener {
            val exerciseDetailDialog = ExerciseDetailFragment.newInstance(exerciseId)
            exerciseDetailDialog.show(parentFragmentManager, "ExerciseDetailDialog")
        }
        setupBlur()
    }

    private fun setupBlur() {
        val parentView = requireParentFragment().requireView()
        val windowBackground = parentView.background

        binding.blurView.setupWith(parentView as ViewGroup)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(1f)
            .setBlurAutoUpdate(true)
            .setOverlayColor(ContextCompat.getColor(requireContext(), R.color.blur_overlay))
    }

    private fun createNewSet(order: Int): SetEntity {
        return SetEntity(
            trainingId = trainingId,
            exerciseId = exerciseId,
            reps = if (exerciseType != ExerciseType.CARDIO_DISTANCE) 0 else null,
            weight = if (exerciseType == ExerciseType.STRENGTH) 0 else null,
            exerciseOrder = order,
            minutes = if (exerciseType != ExerciseType.STRENGTH) 0 else null,
            seconds = if (exerciseType != ExerciseType.STRENGTH) 0 else null,
            distanceKm = if (exerciseType == ExerciseType.CARDIO_DISTANCE) 0f else null
        )
    }
    private fun isSetValid(set: SetEntity): Boolean {
        return when (exerciseType) {
            ExerciseType.STRENGTH -> (set.weight ?: 0) != 0 || (set.reps ?: 0) != 0
            ExerciseType.CARDIO_DISTANCE -> (set.minutes ?: 0) != 0 || (set.seconds ?: 0) != 0 || (set.distanceKm ?: 0f) != 0f
            ExerciseType.CARDIO_TIME_REPS -> (set.minutes ?: 0) != 0 || (set.seconds ?: 0) != 0 || (set.reps ?: 0) != 0
        }
    }

    override fun onStart() {
        super.onStart()
            dialog?.window?.apply {
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onStop() {
        super.onStop()
        if (adapter.getCurrentSets().none { isSetValid(it) } && adapter.getCurrentSets().isEmpty()) {
            val dummySet = createNewSet(0)
            onAddSet(dummySet)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

