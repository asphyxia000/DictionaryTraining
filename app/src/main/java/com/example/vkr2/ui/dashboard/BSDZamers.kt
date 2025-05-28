package com.example.vkr2.ui.dashboard

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView // Added for Calendar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children // Added for Calendar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.DialogDatePickerBinding // Assuming this exists for your calendar dialog
import com.example.vkr2.databinding.DialogForBodyMeasurementsBinding
import com.example.vkr2.repository.BodyMeasurementsRepositoryImpl
import com.example.vkr2.ui.AdaptersDirectory.ZamersItemAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton // Added for dialog_add_zamer.xml
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kizitonwose.calendar.core.* // Added for Calendar
import com.kizitonwose.calendar.view.MonthDayBinder // Added for Calendar
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder // Added for Calendar
import com.kizitonwose.calendar.view.ViewContainer // Added for Calendar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek // Added for Calendar
import java.time.LocalDate
import java.time.YearMonth // Added for Calendar
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle // Added for Calendar
import java.util.Locale // Added for Calendar
import java.time.temporal.WeekFields // If using the daysOfWeek function provided above


class BSDZamers : DialogFragment() {

    private var _binding: DialogForBodyMeasurementsBinding? = null
    private val binding get() = _binding!!

    private lateinit var bodyPart: String
    private var isLeft: Boolean = true
    private lateinit var zamersAdapter: ZamersItemAdapter
    private lateinit var repository: BodyMeasurementsRepositoryImpl

