package com.example.vkr2.ui.AdaptersDirectory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.MuscleGroup.MuscleGroupEntity
import com.example.vkr2.R
import com.google.android.material.imageview.ShapeableImageView

class NoExpAdapter(private var items: List<MuscleGroupEntity>, // Принимаем список сущностей
                   private val onItemClick: (MuscleGroupEntity) -> Unit) :
    RecyclerView.Adapter<NoExpAdapter.ExerciseViewHolder>() {



    private val muscleIcons = mapOf(
        "Грудь" to R.drawable.grud,
        "Руки" to R.drawable.biceps,
        "Ноги" to R.drawable.legs,
        "Спина" to R.drawable.back,
        "Плечи" to R.drawable.plechi,
        "Корпус" to R.drawable.press,
        "Кардио" to R.drawable.cardio,
        "Фулбоди" to R.drawable.fullbody
    )

    // Добавляем метод для обновления данных
    fun updateData(newItems: List<MuscleGroupEntity>) {
        if (items != newItems) {
            items = newItems
            notifyDataSetChanged()
        }
    }


    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.imageView3)
        val textView: TextView = itemView.findViewById(R.id.textnameexp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_exp, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentItem = items[position]

        // Устанавливаем текст
        holder.textView.text = currentItem.NameMuscleGroups

        // Устанавливаем иконку из маппинга
        muscleIcons[currentItem.NameMuscleGroups]?.let {
            holder.imageView.setImageResource(it)
        } ?: run {
            // Иконка по умолчанию, если не найдена
            holder.imageView.setImageResource(R.drawable.biceps)
        }

        // Обработчик клика
        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }


    override fun getItemCount() = items.size
}
