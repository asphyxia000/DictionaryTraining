package com.example.vkr2.ui.AdaptersDirectory

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.Trainings.TrainingsEntity
import com.example.vkr2.R
import com.example.vkr2.databinding.ViewholderWeekBinding
import com.example.vkr2.ui.home.WeekGroup

class WeekAdapter (
    private val context: Context,
    private var items:List<WeekGroup>,
    private val onItemClick: (TrainingsEntity) -> Unit,
    private val onEdit: (TrainingsEntity) -> Unit,
    private val onDelete: (TrainingsEntity) -> Unit
):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    inner class WeekViewHolder(val binding: ViewholderWeekBinding): RecyclerView.ViewHolder(binding.root)
    inner class FooterViewHolder(view: View):RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        return if (viewType== VIEW_TYPE_WEEK){
            val binding = ViewholderWeekBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            WeekViewHolder(binding)
        }
        else{
            val spacer = View(parent.context)
            spacer.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                parent.context.resources.getDimensionPixelSize(R.dimen.training_footer_spacing)
            )
            FooterViewHolder(spacer)
        }
    }
    override fun getItemCount()=items.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) VIEW_TYPE_FOOTER else VIEW_TYPE_WEEK
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WeekViewHolder && position < items.size) {
            val item = items[position]
            val b = holder.binding

            b.weekTitle.text = item.title
            b.weekCount.text = when {
                item.trainings.size % 100 in 11..14 -> "${item.trainings.size} тренировок"
                item.trainings.size % 10 == 1 -> "${item.trainings.size} тренировка"
                item.trainings.size % 10 in 2..4 -> "${item.trainings.size} тренировки"
                else -> "${item.trainings.size} тренировок"
            }


            val innerAdapter = TrainingDayAdapter(item.trainings,  onItemClick = onItemClick, onEdit = onEdit, onDelete = onDelete)
            b.weekRecycler.layoutManager = LinearLayoutManager(context)
            b.weekRecycler.adapter = innerAdapter
            b.weekRecycler.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

            b.headerContainer.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(position)
            }
            b.buttonclose.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(position)
            }


        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<WeekGroup>){
        items = newList
        notifyDataSetChanged()
    }
    companion object {
        const val VIEW_TYPE_WEEK = 0
        const val VIEW_TYPE_FOOTER = 1
    }
}