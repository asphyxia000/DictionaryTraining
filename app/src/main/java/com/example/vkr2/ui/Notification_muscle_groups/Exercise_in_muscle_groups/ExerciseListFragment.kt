package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.DataBase.TagsforExercise.TagsEntity
import com.example.vkr2.R
import com.example.vkr2.SharedSelection
import com.example.vkr2.databinding.FragmentExerciseListBinding
import com.example.vkr2.repository.ExercisesRepositoryImpl
import com.example.vkr2.repository.TagsRepositoryImpl
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import com.example.vkr2.databinding.DialogConfirmAddBinding
import com.example.vkr2.databinding.DialogDatePickerBinding
import com.example.vkr2.repository.TrainingRepositoryImpl
import com.example.vkr2.ui.AdaptersDirectory.ExerciseAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private const val ARG_GROUP_NAME = "groupName"


@Suppress("DEPRECATION")
class ExerciseListFragment : Fragment() {

    private var _binding: FragmentExerciseListBinding? = null
    private val binding get() = _binding!!

    private var isAddExercise:Boolean = false
    private var trainingIdToAdd: Int = -1

    private lateinit var daysOfWeek: List<DayOfWeek>

    private lateinit var adapter: ExerciseAdapter

    private val viewModel: ExerciseListViewModel by viewModels {
        ExerciseListViewModelFactory(
            ExercisesRepositoryImpl(requireContext(), CoroutineScope(Dispatchers.IO)),
            TagsRepositoryImpl(requireContext(), CoroutineScope(Dispatchers.IO)),
            TrainingRepositoryImpl(requireContext(),CoroutineScope(Dispatchers.IO))
        )
    }

    private var groupName: String? = null

    private val sharedViewModel: SharedSelection by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupName = it.getString(ARG_GROUP_NAME)
            isAddExercise = it.getBoolean("isAddExercise",false)
            trainingIdToAdd = it.getInt("trainingId",-1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentExerciseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAdd.alpha = 0f
        binding.btnAdd.visibility = View.GONE
        binding.cancelBtn.translationX = 0f

        // ✅ Настраиваем СВОЙ Toolbar (а не supportActionBar)
        binding.dialogToolbar.apply {
            title = arguments?.getString("selectedGroupName") ?: "Упражнения"
            setNavigationIcon(R.drawable.ic_arrow_back) // 👈 Стандартная стрелка назад
            setNavigationOnClickListener {
                findNavController().popBackStack() // 👈 Назад при нажатии
            }
        }

        // Скрытие клавиатуры при клике на корень
        binding.root.setOnTouchListener { v, _ ->
            if (binding.searchEditText.hasFocus()) {
                binding.searchEditText.clearFocus()
                hideKeyboard()
                v.performClick()
            }
            false
        }

        binding.cancelBtn.setOnClickListener {
            val selectedCount = adapter.getSelectedExercises().size
            if (sharedViewModel.getSelectedCount() == 0) {
                findNavController().popBackStack()
            } else {
                sharedViewModel.clearSelection()
                adapter.clearSelectin()
                hideAddBtn()
            }
        }

        binding.btnAdd.setOnClickListener {
            showDialog()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.searchEditText.hasFocus()) {
                        binding.searchEditText.clearFocus()
                        hideKeyboard()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        )

        val groupID = arguments?.getInt("selectedGroupID") ?: return
        val groupName = arguments?.getString("selectedGroupName") ?: "Упражнения"

        viewModel.setGroupId(groupID)

        setupAdapter()
        setupObservers()
        setupSearch()
        loadTags(groupID)
        hideBottomNav()
    }

