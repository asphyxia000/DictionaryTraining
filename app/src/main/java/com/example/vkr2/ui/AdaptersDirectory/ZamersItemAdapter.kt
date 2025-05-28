package com.example.vkr2.ui.AdaptersDirectory

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Measurements.BodyMeasurementsEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.ViewholderForBodyBinding
import java.time.format.DateTimeFormatter

class ZamersItemAdapter(
    private val bodyPart: String,
    private val isLeft: Boolean,
    private val onEditClick: (BodyMeasurementsEntity) -> Unit,
    private val onDeleteClick: (BodyMeasurementsEntity) -> Unit
) : RecyclerView.Adapter<ZamersItemAdapter.ZamerViewHolder>() {

    private var items = listOf<BodyMeasurementsEntity>()

    inner class ZamerViewHolder(val binding: ViewholderForBodyBinding) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(data: List<BodyMeasurementsEntity>) {
        items = data.sortedWith(compareByDescending<BodyMeasurementsEntity> { it.date }
            .thenByDescending { it.id })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZamerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewholderForBodyBinding.inflate(inflater, parent, false)
        return ZamerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ZamerViewHolder, position: Int) {
        val item = items[position]
        holder.binding.dateZamersBody.text = item.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        holder.binding.textView2.text = bodyValue(item)?.toString() ?: "-"

        holder.binding.optionsMenu.setOnClickListener { v ->
            val popup = PopupMenu(v.context, v, Gravity.END, 0, R.style.MyPopupMenu)
            popup.menu.add(0, 1, 0, "Редактировать")
            popup.menu.add(0, 2, 1, "Удалить")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    1 -> {
                        onEditClick(item)
                        true
                    }
                    2 -> {
                        onDeleteClick(item)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

    }

    override fun getItemCount(): Int = items.size

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
            "Бедра" -> if (isLeft) measurement.bedroLeft else measurement.begroRight
            "Икры" -> if (isLeft) measurement.ikriLeft else measurement.ikriRight
            else -> null
        }
    }
}
