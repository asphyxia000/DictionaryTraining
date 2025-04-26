package com.example.vkr2.ui.home

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentHomeBinding
import com.example.vkr2.repository.TrainingRepositoryImpl
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.fragment.app.viewModels
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.databinding.DialogConfirmAddBinding
import com.example.vkr2.databinding.DialogConfirmDeleteBinding
import com.example.vkr2.ui.home.TrainingsDay.TrainingsDetailFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding?=null
    private val binding get() = _binding!!

    private val currentDate = LocalDate.now()
    private var selectedDate: LocalDate = currentDate
    private lateinit var daysOfWeek: List<DayOfWeek>
    private var isWeekMode: Boolean = true
    private var isDayMode = false
    private val weekHeight by lazy { resources.getDimensionPixelSize(R.dimen.week_calendar_height) }
    private var displayedMonth: YearMonth = YearMonth.now()



    private val viewModel: HomeViewModel by viewModels{
        HomeViewModelFactory(
            TrainingRepositoryImpl(requireContext(), CoroutineScope(Dispatchers.IO))
        )
    }
    private lateinit var trainingDayAdapter: TrainingDayAdapter
    private lateinit var weekAdapter: WeekAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadAllTrainings()
        viewModel.loadTrainingsGroupedByWeek()

        weekAdapter = WeekAdapter(requireContext(), emptyList(), onItemClick = { training ->
            val bottomSheetDialogFragmentWeek=TrainingsDetailFragment().apply {
                arguments=Bundle().apply {
                    putInt("trainingId", training.trainingId)
                }
            }
            bottomSheetDialogFragmentWeek.show(parentFragmentManager,bottomSheetDialogFragmentWeek.tag)},
//            val bundle = Bundle().apply {
//                putInt("trainingId", training.trainingId)
//            }
//            findNavController().navigate(R.id.trainingsDetailFragment, bundle) },
            onEdit = { showEditDialog(it)},
            onDelete = { deleteTraining(it)})

        binding.recyclerViewtrainDay.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewtrainDay.adapter = weekAdapter

        trainingDayAdapter = TrainingDayAdapter(emptyList(), onItemClick = { training ->
        val bottomSheetDialogFragment = TrainingsDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("trainingId",training.trainingId)
            }
        }
            bottomSheetDialogFragment.show(parentFragmentManager,bottomSheetDialogFragment.tag)
