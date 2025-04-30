package com.example.vkr2.ui.home.TrainingsDay

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.databinding.ItemsSetsBinding
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SetsAdapter(
    private val sets: MutableList<SetEntity>,
    private val onUpdateSet: (SetEntity) -> Unit,
    private val onDeleteSet: ((SetEntity)->Unit)? = null,
) : RecyclerView.Adapter<SetsAdapter.SetsViewHolder>() {

    inner class SetsViewHolder(val binding: ItemsSetsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemsSetsBinding.inflate(inflater, parent, false)
        return SetsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetsViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.setNumber.text = "${position + 1}."

        val editWeight = holder.binding.editWeight
        val editReps = holder.binding.editReps

        holder.binding.editWeight.addTextChangedListener {
            val newWeight = it.toString().toIntOrNull() ?: 0
            if (newWeight != set.weight) {
                set.weight = newWeight
            }
        }

        holder.binding.editReps.addTextChangedListener {
            val newReps = it.toString().toIntOrNull() ?: 0
            if (newReps != set.reps) {
                set.reps = newReps
            }
        }

        holder.itemView.setOnLongClickListener {
            showDeleteConfirm(holder.adapterPosition, holder.itemView.context)
            true
        }
        // Убираем слушатели, чтобы не срабатывали повторно при переиспользовании
        editWeight.setOnFocusChangeListener(null)
        editReps.setOnFocusChangeListener(null)

        editWeight.setText(set.weight.takeIf { it != 0 }?.toString() ?: "")
        editWeight.hint = if (set.weight == 0) "0" else ""
        editWeight.setOnFocusChangeListener { v, hasFocus ->
            val et = v as EditText
            if (hasFocus && et.text.isNotEmpty()) {
                et.hint = et.text.toString()
                et.setText("")
            } else if (!hasFocus && et.text.isNullOrBlank()) {
                et.setText(et.hint)
                et.hint = ""
            }

            val newWeight = et.text.toString().toIntOrNull() ?: 0
            if (newWeight != set.weight) {
                set.weight = newWeight
                onUpdateSet(set)
            }
        }

        editReps.setText(set.reps.takeIf { it != 0 }?.toString() ?: "")
        editReps.hint = if (set.reps == 0) "0" else ""
        editReps.setOnFocusChangeListener { v, hasFocus ->
            val et = v as EditText
            if (hasFocus && et.text.isNotEmpty()) {
                et.hint = et.text.toString()
                et.setText("")
            } else if (!hasFocus && et.text.isNullOrBlank()) {
                et.setText(et.hint)
                et.hint = ""
            }

            val newReps = et.text.toString().toIntOrNull() ?: 0
            if (newReps != set.reps) {
                set.reps = newReps
                onUpdateSet(set)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDeleteConfirm(position: Int, context: Context) {
        val set = sets[position]

        MaterialAlertDialogBuilder(context)
            .setTitle("Удалить подход?")
            .setMessage("Вы действительно хотите удалить этот подход?")
            .setPositiveButton("Удалить") { _, _ ->
                if (sets.size == 1) {
                    set.weight = 0
                    set.reps = 0
                    notifyItemChanged(position)
                    notifyDataSetChanged()
                    onUpdateSet(set) // сохраняем изменения
                } else {
                    // Удаляем как обычно
                    val removedSet = sets.removeAt(position)
                    notifyItemRemoved(position)
                    notifyDataSetChanged()
                    onDeleteSet?.invoke(removedSet)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }



    override fun getItemCount(): Int = sets.size

    fun getCurrentSets():List<SetEntity> = sets

    fun addSet(set: SetEntity) {
        sets.add(set)
        notifyItemInserted(sets.lastIndex)
    }
}
