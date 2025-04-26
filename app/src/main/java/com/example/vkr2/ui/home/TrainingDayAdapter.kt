package com.example.vkr2.ui.home

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.ViewholderTrainingdayBinding
import kotlinx.coroutines.awaitCancellation
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class TrainingDayAdapter (
    private var items: List<TrainingsEntity>,
    private val onItemClick: (TrainingsEntity)->Unit,
    private val onEdit: (TrainingsEntity)-> Unit,
    private val onDelete: (TrainingsEntity)->Unit
):RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    inner class TrainingDayViewHolder(private val binding: ViewholderTrainingdayBinding):
            RecyclerView.ViewHolder(binding.root){
                fun bind(items: TrainingsEntity){
                    val date=items.date
                    val day=date.dayOfMonth
                    val month = date.month.getDisplayName(TextStyle.FULL, Locale("ru")).replaceFirstChar { it.uppercaseChar() }
                    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL,Locale("ru")).replaceFirstChar { it.uppercaseChar() }
                    binding.trainingDate.text = "$day $month, $dayOfWeek"
                    binding.Nametrainingday.text=items.name.replaceFirstChar { it.uppercaseChar() }
                    if (items.comment.isNullOrBlank()) {
                        binding.Commenttrainingday.visibility = View.GONE
                    } else {
                        binding.Commenttrainingday.visibility = View.VISIBLE
                        binding.Commenttrainingday.text = items.comment.replaceFirstChar { it.uppercaseChar() }
                        binding.Commenttrainingday.setBackgroundResource(R.drawable.backforcomments)
                    }
                    binding.root.setOnClickListener{
                        onItemClick(items)
                    }
                    binding.optionsMenu.setOnClickListener {
                        val popup = PopupMenu(binding.root.context, binding.optionsMenu, Gravity.START, 0, R.style.MyPopupMenu)
                        popup.inflate(R.menu.training_day_menu)
                        popup.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_trainingdayedit -> {
                                    onEdit(items)
                                    true
                                }
                                R.id.action_trainingdaydelete -> {
                                    onDelete(items)
                                    true
                                }
                                else -> false
                            }
                        }
                        popup.show()
                    }
                    Log.d("TrainingAdapter","Binding training:${items.name} on ${items.date}")

                }
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ViewholderTrainingdayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrainingDayViewHolder(binding)

    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TrainingDayViewHolder && position<items.size){
            holder.bind(items[position])
        }
    }

    fun updateList(newList: List<TrainingsEntity>){
        items = newList
        notifyDataSetChanged()
    }


}