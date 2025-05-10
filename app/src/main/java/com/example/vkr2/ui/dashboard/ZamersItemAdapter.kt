package com.example.vkr2.ui.dashboard

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import com.example.vkr2.databinding.ViewholderForBodyBinding
import java.time.format.DateTimeFormatter

class ZamersItemAdapter(
  private val bodyPart: String,
    private val isLeft: Boolean
) :RecyclerView.Adapter<ZamersItemAdapter.ZamerViewHolder>() {
    private var items = listOf<BodyMeasurementsEntity>()

    inner class ZamerViewHolder(var binding: ViewholderForBodyBinding) : RecyclerView.ViewHolder(binding.root)


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(data: List<BodyMeasurementsEntity>){
        items = data.sortedByDescending { it.date }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ZamersItemAdapter.ZamerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewholderForBodyBinding.inflate(inflater,parent,false)
        return ZamerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ZamersItemAdapter.ZamerViewHolder, position: Int) {
        val item = items[position]
        holder.binding.dateZamersBody.text = item.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        holder.binding.textView2.text = bodyValue(item).toString()
    }

    private fun bodyValue(measurement: BodyMeasurementsEntity): Int? {
        return when (bodyPart) {
            "Шея" -> measurement.neck
            "Плечи" -> measurement.shoulders
            "Грудь" -> measurement.chest
            "Талия" -> measurement.waist
            "Таз" -> measurement.pelvis
            "Предплечья" -> if (isLeft) measurement.forearmsLeft else measurement.forearmsRight
            "Бицепсы" -> if (isLeft) measurement.bicepsLeft else measurement.bicepsRight
            "Трицепсы" -> if (isLeft) measurement.tricepsLeft else measurement.tricepsRight
            "Бедра" -> if (isLeft) measurement.bedroLeft else measurement.bicepsRight
            "Икры" -> if (isLeft) measurement.ikriLeft else measurement.ikriRight
            else -> null
        }
    }
    override fun getItemCount(): Int = items.size
}