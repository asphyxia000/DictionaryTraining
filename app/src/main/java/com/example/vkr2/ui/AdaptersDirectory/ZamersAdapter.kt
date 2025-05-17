package com.example.vkr2.ui.AdaptersDirectory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.databinding.ViewholderForZamersBinding
import com.example.vkr2.ui.dashboard.DashboardItem

class ZamersAdapter (
    private var items: List<DashboardItem.BodyMeasurementItem>,
    private val onItemClick: (String,Boolean)->Unit
):RecyclerView.Adapter<ZamersAdapter.ZamersViewHolder>(){
    inner class ZamersViewHolder(val binding: ViewholderForZamersBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZamersViewHolder {
        val binding = ViewholderForZamersBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ZamersViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    // ZamersAdapter.kt
    override fun onBindViewHolder(holder: ZamersViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvBodyPart.text = item.label

        val isDoubleSided = item.label in listOf("Предплечья", "Бицепсы", "Трицепсы", "Бедра", "Икры")

        // Устанавливаем текст для etLeft
        holder.binding.etLeft.setText(item.left?.toString() ?: "-") // Используем value1

        // Видимость и текст для etRight
        if (isDoubleSided) {
            holder.binding.etRight.visibility = View.VISIBLE
        } else {
            holder.binding.divider.visibility = View.GONE
            holder.binding.etRight.visibility = View.GONE
        }

        // Нажатия (остаются без изменений)
        holder.binding.etLeft.isFocusable = false
        holder.binding.etLeft.isClickable = true
        holder.binding.etLeft.setOnClickListener {
            onItemClick(item.label, true) // true для левого/одиночного
        }

        if (isDoubleSided) {
            holder.binding.etRight.isFocusable = false
            holder.binding.etRight.isClickable = true
            holder.binding.etRight.setOnClickListener {
                onItemClick(item.label, false) // false для правого
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<DashboardItem.BodyMeasurementItem>)
    {
        items = newItems
        notifyDataSetChanged()
    }
}