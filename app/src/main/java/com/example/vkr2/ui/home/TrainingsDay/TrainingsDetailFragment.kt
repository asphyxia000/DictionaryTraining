package com.example.vkr2.ui.home.TrainingsDay

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentTrainingsDetailBinding
import com.example.vkr2.repository.InfoStatsRepositoryImpl
import com.example.vkr2.repository.TrainingRepositoryImpl
import com.example.vkr2.ui.AdaptersDirectory.TrainingDetailAdapter
import com.example.vkr2.ui.Notification_muscle_groups.NotificationsDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TrainingsDetailFragment : DialogFragment() {

    private var _binding: FragmentTrainingsDetailBinding? = null
    private val binding get() = _binding!!
    private var trainingId: Int = -1
    private var isEdit = false

    private var originalTitle: String = ""
    private var originalComment: String = ""

    private lateinit var adapter: TrainingDetailAdapter


    private val viewModel: TrainindDetailViewModel by viewModels {
        TrainindDetailViewModelFactory(
            TrainingRepositoryImpl(requireContext(), CoroutineScope(Dispatchers.IO)),
            InfoStatsRepositoryImpl(requireContext(), CoroutineScope(Dispatchers.IO))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.ScreenDialog)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        trainingId = arguments?.getInt("trainingId") ?: return

        adapter = TrainingDetailAdapter(
            trainingId = trainingId,
            onExercisesClick = { exercise ->
                showDialog(exercise.ExercisesId, exercise.ExercisesName)
            },
            onDeleteExercise = { exercise ->
                viewModel.deleteExercise(trainingId, exercise.ExercisesId)
            }
        )


        binding.recyclerViewExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewExercises.adapter = adapter

        viewModel.loadTraining(trainingId)
        viewModel.training.observe(viewLifecycleOwner) { data ->
            if (data == null) {
                Log.e("TrainingsDetailFragment", "Training data is null. Cannot update UI.")
                return@observe
            }
            originalTitle = data.training.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            originalComment = data.training.comment.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

            binding.trainingTitle.setText(originalTitle)
            binding.trainingComment.setText(originalComment)

            adapter.updateList(data.exercises)

            val createdAtFormatted = formatDateTime(data.training.createdAt)
            binding.dataDetail.text = createdAtFormatted

            setupEditWatchers()
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.trainingTitle.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showCheckButton() }
        binding.trainingComment.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showCheckButton() }

        binding.root.setOnTouchListener { _, _ ->
            saveIfChanged()
            hideKeyboard()
            clearEditFocus()
            false
        }

        binding.checkButton.setOnClickListener {
            saveIfChanged()
            hideKeyboard()
            clearEditFocus()
        }

        binding.closeBottomSheet.setOnClickListener { dismiss() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isEdit) {
                        hideKeyboard()
                        clearEditFocus()
                    } else {
                        dismiss()
                    }
                }
            })

        binding.buttonaddMini.setOnClickListener {
            val dialog = NotificationsDialogFragment.newInstance(trainingId)
            dialog.show(parentFragmentManager, "NotificationsDialog")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnKeyListener{ _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP){
                    if (isEdit){
                        hideKeyboard()
                        clearEditFocus()
                    }else{
                        dismiss()
                    }
                    true
                }else{
                    false
                }
            }
        }
    }

    private fun saveIfChanged() {
        val newTitle = binding.trainingTitle.text.toString()
        val newComment = binding.trainingComment.text.toString()

        if (newTitle != originalTitle || newComment != originalComment) {
            viewModel.saveChanges(trainingId, newTitle, newComment)
            originalTitle = newTitle
            originalComment = newComment
        }
    }

    private fun showDialog(exerciseId: Int, exerciseName: String) {
        lifecycleScope.launch {
            val currentSets = viewModel.getSetsForExercise(trainingId, exerciseId)
            val sets = currentSets.toMutableList()

            val trainingData = viewModel.training.value
            if (trainingData == null) {
                Log.e("TrainingsDetailFragment", "Cannot show SetsEditDialog: training data is null.")
                return@launch
            }

            val exerciseWithData = trainingData.exercises
                .find { it.exercise.ExercisesId == exerciseId }

            if (exerciseWithData == null) {
                Log.e("TrainingsDetailFragment", "Cannot show SetsEditDialog: exerciseWithData not found for id $exerciseId")
                return@launch
            }

            val exerciseImagePath = exerciseWithData.exercise.imagePath ?: ""

            val exerciseTypeString = exerciseWithData.exercise.type

            val exerciseType = try {
                ExerciseType.valueOf(exerciseTypeString.uppercase())
            } catch (e: IllegalArgumentException) {
                Log.w("TrainingsDetailFragment", "Invalid exercise type string: $exerciseTypeString. Defaulting to STRENGTH.")
                ExerciseType.STRENGTH // Default type if parsing fails
            } catch (e: Exception) {
                Log.e("TrainingsDetailFragment", "Error parsing exercise type: $exerciseTypeString", e)
                ExerciseType.STRENGTH // Default type on other errors
            }

            SetsEditDialogFragment(
                trainingId = trainingId,
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                exerciseImagePath = exerciseImagePath,
                exerciseType = exerciseType, // Pass the determined ExerciseType
                sets = sets,
                onAddSet = { viewModel.addSet(it) },
                onUpdateSet = { viewModel.updateSet(it) },
                onDeleteSet = { viewModel.deleteSet(it) }
            ).show(childFragmentManager, "SetsEditDialog")
        }
    }

    private fun setupEditWatchers() {
        binding.trainingTitle.addTextChangedListener { checkForChanges() }
        binding.trainingComment.addTextChangedListener { checkForChanges() }
    }

    private fun checkForChanges() {
        val hasChanged = binding.trainingTitle.text.toString() != originalTitle ||
                binding.trainingComment.text.toString() != originalComment
        isEdit = hasChanged
        binding.checkButton.visibility = if (hasChanged) View.VISIBLE else View.GONE
    }

    private fun showCheckButton() {
        isEdit = true
        binding.checkButton.visibility = View.VISIBLE
    }

    private fun clearEditFocus() {
        binding.trainingTitle.clearFocus()
        binding.trainingComment.clearFocus()
        binding.checkButton.visibility = View.GONE
        isEdit = false // Reset edit state
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val height = dpToPx(810) // Consider making this dynamic or a percentage
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height)
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawableResource(android.R.color.transparent) // Ensure this is intended
            setWindowAnimations(R.style.DialogSlideAnimation)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    private fun formatDateTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("ru"))
        return dateTime.format(formatter)
    }
}
