package com.example.vkr2.ui.home.TrainingsDay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.DialogAddSetBinding

class SetsEditDialogFragment(
    private val trainingId:Int,
    private val exerciseId: Int,
    private val exerciseName: String,
    private val sets: MutableList<SetEntity>,
    private val onAddSet: (SetEntity) -> Unit,
    private val onUpdateSet: (SetEntity) -> Unit,
    private val onDeleteSet: (SetEntity)->Unit,
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

        if (sets.isEmpty()) {
            val validTrainingId = sets.firstOrNull()?.trainingId ?: trainingId
            if (validTrainingId != null) {
                val dummy = SetEntity(
                    trainingId = validTrainingId,
                    exerciseId = exerciseId,
                    reps = 0,
                    weight = 0,
                    duration = null,
                    exerciseOrder = 0
                )
                sets.add(dummy)
                onAddSet(dummy) // добавляем в БД
            }
        }

        adapter = SetsAdapter(
            sets = sets,
            onUpdateSet = { updatedSet -> onUpdateSet(updatedSet) },
            onDeleteSet = { removed ->
                sets.remove(removed)
                onDeleteSet(removed)
                // если удалили последний — создаём dummy
                if (sets.isEmpty()) {
                    val fallback = SetEntity(
                        trainingId = removed.trainingId,
                        exerciseId = removed.exerciseId,
                        reps = 0,
                        weight = 0,
                        duration = null,
                        exerciseOrder = 0
                    )
                    sets.add(fallback)
                    onAddSet(fallback)
                }
            }
        )

        binding.setsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.setsRecyclerView.adapter = adapter

        binding.buttonAddSet.setOnClickListener {
            val newSet = SetEntity(
                trainingId = sets.first().trainingId, // безопаснее использовать уже существующий
                exerciseId = exerciseId,
                reps = 0,
                weight = 0,
                duration = null,
                exerciseOrder = sets.size
            )
            adapter.addSet(newSet)
            onAddSet(newSet)
        }
        binding.buttonCheckSets.setOnClickListener {
            binding.root.clearFocus()

            val updatedSets = adapter.getCurrentSets()
                .filter { it.weight != 0 || it.reps != 0 }

            updatedSets.forEachIndexed { index, set ->
                set.exerciseOrder = index
                onUpdateSet(set)
            }

            dismiss()
        }

        binding.closeButton.setOnClickListener(){
            dismiss()
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

        if (adapter.getCurrentSets().isEmpty()){
            val dummySet = SetEntity(
                trainingId = sets.firstOrNull()?.trainingId ?: 0,
                exerciseId = exerciseId,
                reps = 0,
                weight = 0,
                duration = null,
                exerciseOrder = 0
            )
            onAddSet(dummySet)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

