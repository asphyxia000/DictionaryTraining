package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Trainings.SetEntity
import com.example.vkr2.databinding.ItemSetStatsBinding

class StatsSetsAdapter(private val sets: List<SetEntity>) :
    RecyclerView.Adapter<StatsSetsAdapter.SetViewHolder>(){

    inner class SetViewHolder(val binding: ItemSetStatsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatsSetsAdapter.SetViewHolder {
        val binding = ItemSetStatsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatsSetsAdapter.SetViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.setNumber.text = "${position+1}"
        holder.binding.weight.text = "${set.weight ?: 0}"
        holder.binding.reps.text = "${set.reps ?:0}"
    }

    override fun getItemCount(): Int = sets.size

}