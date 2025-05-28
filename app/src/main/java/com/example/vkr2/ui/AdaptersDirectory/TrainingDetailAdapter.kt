package com.example.vkr2.ui.AdaptersDirectory

import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.Relations.ExerciseWithSets
import com.example.vkr2.R
import androidx.appcompat.widget.PopupMenu
import android.view.Gravity
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.vkr2.ui.home.TrainingsDay.ExerciseType // Import ExerciseType
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class TrainingDetailAdapter(
    private val trainingId:Int,
    private val onExercisesClick:(ExercisesEntity)->Unit,
    private val onDeleteExercise: (ExercisesEntity) -> Unit
): RecyclerView.Adapter<TrainingDetailAdapter.ExViewHolder>() {

    private var items: List<ExerciseWithSets> = emptyList()

    inner class ExViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.exerciseName)
        val sets: TextView = view.findViewById(R.id.setsInfo)
        val optionsMenu:View = view.findViewById(R.id.optionsMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_in_training, parent, false)
        return ExViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ExViewHolder, position: Int) {
        val item = items[position]
        val imagePath = item.exercise.imagePath
        val context = holder.itemView.context

        val imageView3 = holder.view.findViewById<ImageView>(R.id.imageView3) // добавьте import android.widget.ImageView

        if (!imagePath.isNullOrEmpty()) {
            val resId = context.resources.getIdentifier(
                imagePath.substringBeforeLast('.'),
                "drawable",
                context.packageName
            )
            if (resId != 0) {
                Glide.with(context)
                    .load(resId)
                    .into(imageView3)
            } else {
                imageView3.setImageResource(R.drawable.biceps)
            }
        } else {
            imageView3.setImageResource(R.drawable.biceps)
        }

        holder.itemView.setOnClickListener(){
            onExercisesClick(item.exercise)
        }
        holder.name.text = item.exercise.ExercisesName
        val filteredSets = item.sets.filter { it.trainingId == trainingId }
        if (item.sets.any { it.trainingId != trainingId }) {
            Log.w("AdapterWarning", "Найдены подходы от других тренировок для упражнения ${item.exercise.ExercisesName}")
        }

        // Determine exercise type
        val exerciseType = try {
            ExerciseType.valueOf(item.exercise.type.uppercase())
        } catch (e: IllegalArgumentException) {
            Log.w("TrainingDetailAdapter", "Invalid exercise type string for ${item.exercise.ExercisesName}: ${item.exercise.type}. Defaulting to STRENGTH.")
            ExerciseType.STRENGTH // Default type if parsing fails
        }

        val setsText = when (exerciseType) {
            ExerciseType.STRENGTH -> {
                filteredSets.joinToString(separator = ", ") { set ->
                    val weight = set.weight ?: 0
                    val reps = set.reps ?: 0
                    "${weight} кг × ${reps}"
                }
            }
            ExerciseType.CARDIO_DISTANCE -> {
                filteredSets.joinToString(separator = ", ") { set ->
                    val minutes = set.minutes ?: 0
                    val seconds = set.seconds ?: 0
                    val distance = set.distanceKm ?: 0f
                    val formattedDistance = String.format("%.1f", distance).replace(",", ".") // Format to one decimal place, use dot for decimal
                    "${minutes} мин : ${String.format("%02d", seconds)} сек / ${formattedDistance} км"
                }
            }
            ExerciseType.CARDIO_TIME_REPS -> {
                filteredSets.joinToString(separator = ", ") { set ->
                    val minutes = set.minutes ?: 0
                    val seconds = set.seconds ?: 0
                    val reps = set.reps ?: 0
                    "${minutes} мин : ${String.format("%02d", seconds)} сек / ${reps} повт"
                }
            }
        }
        holder.sets.text = if (setsText.isNotBlank()) setsText else getEmptySetText(exerciseType)

        // Обработка optionsMenu
        holder.optionsMenu.setOnClickListener { v ->
            val popup = PopupMenu(v.context, v, Gravity.END, 0, R.style.MyPopupMenu)
            popup.inflate(R.menu.training_popup_menu)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_delete_training -> {
                        // Показываем диалог подтверждения
                        MaterialAlertDialogBuilder(v.context,R.style.CustomAlertDialogTheme)
                            .setTitle("Удалить упражнение?")
                            .setMessage("Все подходы будут также удалены. Вы уверены?")
                            .setNegativeButton("Отмена", null)
                            .setPositiveButton("Удалить") { _, _ ->
                                onDeleteExercise(item.exercise)
                            }
                            .show()
                            .getButton(DialogInterface.BUTTON_POSITIVE)
                            ?.setTextColor(ContextCompat.getColor(v.context, R.color.blue_500))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

    }

    private fun getEmptySetText(exerciseType: ExerciseType): String {
        return when (exerciseType) {
            ExerciseType.STRENGTH -> "0 кг × 0"
            ExerciseType.CARDIO_DISTANCE -> "0 мин : 00 сек / 0.0 км"
            ExerciseType.CARDIO_TIME_REPS -> "0 мин : 00 сек / 0 повт"
        }
    }

    fun updateList(newList: List<ExerciseWithSets>) {
        items = newList
        notifyDataSetChanged()
    }
}