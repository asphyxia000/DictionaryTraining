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
import androidx.core.content.ContextCompat
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
        holder.itemView.setOnClickListener(){
            onExercisesClick(item.exercise)
        }
        holder.name.text = item.exercise.ExercisesName
        val filteredSets = item.sets.filter { it.trainingId == trainingId }
        if (item.sets.any { it.trainingId != trainingId }) {
            Log.w("AdapterWarning", "Найдены подходы от других тренировок для упражнения ${item.exercise.ExercisesName}")
        }
        val setsText = filteredSets.joinToString(separator = ", ") { "${it.weight} кг × ${it.reps}" }
        holder.sets.text = if (setsText.isNotBlank()) setsText else "0 кг × 0"

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

    fun updateList(newList: List<ExerciseWithSets>) {
        items = newList
        notifyDataSetChanged()
    }
}