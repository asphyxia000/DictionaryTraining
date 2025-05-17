package com.example.vkr2.ui.AdaptersDirectory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vkr2.DataBase.Exercises.ExercisesEntity
import com.example.vkr2.R
import com.google.android.material.card.MaterialCardView

class ExerciseAdapter(private var items: List<ExercisesEntity>,
    private val onExerciseSelected:(ExercisesEntity, Boolean, Boolean)->Unit):
RecyclerView.Adapter<ExerciseAdapter.ExercisesViewHolder>()
{
    private val selectedIds = mutableListOf<Int>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<ExercisesEntity>){
        items=newItems
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelectin(){
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun getSelectedExercises(): List<ExercisesEntity>{
        return items.filter { selectedIds.contains(it.ExercisesId) }
    }

    inner class ExercisesViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.imageView4)
        val textView: TextView = itemView.findViewById(R.id.exercises_name)
        val cardView: MaterialCardView = itemView as MaterialCardView
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExercisesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_exercise,parent,false)
        return ExercisesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExercisesViewHolder, position: Int) {
        val currentItem = items[position]
        val isSelected = selectedIds.contains(currentItem.ExercisesId)
        holder.textView.text = currentItem.ExercisesName

        val context = holder.itemView.context
        val resId = context.resources.getIdentifier(
            currentItem.imagePath.substringBeforeLast('.'),
            "drawable",
            context.packageName
        )

        if (resId!=0){
            // Загрузка картинки через Glide
            Glide.with(context)
                .load(resId) // путь из БД
                .into(holder.imageView)
        }else{
            holder.imageView.setImageResource(R.drawable.biceps)
        }

        if (isSelected){
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context,R.color.blue_100))
            holder.cardView.strokeWidth = 2
            holder.cardView.strokeColor = ContextCompat.getColor(holder.itemView.context,R.color.blue_500)
        }
        else{
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context,R.color.viewholder_exp))
            holder.cardView.strokeWidth = 0
        }

        holder.itemView.setOnClickListener(){
            val wasSelected = selectedIds.contains(currentItem.ExercisesId)
            if (wasSelected){
                selectedIds.remove(currentItem.ExercisesId)
            }
            else{
                selectedIds.add(currentItem.ExercisesId)
            }
            notifyItemChanged(position)
            onExerciseSelected(currentItem,!wasSelected,false)
        }

        holder.imageView.setOnClickListener(){
            onExerciseSelected(currentItem,false,true)
        }
    }

    fun setInitialSelection(ids: List<Int>) {
        selectedIds.clear()
        selectedIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}