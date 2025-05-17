package com.example.vkr2.ui.AdaptersDirectory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.databinding.ViewholderForStatsAllBinding
import com.example.vkr2.ui.dashboard.DashboardItem

class DashboardAdapter(
    private var items: List<DashboardItem.GeneralStatItem>
):RecyclerView.Adapter<DashboardAdapter.StatsViewHolder>() {
    inner class StatsViewHolder(val binding: ViewholderForStatsAllBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val binding = ViewholderForStatsAllBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return StatsViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvMetricName.text = item.label
        holder.binding.tvMetricValue.text = item.value
    }

    @SuppressLint("NotifyDataSetChanged")
    fun  updateData(newItem: List<DashboardItem.GeneralStatItem>){
        items = newItem
        notifyDataSetChanged()
    }
}