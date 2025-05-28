package com.example.vkr2.ui.AdaptersDirectory

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.ItemsSetsBinding
import com.example.vkr2.databinding.ItemsSetsCardioDistanceBinding
import com.example.vkr2.databinding.ItemsSetsCardioRepsBinding
import com.example.vkr2.ui.home.TrainingsDay.ExerciseType
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SetsAdapter(
    private val sets: MutableList<SetEntity>,
    private val exerciseType: ExerciseType,
    private val onUpdateSet: (SetEntity) -> Unit,
    private val onDeleteSet: ((SetEntity) -> Unit)? = null,
) : RecyclerView.Adapter<SetsAdapter.BaseViewHolder>() {

    companion object {
        private const val VIEW_TYPE_STRENGTH = 0
        private const val VIEW_TYPE_CARDIO_DISTANCE = 1
        private const val VIEW_TYPE_CARDIO_TIME_REPS = 2
    }

    abstract class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(set: SetEntity, position: Int)
        abstract fun clearListeners()
    }

    inner class StrengthViewHolder(
        val binding: ItemsSetsBinding // Используем ItemsSetsBinding согласно onCreateViewHolder
    ) : BaseViewHolder(binding.root) {
        override fun bind(set: SetEntity, position: Int) {
            binding.setNumber.text = "${position + 1}."
            // Передаем 'position' в setupEditText
            setupEditText(binding.editWeight, set.weight, position) { updatedValue -> set.weight = updatedValue }
            setupEditText(binding.editReps, set.reps, position) { updatedValue -> set.reps = updatedValue }
        }
        override fun clearListeners() {
            binding.editWeight.onFocusChangeListener = null
            binding.editReps.onFocusChangeListener = null
            // Очищаем TextChangedListeners, если они добавлялись бы напрямую
            // binding.editWeight.removeTextChangedListener(...)
            // binding.editReps.removeTextChangedListener(...)
        }
    }

    inner class CardioDistanceViewHolder(
        val binding: ItemsSetsCardioDistanceBinding // Используем ItemsSetsCardioDistanceBinding
    ) : BaseViewHolder(binding.root) {
        override fun bind(set: SetEntity, position: Int) {
            binding.setNumber.text = "${position + 1}."
            setupEditText(binding.editMinutes, set.minutes, position) { updatedValue -> set.minutes = updatedValue }
            setupEditText(binding.editSeconds, set.seconds, position) { updatedValue -> set.seconds = updatedValue }
            setupEditText(binding.editDistance, set.distanceKm, position) { updatedValue -> set.distanceKm = updatedValue }
        }
        override fun clearListeners() {
            binding.editMinutes.onFocusChangeListener = null
            binding.editSeconds.onFocusChangeListener = null
            binding.editDistance.onFocusChangeListener = null
        }
    }

    inner class CardioTimeRepsViewHolder(
        val binding: ItemsSetsCardioRepsBinding // Используем ItemsSetsCardioRepsBinding
    ) : BaseViewHolder(binding.root) {
        override fun bind(set: SetEntity, position: Int) {
            binding.setNumber.text = "${position + 1}."
            setupEditText(binding.editMinutes, set.minutes, position) { updatedValue -> set.minutes = updatedValue }
            setupEditText(binding.editSeconds, set.seconds, position) { updatedValue -> set.seconds = updatedValue }
            setupEditText(binding.editReps, set.reps, position) { updatedValue -> set.reps = updatedValue }
        }
        override fun clearListeners() {
            binding.editMinutes.onFocusChangeListener = null
            binding.editSeconds.onFocusChangeListener = null
            binding.editReps.onFocusChangeListener = null
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (exerciseType) {
            ExerciseType.CARDIO_DISTANCE -> VIEW_TYPE_CARDIO_DISTANCE
            ExerciseType.CARDIO_TIME_REPS -> VIEW_TYPE_CARDIO_TIME_REPS
            else -> VIEW_TYPE_STRENGTH // ExerciseType.STRENGTH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CARDIO_DISTANCE -> CardioDistanceViewHolder(
                ItemsSetsCardioDistanceBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_CARDIO_TIME_REPS -> CardioTimeRepsViewHolder(
                ItemsSetsCardioRepsBinding.inflate(inflater, parent, false)
            )
            else -> StrengthViewHolder( // VIEW_TYPE_STRENGTH
                ItemsSetsBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val set = sets[position]
        holder.clearListeners() // Сначала очищаем старые слушатели
        holder.bind(set, position) // 'position' здесь доступна

        holder.itemView.setOnLongClickListener {
            showDeleteConfirm(holder.bindingAdapterPosition, holder.itemView.context)
            true
        }
    }

    // Универсальная функция для настройки EditText (Int)
    private fun setupEditText(editText: EditText, value: Int?, itemPosition: Int, updateAction: (Int) -> Unit) {
        editText.setText(value?.takeIf { it != 0 }?.toString() ?: "")
        editText.hint = if (value == 0 || value == null) "0" else "" // Показываем "0" если значение 0 или null
        editText.tag = itemPosition // Сохраняем позицию элемента списка

        editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            val et = v as EditText
            val currentSetPosition = et.tag as Int // Получаем сохраненную позицию
            if (currentSetPosition >= 0 && currentSetPosition < sets.size) { // Проверка валидности позиции
                handleFocusChange(et, hasFocus) { textValue ->
                    val intValue = textValue.toIntOrNull() ?: 0
                    updateAction(intValue)
                    onUpdateSet(sets[currentSetPosition]) // Обновляем сет по сохраненной позиции
                }
            }
        }
    }

    // Универсальная функция для настройки EditText (Float)
    private fun setupEditText(editText: EditText, value: Float?, itemPosition: Int, updateAction: (Float) -> Unit) {
        editText.setText(value?.takeIf { it != 0f }?.toString() ?: "")
        editText.hint = if (value == 0f || value == null) "0.0" else "" // Показываем "0.0" если значение 0f или null
        editText.tag = itemPosition // Сохраняем позицию элемента списка

        editText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            val et = v as EditText
            val currentSetPosition = et.tag as Int // Получаем сохраненную позицию
            if (currentSetPosition >= 0 && currentSetPosition < sets.size) { // Проверка валидности позиции
                handleFocusChange(et, hasFocus) { textValue ->
                    val floatValue = textValue.toFloatOrNull() ?: 0f
                    updateAction(floatValue)
                    onUpdateSet(sets[currentSetPosition]) // Обновляем сет по сохраненной позиции
                }
            }
        }
    }

    // Обработчик фокуса
    private fun handleFocusChange(et: EditText, hasFocus: Boolean, saveAction: (String) -> Unit) {
        if (hasFocus) {
            if (et.text.toString() == "0" || et.text.toString() == "0.0") {
                et.hint = et.text.toString() // Сохраняем "0" или "0.0" в hint
                et.setText("") // Очищаем для ввода
            } else if (et.text.isNotEmpty()) {
                et.hint = et.text.toString()
                et.setText("")
            }
        } else { // Потеря фокуса
            if (et.text.isNullOrBlank()) {
                et.setText(et.hint.takeIf { !it.isNullOrEmpty() } ?: if (et.inputType and android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL != 0) "0.0" else "0")
            }
            saveAction(et.text.toString())
            // Обновляем hint после сохранения, если значение стало 0
            if (et.text.toString() == "0" || et.text.toString() == "0.0") {
                et.hint = et.text.toString()
            } else {
                et.hint = ""
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteConfirm(position: Int, context: Context) {
        if (position < 0 || position >= sets.size) return // Проверка на валидность позиции

        val set = sets[position]

        val dialog = MaterialAlertDialogBuilder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Удалить подход?")
            .setMessage("Вы действительно хотите удалить этот подход?")
            .setPositiveButton("Удалить") { _, _ ->
                if (sets.size == 1) {
                    set.weight = null // Используем null для сброса
                    set.reps = null
                    set.minutes = null
                    set.seconds = null
                    set.distanceKm = null
                    notifyItemChanged(position)
                    onUpdateSet(set)
                } else {
                    sets.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, sets.size - position) // Обновляем отображение номеров подходов
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(context, R.color.blue_500))
        }

        dialog.show()
    }

    override fun getItemCount(): Int = sets.size

    fun getCurrentSets(): List<SetEntity> = sets.toList() // Возвращаем копию для безопасности

    @SuppressLint("NotifyDataSetChanged") // Используем если добавляем/удаляем много элементов или меняем порядок
    fun addSet(set: SetEntity) {
        sets.add(set)
        notifyItemInserted(sets.lastIndex)
    }
}