    // Date formatter for display on buttons/text
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))
    private val shortDisplayDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("ru"))


    // --- onCreate, onCreateDialog, onCreateView (no changes from your original) ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bodyPart = requireArguments().getString("bodyPart") ?: "?"
        isLeft = requireArguments().getBoolean("isLeft")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                dismiss()
            }
        }.apply {
            window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogForBodyMeasurementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = BodyMeasurementsRepositoryImpl(requireContext(), lifecycleScope)

        binding.nameZamer.text = if (bodyPart in listOf("Предплечья", "Бицепсы", "Трицепсы", "Бедра", "Икры")) {
            "$bodyPart (${if (isLeft) "Л" else "П"})"
        } else {
            bodyPart
        }

        val barChart = binding.barChartStats
        val lineChart = binding.lineChartStats
        val switchButton = binding.chartButton

        lifecycleScope.launch {
            val (entries, labels) = loadChartData()
            if (entries.isEmpty()) {
                binding.chartCard.visibility = View.GONE
            } else {
                binding.chartCard.visibility = View.VISIBLE
                setupBarChart(barChart, entries, labels)
                setupLineChart(lineChart, entries, labels)
                barChart.visibility = View.VISIBLE
                lineChart.visibility = View.GONE
            }
        }

        // --- Новый адаптер с двумя колбэками для PopupMenu ---
        zamersAdapter = ZamersItemAdapter(
            bodyPart,
            isLeft,
            onEditClick = { zamer -> showAddOrEditZamerDialog(zamer) },
            onDeleteClick = { zamer -> showDeleteConfirmationDialog(zamer) }
        )

        binding.zamerConteiner.layoutManager = LinearLayoutManager(requireContext())
        binding.zamerConteiner.adapter = zamersAdapter

        switchButton.setOnClickListener {
            if (barChart.visibility == View.VISIBLE) {
                barChart.visibility = View.GONE
                lineChart.visibility = View.VISIBLE
                lineChart.invalidate()
            } else {
                barChart.visibility = View.VISIBLE
                lineChart.visibility = View.GONE
                barChart.invalidate()
            }
        }

        binding.closeZamer.setOnClickListener { dismiss() }
        binding.bntAddZamer.setOnClickListener { showAddOrEditZamerDialog(null) }
        loadZamer()
    }

    private fun showDeleteConfirmationDialog(zamer: BodyMeasurementsEntity) {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Удаление замера")
            .setMessage("Удалить замер от ${zamer.date.format(displayDateFormatter)}?")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    val updated = when (bodyPart) {
                        "Бицепсы" -> if (isLeft) zamer.copy(bicepsLeft = null) else zamer.copy(bicepsRight = null)
                        "Трицепсы" -> if (isLeft) zamer.copy(tricepsLeft = null) else zamer.copy(tricepsRight = null)
                        "Предплечья" -> if (isLeft) zamer.copy(forearmsLeft = null) else zamer.copy(forearmsRight = null)
                        "Бедра" -> if (isLeft) zamer.copy(bedroLeft = null) else zamer.copy(begroRight = null)
                        "Икры" -> if (isLeft) zamer.copy(ikriLeft = null) else zamer.copy(ikriRight = null)
                        "Шея" -> zamer.copy(neck = null)
                        "Плечи" -> zamer.copy(shoulders = null)
                        "Грудь" -> zamer.copy(chest = null)
                        "Талия" -> zamer.copy(waist = null)
                        "Таз" -> zamer.copy(pelvis = null)
                        else -> zamer
                    }
                    repository.update(updated)
                    loadZamer()
                    updateChartsVisibility()
                }
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
        }
        dialog.show()
    }

    private fun updateChartsVisibility() {
        lifecycleScope.launch {
            val (entries, labels) = loadChartData()
            val barChart = binding.barChartStats
            val lineChart = binding.lineChartStats

            if (entries.isEmpty()) {
                binding.chartCard.visibility = View.GONE
            } else {
                binding.chartCard.visibility = View.VISIBLE
                setupBarChart(barChart, entries, labels)
                setupLineChart(lineChart, entries, labels)
                if (lineChart.visibility == View.VISIBLE) {
                    barChart.visibility = View.GONE
                    lineChart.visibility = View.VISIBLE
                    lineChart.invalidate()
                } else {
                    barChart.visibility = View.VISIBLE
                    lineChart.visibility = View.GONE
                    barChart.invalidate()
                }
            }
        }
    }

    private fun loadZamer() {
        lifecycleScope.launch {
            val all = repository.getAll().first()
            val filtered = all.filter { getValueForBodyPart(it) != null }
                .sortedByDescending { it.date }
            if (filtered.isEmpty()) {
                binding.ifNotExist.visibility = View.VISIBLE
                binding.zamerConteiner.visibility = View.GONE
            } else {
                binding.ifNotExist.visibility = View.GONE
                binding.zamerConteiner.visibility = View.VISIBLE
                zamersAdapter.submitList(filtered)
            }
        }
    }

    private fun showAddOrEditZamerDialog(existingZamer: BodyMeasurementsEntity?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_zamer, null)
        val editText = dialogView.findViewById<EditText>(R.id.zamerValue)
        val selectDateButton = dialogView.findViewById<MaterialButton>(R.id.selectDateButton)
        var selectedMeasurementDate: LocalDate = existingZamer?.date ?: LocalDate.now()
        if (existingZamer != null) {
            editText.setText(getValueForBodyPart(existingZamer)?.toString() ?: "")
        }
        selectDateButton.text = selectedMeasurementDate.format(shortDisplayDateFormatter)
        selectDateButton.setOnClickListener {
            showDatePicker { date ->
                selectedMeasurementDate = date
                selectDateButton.text = date.format(shortDisplayDateFormatter)
            }
        }
        val title = if (existingZamer == null) "Добавить замер" else "Редактировать замер"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val valueString = editText.text.toString()
                if (valueString.isBlank()) {
                    Toast.makeText(requireContext(), "Значение не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val value = valueString.toIntOrNull()
                if (value != null) {
                    lifecycleScope.launch {
                        val measurementToSave: BodyMeasurementsEntity =
                            existingZamer?.copy(date = selectedMeasurementDate)
                                ?: BodyMeasurementsEntity(date = selectedMeasurementDate)
                        val finalMeasurement = when (bodyPart) {
                            "Шея" -> measurementToSave.copy(neck = value)
                            "Плечи" -> measurementToSave.copy(shoulders = value)
                            "Грудь" -> measurementToSave.copy(chest = value)
                            "Талия" -> measurementToSave.copy(waist = value)
                            "Таз" -> measurementToSave.copy(pelvis = value)
                            "Предплечья" -> if (isLeft) measurementToSave.copy(forearmsLeft = value) else measurementToSave.copy(forearmsRight = value)
                            "Бицепсы" -> if (isLeft) measurementToSave.copy(bicepsLeft = value) else measurementToSave.copy(bicepsRight = value)
                            "Трицепсы" -> if (isLeft) measurementToSave.copy(tricepsLeft = value) else measurementToSave.copy(tricepsRight = value)
                            "Бедра" -> if (isLeft) measurementToSave.copy(bedroLeft = value) else measurementToSave.copy(begroRight = value)
                            "Икры" -> if (isLeft) measurementToSave.copy(ikriLeft = value) else measurementToSave.copy(ikriRight = value)
                            else -> measurementToSave
                        }
                        repository.insertOrUpdate(finalMeasurement)
                        loadZamer()
                        updateChartsVisibility()
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(requireContext(), "Введите корректное число", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getValueForBodyPart(measurement: BodyMeasurementsEntity): Int? {
        return when (bodyPart) {
            "Шея" -> measurement.neck
            "Плечи" -> measurement.shoulders
            "Грудь" -> measurement.chest
            "Талия" -> measurement.waist
            "Таз" -> measurement.pelvis
            "Предплечья" -> if (isLeft) measurement.forearmsLeft else measurement.forearmsRight
            "Бицепсы" -> if (isLeft) measurement.bicepsLeft else measurement.bicepsRight
            "Трицепсы" -> if (isLeft) measurement.tricepsLeft else measurement.tricepsRight
            "Бедра" -> if (isLeft) measurement.bedroLeft else measurement.begroRight
            "Икры" -> if (isLeft) measurement.ikriLeft else measurement.ikriRight
            else -> null
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private suspend fun loadChartData(): Pair<List<Entry>, List<String>> {
        val allMeasurements = repository.getAll().first()
        val formatter = DateTimeFormatter.ofPattern("dd.MM")
        val filteredAndSorted = allMeasurements
            .mapNotNull { measurement ->
                getValueForBodyPart(measurement)?.let { value ->
                    Pair(measurement.date, value)
                }
            }
            .sortedBy { it.first }
        val entries = filteredAndSorted.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second.toFloat())
        }
        val labels = filteredAndSorted.map { it.first.format(formatter) }
        return Pair(entries, labels)
    }

    private fun setupBarChart(chart: BarChart, entries: List<Entry>, labels: List<String>) {
        val barEntries = entries.map { BarEntry(it.x, it.y) }
        val dataSet = BarDataSet(barEntries, bodyPart).apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue_500)
            valueTextColor = Color.WHITE
            valueTextSize = 16f
            valueFormatter = DefaultValueFormatter(0)
        }
        chart.data = BarData(dataSet)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setDrawValueAboveBar(true)
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(labels)
            setLabelCount(labels.size, false)
            textColor = Color.WHITE
        }
        chart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
            textColor = Color.WHITE
            textSize = 16f
        }
        chart.axisRight.isEnabled = false
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.animateY(1000)
        chart.invalidate()
    }

    private fun setupLineChart(chart: LineChart, entries: List<Entry>, labels: List<String>) {
        val dataSet = LineDataSet(entries, bodyPart).apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue_500)
            valueTextColor = Color.WHITE
            valueTextSize = 16f
            valueFormatter = DefaultValueFormatter(0)
            circleRadius = 4f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.blue_100)
            fillAlpha = 100
        }
        chart.data = LineData(dataSet)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(labels)
            setLabelCount(labels.size, false)
            textColor = Color.WHITE
        }
        chart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
            textColor = Color.WHITE
            textSize = 16f
        }
        chart.axisRight.isEnabled = false
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.animateX(1000)
        chart.invalidate()
    }


    // --- DATE PICKER FUNCTION AND HELPER CLASSES ---
    // (This is your provided showDatePicker function, with its helper classes)
    // Make sure DialogDatePickerBinding is correctly generated from your dialog_date_picker.xml
    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val bindingCalendar = DialogDatePickerBinding.inflate(LayoutInflater.from(requireContext())) // Renamed to avoid conflict
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(bindingCalendar.root)
            .create()

        val today = LocalDate.now()
        var selectedDate = today // Initially selected date for the picker
        val daysOfWeek = daysOfWeek(DayOfWeek.MONDAY) // Make sure daysOfWeek() is defined
        val startMonth = YearMonth.now().minusMonths(12)
        val endMonth = YearMonth.now().plusMonths(12)

        val calendarView = bindingCalendar.calendarView
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(today.yearMonth)

        fun updateMonthYearText(month: YearMonth) {
            val formatter = DateTimeFormatter.ofPattern("LLL yyyy", Locale("ru"))
            val text = month.format(formatter).replaceFirstChar { it.uppercase() }
            bindingCalendar.monthYearTextDatepicker.text = text
        }
        updateMonthYearText(today.yearMonth)
        calendarView.monthScrollListener = {
            updateMonthYearText(it.yearMonth)
        }

        bindingCalendar.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderContainer> {
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
        calendarView.dayBinder = object : MonthDayBinder<MonthDayContainer> {
            override fun create(view: View) = MonthDayContainer(view)
            override fun bind(container: MonthDayContainer, data: CalendarDay) {
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                val calendarColor = ContextCompat.getColor(requireContext(), R.color.Calendar)
                val calendarSetColor = ContextCompat.getColor(requireContext(), R.color.CalendarSelect)
                val todayColor = ContextCompat.getColor(requireContext(), R.color.CalendarToday)

                textView.setTextColor(calendarColor)
                textView.background = null
                textView.alpha = if (data.position == DayPosition.MonthDate) 1f else 0.5f

                when {
                    data.date == selectedDate -> { // Highlight the initially passed selectedDate
                        textView.setTextColor(calendarSetColor)
                        textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.selected_day_bg)
                    }
                    data.date == today && data.date != selectedDate -> { // Ensure 'today' styling doesn't override 'selected'
                        textView.setTextColor(todayColor)
                        textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_today_style)
                    }
                }
                container.view.setOnClickListener {
                    if (data.position == DayPosition.MonthDate) { // Only allow selection of dates in the current month
                        val oldDate = selectedDate
                        selectedDate = data.date
                        calendarView.notifyDateChanged(data.date) // Update the new selected day
                        oldDate?.let { calendarView.notifyDateChanged(it) } // Update the old selected day
                        onDateSelected(data.date)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    // Helper for daysOfWeek, if not already globally available
    // (Consider moving this to a utility file if used elsewhere)
    private fun daysOfWeek(firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek): List<DayOfWeek> {
        val daysOfWeek = DayOfWeek.values()
        if (firstDayOfWeek != daysOfWeek.first()) {
            val tailoredDays = daysOfWeek.toList().toMutableList()
            while (tailoredDays.first() != firstDayOfWeek) {
                tailoredDays.add(tailoredDays.removeAt(0))
            }
            return tailoredDays
        }
        return daysOfWeek.toList()
    }


    // Companion object and onDestroyView (no changes from your original)
    companion object {
        fun newInstance(bodyPart: String, isLeft: Boolean): BSDZamers {
            val fragment = BSDZamers()
            fragment.arguments = Bundle().apply {
                putString("bodyPart", bodyPart)
                putBoolean("isLeft", isLeft)
            }
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// These classes should be defined, either nested as here, or as separate classes in the same file.
// Make sure R.id.dayText is defined in the layout used by MonthDayContainer (e.g., calendar_day_layout.xml)
class MonthDayContainer(view: View) : ViewContainer(view) {
    // Assuming R.id.dayText is in your day layout file for the calendar
    val textView: TextView = view.findViewById(R.id.dayText)
}

class MonthHeaderContainer(view: View) : ViewContainer(view) {
    // Assuming the root of your month header layout is a ViewGroup
    val titlesContainer: ViewGroup = view as ViewGroup
}