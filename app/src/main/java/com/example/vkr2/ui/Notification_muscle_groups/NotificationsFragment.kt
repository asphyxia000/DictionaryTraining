package com.example.vkr2.ui.Notification_muscle_groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.DataBase.MuscleGroup.MuscleGroupEntity
import com.example.vkr2.R
import com.example.vkr2.SharedSelection
import com.example.vkr2.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NoExpAdapter

    private val sharedViewModel: SharedSelection by activityViewModels()

    private var isAddExercise:Boolean = false
    private var trainingIdToAdd:Int = -1

    private val viewModel: NotificationsViewModel by viewModels {
        NotificationListViewModelFactory(requireContext())
    }

    companion object{
        fun newInstanceForAddingExercises (trainingId:Int):NotificationsFragment{
            val fragment = NotificationsFragment()
            fragment.arguments = Bundle().apply {
                putBoolean("isAddExercise",true)
                putInt("trainingId",trainingId)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isAddExercise = arguments?.getBoolean("isAddExercise",false)?:false
        val trainingId = arguments?.getInt("trainingId", -1)?: -1

        val selectedCount=sharedViewModel.getSelectedCount()
        if (selectedCount>0){
            showBtn(selectedCount)
        }
        else{
            hideBtn()
        }

        binding.cancelBtn.setOnClickListener {
            sharedViewModel.clearSelection()
            hideBtn()
        }

        binding.btnAdd.setOnClickListener {
            val selected = sharedViewModel.getAllSelected()
            // Например, передаём их в следующий экран или сохраняем
        }

        setupAdapter()
        setupObservers()
    }


    private fun setupAdapter() {
        adapter = NoExpAdapter(emptyList()) { muscleGroup ->
            val bundle = bundleOf(
                "selectedGroupName" to muscleGroup.NameMuscleGroups,
                "selectedGroupID" to muscleGroup.MuscleGroupsID,
                "isAddExercise" to isAddExercise,
                "trainingId" to trainingIdToAdd
            )
            findNavController().navigate(R.id.exercisesListFragment, bundle)
        }

        binding.view3.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificationsFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.text.observe(viewLifecycleOwner) {
            binding.textforexp1.text = "Группы упражнений"
            binding.textViewdesc.text = "Выберите определенную группу упражнений"
        }
        viewModel.muscleGroups.observe(viewLifecycleOwner) { groups ->
            adapter.updateData(groups)
        }
    }

    private fun updateResults(mg: List<MuscleGroupEntity>) {
        adapter.updateData(mg)
    }

    override fun onResume() {
        super.onResume()
        val count = sharedViewModel.getSelectedCount()
        if (count > 0) {
            showBtn(count)
        } else {
            hideBtn()
        }
    }

    private fun showBtn(count: Int){
        binding.buttonGroup.visibility=View.VISIBLE
        binding.btnAdd.text = "Добавить $count"
    }

    private fun hideBtn(){
        binding.buttonGroup.visibility=View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isAddExercise = it.getBoolean("isAddExercise",false)
            trainingIdToAdd = it.getInt("trainingId",-1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}