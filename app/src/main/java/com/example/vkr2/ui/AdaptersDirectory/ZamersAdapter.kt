// ZamersAdapter.kt
package com.example.vkr2.ui.AdaptersDirectory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.R
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

    override fun onBindViewHolder(holder: ZamersViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvBodyPart.text = item.label

        val isDoubleSided = item.label in listOf("Предплечья", "Бицепсы", "Трицепсы", "Бедра", "Икры")

        // Устанавливаем текст для etLeft
        holder.binding.etLeft.text = item.left?.toString() ?: "-"

        // Видимость и текст для etRight
        if (isDoubleSided) {
            holder.binding.etRight.visibility = View.VISIBLE
            holder.binding.divider.visibility = View.VISIBLE
            holder.binding.etRight.text = item.right?.toString() ?: "-" // Устанавливаем текст для etRight
        } else {
            holder.binding.divider.visibility = View.GONE
            holder.binding.etRight.visibility = View.GONE
        }

        if (item.right != null) {
            holder.binding.divider.visibility = View.VISIBLE
            holder.binding.rightContainer.visibility = View.VISIBLE

            val params = holder.binding.leftContainer.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToStart = holder.binding.divider.id
            holder.binding.leftContainer.layoutParams = params
        } else {
            holder.binding.divider.visibility = View.GONE
            holder.binding.rightContainer.visibility = View.GONE

            val params = holder.binding.leftContainer.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            holder.binding.leftContainer.layoutParams = params
        }

        // ==== ЛОГИКА ДЛЯ LEFT ====
        if (item.left != null && item.prevLeft != null) {
            val diff = item.left - item.prevLeft
            holder.binding.leftDiffContainer.visibility = View.VISIBLE
            holder.binding.leftDiff.text = diff.toString()

            if (diff > 0) {
                holder.binding.leftArrow.setImageResource(R.drawable.ic_arrow_up)
                holder.binding.leftArrow.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light)
                )
                holder.binding.leftDiff.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light)
                )
            } else {
                holder.binding.leftArrow.setImageResource(R.drawable.ic_arrow_down)
                holder.binding.leftArrow.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_light)
                )
                holder.binding.leftDiff.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_light)
                )
            }
        } else {
            holder.binding.leftDiffContainer.visibility = View.GONE
        }
        // ==== ЛОГИКА ДЛЯ RIGHT ====
        if (isDoubleSided && item.right != null && item.prevRight != null) {
            val diff = item.right - item.prevRight
            holder.binding.rightDiffContainer.visibility = View.VISIBLE
            holder.binding.rightDiff.text = diff.toString()

            if (diff > 0) {
                holder.binding.rightArrow.setImageResource(R.drawable.ic_arrow_up)
                holder.binding.rightArrow.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light)
                )
                holder.binding.rightDiff.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_light)
                )
            } else {
                holder.binding.rightArrow.setImageResource(R.drawable.ic_arrow_down)
                holder.binding.rightArrow.setColorFilter(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_light)
                )
                holder.binding.rightDiff.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_light)
                )
            }
        } else {
            holder.binding.rightDiffContainer.visibility = View.GONE
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