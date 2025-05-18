package com.example.vkr2.ui.settings

import android.text.format.DateUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.R
import com.example.vkr2.databinding.ViewholderBackupBinding

class BackupAdapter(
    private val items:List<BackupInfo>,
    private val onRestoreClick: (BackupInfo)->Unit,
    private val onDeleteClick: (BackupInfo)->Unit,
    private val onShareClick: (BackupInfo)->Unit
):RecyclerView.Adapter<BackupAdapter.BackupViewHolder>() {
    inner class BackupViewHolder(val binding: ViewholderBackupBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewholderBackupBinding.inflate(inflater,parent,false)
        return BackupViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.binding.apply {
            tvTrainingCount.text = when {
                item.trainingCount % 100 in 11..14 -> "${item.trainingCount} тренировок"
                item.trainingCount % 10 == 1 -> "${item.trainingCount} тренировка"
                item.trainingCount % 10 in 2..4 -> "${item.trainingCount} тренировки"
                else -> "${item.trainingCount} тренировок"
            }
            tvTimeAgo.text = getRelativeTime(item.createdAt)
            tvSizeBackup.text = android.text.format.Formatter.formatShortFileSize(context,item.sizeInBytes)
            imbtnDownload.setOnClickListener{onRestoreClick(item)}
        }
        holder.binding.optionsMenu.setOnClickListener { v ->
            val popup = PopupMenu(v.context, v, Gravity.END, 0, R.style.MyPopupMenu)
            popup.menuInflater.inflate(R.menu.menu_backup_options, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_share_backup -> {
                        onShareClick(items[position]) // callback
                        true
                    }
                    R.id.menu_delete_backup -> {
                        onDeleteClick(items[position])
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun getRelativeTime(timestamp: Long): String{
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return DateUtils.getRelativeTimeSpanString(timestamp,now,DateUtils.MINUTE_IN_MILLIS).toString()
    }
}