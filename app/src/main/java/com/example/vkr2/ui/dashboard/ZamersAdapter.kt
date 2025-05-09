package com.example.vkr2.ui.dashboard

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.databinding.ViewholderForZamersBinding

class ZamersAdapter (
    private var items: List<DashboardItem.BodyMeasurementItem>
):RecyclerView.Adapter<ZamersAdapter.ZamersViewHolder>(){
    inner class ZamersViewHolder(val binding: ViewholderForZamersBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZamersViewHolder {
        val binding = ViewholderForZamersBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ZamersViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ZamersViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvBodyPart.text = item.label
        holder.binding.etLeft.setText(item.left?.toString()?:"")
        holder.binding.etRight.setText(item.right?.toString()?:"")
        holder.binding.etLeft.isEnabled = false
        holder.binding.etRight.isEnabled = false
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<DashboardItem.BodyMeasurementItem>)
    {
        items = newItems
        notifyDataSetChanged()
    }
}