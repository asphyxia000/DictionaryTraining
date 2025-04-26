package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentInfoBinding
import com.example.vkr2.repository.InfoStatsRepository
import com.example.vkr2.repository.InfoStatsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ExerciseDetailViewModel


    companion object {
        fun newInstance(exerciseId: Int): Fragment {
            val fragment = InfoFragment()
            fragment.arguments = Bundle().apply {
                putInt("exerciseId", exerciseId)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exerciseId = requireArguments().getInt("exerciseId")

        val repository = InfoStatsRepositoryImpl(requireContext().applicationContext, CoroutineScope(
            Dispatchers.IO)
        )
        val factory = ExercisesDetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ExerciseDetailViewModel::class.java]

        viewModel.loadExerciseInfo(exerciseId)

        viewModel.exerciseInfo.observe(viewLifecycleOwner) { info ->
            binding.textDescription.text = info?.description
            binding.textTips.text = info?.executionTips
        }

        viewModel.exerciseImagePath.observe(viewLifecycleOwner) { imagePath ->
            imagePath?.let {
                val context = requireContext()
                val resId = context.resources.getIdentifier(
                    it.substringBeforeLast('.'),
                    "drawable",
                    context.packageName
                )
                if (resId != 0) {
                    Glide.with(context).load(resId).into(binding.imageViewGif)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}