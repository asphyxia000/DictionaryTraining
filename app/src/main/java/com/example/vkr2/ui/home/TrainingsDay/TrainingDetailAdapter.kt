package com.example.vkr2.ui.home.TrainingsDay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.DataBase.Relations.ExerciseWithSets
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentTrainingsDetailBinding

class TrainingDetailAdapter(
    private val onExercisesClick:(ExercisesEntity)->Unit
): RecyclerView.Adapter<TrainingDetailAdapter.ExViewHolder>() {

    private var items: List<ExerciseWithSets> = emptyList()

    inner class ExViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.exerciseName)
        val sets: TextView = view.findViewById(R.id.setsInfo)
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
        val setsText = item.sets.joinToString(separator = ", ") { "${it.weight} кг × ${it.reps}" }
        holder.sets.text = if (setsText.isNotBlank()) setsText else "0 кг × 0"
    }

    fun updateList(newList: List<ExerciseWithSets>) {
        items = newList
        notifyDataSetChanged()
    }
}