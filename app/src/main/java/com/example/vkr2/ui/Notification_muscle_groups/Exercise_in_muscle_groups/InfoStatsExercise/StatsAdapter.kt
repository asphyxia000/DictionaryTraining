package com.example.vkr2.ui.Notification_muscle_groups.Exercise_in_muscle_groups.InfoStatsExercise

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.databinding.ItemStatsBinding

class StatsAdapter(private val items: List<StatsItems>):
RecyclerView.Adapter<StatsAdapter.StatsViewHolder>(){

    inner class StatsViewHolder(val binding: ItemStatsBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatsViewHolder {
        val binding = ItemStatsBinding.inflate(
            LayoutInflater.from(parent.context),parent,false
        )
        return StatsViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            statsTitle.text = item.title
            statsValue.text = item.value+(item.unit ?: "")
            statsDate.text = item.date ?: ""
        }
    }

    override fun getItemCount(): Int = items.size
}