    private fun setupAdapter() {
        adapter = ExerciseAdapter(emptyList()) { exercise, isSelected,isImageClick ->

            if (isImageClick){
                val bundle = Bundle().apply {
                    putInt("exerciseId",exercise.ExercisesId)
                }
                findNavController().navigate(
                    R.id.exercisesDetailFragment,
                    bundle
                )
                return@ExerciseAdapter // 👈 выходим из лямбды, чтобы дальше не выполнялось
            }
            
            sharedViewModel.toggleSelection(exercise)

            val selectedCount = sharedViewModel.getSelectedCount()
            if (selectedCount > 0) {
                updateAddBtn(selectedCount)
            } else {
                hideAddBtn()
            }
        }
        val selectedIds = sharedViewModel.getAllSelected().map { it.ExercisesId }
        adapter.setInitialSelection(selectedIds)
        binding.recyclerViewExercises.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExerciseListFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            val sortedExercises=exercises.sortedBy { it.ExercisesName }
            adapter.updateData(sortedExercises)
        }
    }

    private fun setupSearch() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString()
                viewModel.setSearchQuery(query)
                binding.searchEditText.clearFocus()
                true
            } else {
                false
            }
        }

        binding.searchEditText.addTextChangedListener {
            viewModel.setSearchQuery(it.toString())
        }
    }


    private fun sortTag(tags: List<TagsEntity>): List<TagsEntity> {

        return tags.sortedWith(compareBy(
            { tag ->
                when {
                    tag.name in TagsGroups.anatomyTags -> 0
                    tag.name in TagsGroups.equipmentTags -> 1
                    else -> 2
                }
            },
            { tag -> tag.name }
        ))
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private fun loadTags(groupId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getTagsForGroup(groupId).collectLatest { tags ->
                    binding.chipGroupTags.removeAllViews()

                    val sortedTags = sortTag(tags)

                    sortedTags.forEach { tag ->
                        val chip = Chip(requireContext()).apply {
                            text = tag.name
                            isCheckable = true
                            if (tag.name in TagsGroups.anatomyTags) {
                                chipBackgroundColor = resources.getColorStateList(R.color.chip_back)
                                chipStrokeWidth = 2f
                                chipStrokeColor = ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.chip_text
                                    )
                                )
                                setTextColor(resources.getColor(R.color.chip_text))
                            }
                            setOnCheckedChangeListener { _, _ ->
                                if (tag.name in TagsGroups.anatomyTags) {
                                    if (isChecked) {
                                        chipBackgroundColor = ColorStateList.valueOf(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.chip_selected_bg
                                            )
                                        )
                                        setTextColor(ContextCompat.getColor(context, R.color.white))
                                    } else {
                                        chipBackgroundColor =
                                            resources.getColorStateList(R.color.chip_back)
                                        chipStrokeWidth = 2f
                                        chipStrokeColor = ColorStateList.valueOf(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.chip_text
                                            )
                                        )
                                        setTextColor(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.chip_text
                                            )
                                        )
                                    }
                                }
                                viewModel.toggleTag(tag.name)
                            }
                        }
                        binding.chipGroupTags.addView(chip)
                    }
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun hideBottomNav() {
        val navView = requireActivity().findViewById<BottomNavigationView?>(R.id.nav_view)
        navView?.let {
            it.animate()
                .translationY(it.height.toFloat())
                .setDuration(200)
                .withEndAction { it.visibility = View.GONE }
                .start()
        }
    }


    override fun onResume() {
        super.onResume()
        val count = sharedViewModel.getSelectedCount()
        if (count > 0) {
            updateAddBtn(count)
        } else {
            hideAddBtn()
        }
        hideBottomNav()
    }

    @SuppressLint("SetTextI18n")
    private fun updateAddBtn(count: Int) {
        if (binding.btnAdd.visibility == View.VISIBLE) {
            binding.btnAdd.text = "ДОБАВИТЬ • $count"
        } else {
            showAddBtn(count)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showAddBtn(count: Int) {
        if (binding.btnAdd.visibility != View.VISIBLE) {
            binding.btnAdd.text = "ДОБАВИТЬ • $count"
            // Перед показом, сдвигаем чуть правее и прячем
            binding.btnAdd.translationX = 50f
            binding.btnAdd.alpha = 0f
            binding.btnAdd.visibility = View.VISIBLE
            binding.btnAdd.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.btnAdd.requestLayout()
            // Анимация выезда
            binding.btnAdd.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(200)
                .start()
        } else {
            binding.btnAdd.text = "ДОБАВИТЬ • $count"
        }

    }


    private fun hideAddBtn() {
        binding.btnAdd.animate()
            .translationX(50f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.btnAdd.visibility = View.GONE
            }
            .start()
        Log.d("DEBUG", "cancelBtn translationX = ${binding.cancelBtn.translationX}")
    }

    private fun showDialog() {
        val binding = DialogConfirmAddBinding.inflate(LayoutInflater.from(requireContext()))

        // Установка текущей даты на кнопку
        var today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale("ru"))
        binding.btnSelectDate.text = today.format(formatter)

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()

        alertDialog.show()

        binding.btnSelectDate.setOnClickListener {
//          showMaterialDatePicker(binding)
            showDatePicker { pickedDate ->
                today = pickedDate
                binding.btnSelectDate.text = pickedDate.format(formatter)
            }
        }

        binding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.btnAdd.setOnClickListener {

            val selectedExercises = sharedViewModel.getAllSelected()
            if (isAddExercise){
                if (selectedExercises.isEmpty()){
                    Toast.makeText(requireContext(),"Выберите упражнения",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.addExercisesToExistingTraining(trainingIdToAdd,selectedExercises)
                    sharedViewModel.clearSelection()
                    alertDialog.dismiss()
                    findNavController().popBackStack()

                    parentFragment?.parentFragmentManager?.findFragmentByTag("NotificationsDialog")?.let { dialogFragment->
                        if (dialogFragment is DialogFragment){
                            dialogFragment.dismiss()
                        }
                    }
                }
            }else{
                val name = binding.dialogTrainingName.text?.toString()?.trim().orEmpty()
                val comment = binding.dialogTrainingComment.text?.toString()?.trim().orEmpty()
                val date = today

                if (name.isBlank()||selectedExercises.isEmpty()||comment.isBlank()){
                    Toast.makeText(requireContext(),"Введите название и комментарий",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.createTraining(date,name,comment,selectedExercises)
                    sharedViewModel.clearSelection()
                    alertDialog.dismiss()
                    findNavController().navigate(
                        R.id.navigation_home,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.navigation_home, inclusive = false)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                }
            }
            alertDialog.show()
        }
    }

    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val binding = DialogDatePickerBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()

        val Today = LocalDate.now()
        var selectedDate = Today
        val daysOfWeek = daysOfWeek(DayOfWeek.MONDAY)
        val startMonth = YearMonth.now().minusMonths(12)
        val endMonth = YearMonth.now().plusMonths(12)

        val calendarView = binding.calendarView
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(Today.yearMonth)

        fun updateMonthYearText(month: YearMonth){
            val formatter = DateTimeFormatter.ofPattern("LLL yyyy",Locale("ru"))
            val text = month.format(formatter).replaceFirstChar { it.uppercase() }
            binding.monthYearTextDatepicker.text = text
        }
        updateMonthYearText(Today.yearMonth)
        calendarView.monthScrollListener={
            updateMonthYearText(it.yearMonth)
        }

        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderContainer> {
            override fun create(view: View) = MonthHeaderContainer(view)
            override fun bind(container: MonthHeaderContainer, data: CalendarMonth) {
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
                            textView.text = title
                        }
                }
            }
        }
        calendarView.dayBinder = object : MonthDayBinder<MonthDayContainer>{
            override fun create(view: View)=MonthDayContainer(view)
            override fun bind(container: MonthDayContainer, data: CalendarDay) {
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                val calendarColor = ContextCompat.getColor(requireContext(),R.color.Calendar)
                val calendarSetColor=ContextCompat.getColor(requireContext(),R.color.CalendarSelect)
                val todayColor = ContextCompat.getColor(requireContext(), R.color.CalendarToday)

                textView.setTextColor(calendarColor)
                textView.background=null
                textView.alpha = if (data.position==DayPosition.MonthDate)1f else 0.5f

                when{
                    data.date==selectedDate->{
                        textView.setTextColor(calendarSetColor)
                        textView.background = ContextCompat.getDrawable(requireContext(),R.drawable.selected_day_bg)
                    }
                    data.date==Today->{
                        textView.setTextColor(todayColor)
                        textView.background=ContextCompat.getDrawable(requireContext(),R.drawable.button_today_style)
                    }
                }
                container.view.setOnClickListener(){
                    selectedDate=data.date
                    onDateSelected(data.date)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }
    class MonthDayContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
    }

    class MonthHeaderContainer(view: View) : ViewContainer(view) {
        val titlesContainer: ViewGroup = view as ViewGroup
    }


    private fun showMaterialDatePicker(binding: DialogConfirmAddBinding) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        picker.show(parentFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale("ru"))
            binding.btnSelectDate.text = selectedDate.format(formatter)
        }
    }


    private fun showBottomNavIfNeeded() {
        val currentDestination = findNavController().currentDestination?.id
        if (currentDestination != R.id.exercisesDetailFragment) {
            val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            navView?.let {
                it.visibility = View.VISIBLE
                it.translationY = it.height.toFloat()
                it.animate()
                    .translationY(0f)
                    .setDuration(200)
                    .start()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNavIfNeeded()
        _binding = null
    }


    object TagsGroups {
        val anatomyTags = listOf(
            "Середина", "Верх", "Низ", "Бицепс", "Трицепс", "Предплечье", "Широчайшие",
            "Поясница", "Трапеция ", "Квадрицепс", "Бицепс бедра", "Ягодицы",
            "Икры", "Внутр", "Перед", "Задн", "Пресс", "Косые"
        )

        val equipmentTags = listOf("Штанга", "Гантели", "Тренажер", "Свой вес", "Свободный вес")
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            ExerciseListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_GROUP_NAME, param1)
                }
            }
    }
}