//
//            requireActivity().supportFragmentManager.beginTransaction()
//                .replace(R.id.nav_host_fragment_activity_main,fragment)
//                .addToBackStack(null)
//                .commit()
        },onEdit = { training ->
            showEditDialog(training)
            // обработка нажатия на меню "Редактировать"
        },
            onDelete= {
                    training->
                deleteTraining(training)
            })

        binding.recyclerViewtrainDay.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = weekAdapter
        }

        viewModel.weeks.observe(viewLifecycleOwner) { weekList ->
            if (!isDayMode) {

                weekAdapter.updateList(weekList)
                updateVisibility(weekList.isEmpty())
            }
        }

        viewModel.trainings.observe(viewLifecycleOwner){list->
            if(isDayMode){
                trainingDayAdapter.updateList(list)
                binding.recyclerViewtrainDay.adapter = trainingDayAdapter
                updateVisibility(list.isEmpty())
            }
        }



        binding.buttonftrainingFull.setOnClickListener {
            findNavController().popBackStack(R.id.navigation_home, true)
            findNavController().navigate(R.id.navigation_notifications)
        }
        binding.buttonftrainingMini.setOnClickListener {
            findNavController().popBackStack(R.id.navigation_home, true)
            findNavController().navigate(R.id.navigation_notifications)
        }

        daysOfWeek = daysOfWeek()
        setupCalendars()


        binding.monthYearText.setOnClickListener {
            toggleCalendarMode()
        }

        binding.monthCalendar.visibility = View.GONE

        binding.btnToday.setOnClickListener {
            selectedDate = LocalDate.now()
            displayedMonth = selectedDate.yearMonth
            binding.weekCalendar.smoothScrollToDate(selectedDate)
            updateMonthYearText()
            binding.btnToday.visibility = View.GONE
        }
    }

    private fun updateVisibility(isEmpty: Boolean){
        binding.imageView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewtrainDay.visibility = if (isEmpty)View.GONE else View.VISIBLE
        binding.buttonftrainingMini.visibility = if (isEmpty)View.GONE else View.VISIBLE
        binding.buttonftrainingFull.visibility = if (isEmpty)View.VISIBLE else View.GONE

        binding.weekCalendar.notifyCalendarChanged()
        binding.monthCalendar.notifyCalendarChanged()
    }



    private fun setupCalendars() {
        val startMonth = YearMonth.now().minusMonths(12)
        val endMonth = YearMonth.now().plusMonths(12)
        val firstDayOfWeek = daysOfWeek.first()

        val startDate = currentDate.minusYears(1)
        val endDate = currentDate.plusYears(1)

        binding.weekCalendar.layoutParams.height = weekHeight
        binding.monthCalendar.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.weekCalendar.requestLayout()

        // Настройка месячного календаря
        with(binding.monthCalendar) {
            setup(startMonth, endMonth, firstDayOfWeek)
            scrollToMonth(selectedDate.yearMonth)

            binding.btnToday.visibility=View.GONE

            monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderContainer> {
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

            monthScrollListener = { calendarMonth ->
                displayedMonth = calendarMonth.yearMonth // Обновляем только отображаемый месяц
                updateMonthYearText() // Обновляем заголовок
                updateTodayButtonVisibility()
            }
                dayBinder = object : MonthDayBinder<MonthDayContainer> {
                override fun create(view: View) = MonthDayContainer(view)
                override fun bind(container: MonthDayContainer, data: CalendarDay) {

                    val textView=container.textView
                    textView.text=data.date.dayOfMonth.toString()

                    val calendarColor = ContextCompat.getColor(requireContext(),R.color.Calendar)
                    val calendarSetColor = ContextCompat.getColor(requireContext(),R.color.CalendarSelect)
                    val todayColor = ContextCompat.getColor(requireContext(),R.color.CalendarToday)

                    textView.setTextColor(calendarColor)
                    textView.background=null
                    container.textView.alpha = if (data.position == DayPosition.MonthDate) 1f else 0.5f

                    when {
                        // Если дата выбрана
                        data.date == selectedDate -> {
                            textView.setTextColor(calendarSetColor)
                            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.selected_day_bg)
                        }
                        // Если это текущий день
                        data.date == LocalDate.now() -> {
                            textView.setTextColor(todayColor)
                            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_today_style)
                        }
                    }

                    binding.btnToday.setOnClickListener {
                        selectedDate = LocalDate.now()
                        displayedMonth = selectedDate.yearMonth // Обновляем отображаемый месяц

                        if (isWeekMode) {
                            binding.weekCalendar.smoothScrollToDate(selectedDate)
                        } else {
                            binding.monthCalendar.smoothScrollToMonth(selectedDate.yearMonth)
                        }

                        notifyCalendarsChanged()
                        updateMonthYearText()
                        binding.btnToday.visibility = View.GONE

                        returnToWeekView()
                    }

                    val count = viewModel.trainingDateCounts.value?.get(data.date) ?: 0
                    container.dotContainer.removeAllViews()

                    if (count > 0) {
                        container.dotContainer.visibility = View.VISIBLE
                        val maxDots = minOf(4, count) // максимум 4 точки

                        repeat(maxDots) {
                            val dot = LayoutInflater.from(container.view.context)
                                .inflate(R.layout.dot_item, container.dotContainer, false)
                            container.dotContainer.addView(dot)
                        }
                    } else {
                        container.dotContainer.visibility = View.GONE
                    }

                    // Обработка нажатия
                    container.view.setOnClickListener {
                        selectedDate = data.date
                        displayedMonth = selectedDate.yearMonth
                        toggleCalendarMode() // Возврат к недельному календарю
                        binding.weekCalendar.smoothScrollToDate(selectedDate) // Прокрутка к выбранной дате
                        notifyCalendarsChanged() // Обновление обоих календарей

                        showTrainingsForDate(data.date)

                    }
                }
            }
        }

        with(binding.weekCalendar) {
            setup(startDate, endDate, firstDayOfWeek)

            binding.btnToday.visibility=View.GONE

            weekHeaderBinder = object : WeekHeaderFooterBinder<WeekHeaderContainer> {
                override fun create(view: View) = WeekHeaderContainer(view)
                override fun bind(container: WeekHeaderContainer, data: Week) {
                    val daysOfWeek = data.days.map { it.date.dayOfWeek }

                    container.titlesContainer.children
                        .map { it as TextView }
                        .forEachIndexed { index, textView ->
                            if (index < daysOfWeek.size) {
                                val dayOfWeek = daysOfWeek[index]
                                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                    .replaceFirstChar { it.uppercase() }
                                textView.text = title
                            }
                        }
                }
            }

            weekScrollListener = { week ->
                val isCurrentWeekVisible = week.days.any { it.date == LocalDate.now() }
                binding.btnToday.visibility = if (isCurrentWeekVisible) View.GONE else View.VISIBLE


                val middleDate = week.days[3].date
                displayedMonth = middleDate.yearMonth // Обновляем отображаемый месяц
                updateMonthYearText()
            }


            dayBinder = object : WeekDayBinder<WeekDayContainer> {
                override fun create(view: View) = WeekDayContainer(view)
                override fun bind(container: WeekDayContainer, data: WeekDay) {
                    val textView = container.textView
                    textView.text = data.date.dayOfMonth.toString()
                    val calendarColor = ContextCompat.getColor(requireContext(), R.color.Calendar)
                    val calendarSelectColor = ContextCompat.getColor(requireContext(), R.color.CalendarSelect)
                    val todayColor = ContextCompat.getColor(requireContext(), R.color.CalendarToday)

                    textView.text = data.date.dayOfMonth.toString()
                    textView.setTextColor(calendarColor)
                    textView.textSize = 16f
                    textView.background = null // Убираем фон по умолчанию

                    when {
                        // Если дата - это выбранная дата
                        data.date == selectedDate -> {
                            textView.setTextColor(calendarSelectColor)
                            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.selected_day_bg)
                        }
                        // Если дата - текущий день
                        data.date == LocalDate.now() -> {
                            textView.setTextColor(todayColor)
                            textView.background = ContextCompat.getDrawable(requireContext(),
                                R.drawable.button_today_style
                            ) // Используем selected_day_bg для старта
                        }
                    }

                    val count = viewModel.trainingDateCounts.value?.get(data.date) ?: 0
                    container.dotContainer.removeAllViews()

                    if (count > 0) {
                        container.dotContainer.visibility = View.VISIBLE
                        val maxDots = minOf(4, count) // максимум 4 точки

                        repeat(maxDots) {
                            val dot = LayoutInflater.from(container.view.context)
                                .inflate(R.layout.dot_item, container.dotContainer, false)
                            container.dotContainer.addView(dot)
                        }
                    } else {
                        container.dotContainer.visibility = View.GONE
                    }


                    container.view.setOnClickListener {
                        val previousSelectedDate = selectedDate
                        selectedDate = data.date
                        displayedMonth = selectedDate.yearMonth // Синхронизация
                        // Обновляем заголовок месяца
                        updateMonthYearText()

                        // Перерисовываем календарь
                        notifyCalendarsChanged()

                        // Меняем фон у предыдущей даты на today_bg, если она была текущим днём
                        if (previousSelectedDate == LocalDate.now()) {
                            binding.weekCalendar.notifyDateChanged(previousSelectedDate)
                        }
                        showTrainingsForDate(data.date)

                    }

                    binding.btnToday.setOnClickListener {
                        selectedDate = LocalDate.now() // Делаем текущий день выбранным
                        smoothScrollToDate(LocalDate.now())
                        updateMonthYearText()
                        notifyCalendarsChanged()
                        binding.btnToday.visibility = View.GONE // Скрываем кнопку после возврата

                        returnToWeekView()
                    }
                }
            }
            scrollToDate(selectedDate)
        }
        updateMonthYearText()
        updateTodayButtonVisibility()
    }

    private fun notifyCalendarsChanged() {
        binding.weekCalendar.notifyCalendarChanged()
        binding.monthCalendar.notifyCalendarChanged()
        updateMonthYearText()
        updateTodayButtonVisibility()
    }

    private fun toggleCalendarMode() {
        isWeekMode = !isWeekMode
        if (isWeekMode) {
            binding.weekCalendar.visibility = View.VISIBLE
            binding.monthCalendar.visibility = View.GONE
            displayedMonth = selectedDate.yearMonth
            binding.weekCalendar.smoothScrollToDate(selectedDate) // Прокрутка к выбранной дате
        } else {
            binding.weekCalendar.visibility = View.GONE
            binding.monthCalendar.visibility = View.VISIBLE
            binding.monthCalendar.scrollToMonth(displayedMonth)
            binding.monthCalendar.notifyCalendarChanged() // Ключевое исправление!
        }
        updateMonthYearText()
        updateTodayButtonVisibility()
        animateCalendarHeight()

    }

    private fun animateCalendarHeight() {
        val targetHeight = if (isWeekMode) weekHeight else calculateMonthHeight()
        ValueAnimator.ofInt(binding.weekCalendar.height, targetHeight).apply {
            addUpdateListener { animation ->
                binding.weekCalendar.layoutParams.height = animation.animatedValue as Int
                binding.weekCalendar.requestLayout()

            }
            duration = 300
            start()
        }
    }

    private fun calculateMonthHeight(): Int {
        return (resources.displayMetrics.density * 320).toInt()
    }



    private fun updateMonthYearText() {
        val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
        val text = when {
            isWeekMode -> displayedMonth.format(formatter)
            else -> displayedMonth.format(formatter) // Используем displayedMonth для обоих режимов
        }.replaceFirstChar { it.uppercase() }
        binding.monthYearText.text = text
    }

    private fun updateTodayButtonVisibility() {
        if (isWeekMode) {
            // Проверяем, видна ли текущая неделя
            val isCurrentWeekVisible = binding.weekCalendar.findFirstVisibleDay()?.date?.let {
                it.yearMonth == YearMonth.now()
            } ?: false
            binding.btnToday.visibility = if (isCurrentWeekVisible) View.GONE else View.VISIBLE
        } else {
            // Проверяем, отображается ли текущий месяц и выбран ли сегодняшний день
            val isCurrentMonthVisible = displayedMonth == YearMonth.now()
            val isTodaySelected = selectedDate == LocalDate.now()
            binding.btnToday.visibility = if (isCurrentMonthVisible && isTodaySelected) View.GONE else View.VISIBLE
        }
    }


    class WeekDayContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
        val dotContainer: LinearLayout = view.findViewById(R.id.dotContainer)

    }

    class MonthDayContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
        val dotContainer: LinearLayout = view.findViewById(R.id.dotContainer)

    }

    class WeekHeaderContainer(view: View) : ViewContainer(view) {
        val titlesContainer: ViewGroup = view as ViewGroup
    }

    class MonthHeaderContainer(view: View) : ViewContainer(view) {
        val titlesContainer: ViewGroup = view as ViewGroup
    }

    private fun showTrainingsForDate(date: LocalDate) {
        isDayMode = true
        viewModel.loadTrainingsForDate(date)
        binding.btnToday.visibility = View.VISIBLE
    }

    private fun returnToWeekView() {
        isDayMode = false
        viewModel.loadTrainingsGroupedByWeek()
        binding.recyclerViewtrainDay.adapter = weekAdapter
    }

    private fun deleteTraining(training: TrainingsEntity){
        val dialogBinding = DialogConfirmDeleteBinding.inflate(layoutInflater)

        dialogBinding.deleteMessage.text =  "Вы уверены, что хотите удалить тренировку \"${training.name}\"?"

        val alterDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.btnCancel.setOnClickListener(){
            alterDialog.dismiss()
        }
        dialogBinding.btnAdd.setOnClickListener(){
            viewModel.deleteTraining(training)
            alterDialog.dismiss()
        }
        alterDialog.show()
    }

    private fun showEditDialog(training: TrainingsEntity){
        val dialogBinding = DialogConfirmAddBinding.inflate(layoutInflater)

        dialogBinding.btnSelectDate.visibility = View.GONE

        dialogBinding.dialogTrainingName.setText(training.name)
        dialogBinding.dialogTrainingComment.setText(training.comment)
        dialogBinding.btnAdd.text = "Сохранить"

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.btnAdd.setOnClickListener()
            {
                val updateName = dialogBinding.dialogTrainingName.text.toString()
                val updateComment=dialogBinding.dialogTrainingComment.text.toString()
                val updateTraining = training.copy(name = updateName, comment = updateComment)

                viewModel.updateTraining(updateTraining)
                alertDialog.dismiss()
            }
        dialogBinding.btnCancel.setOnClickListener(){
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}
