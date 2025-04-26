package com.example.vkr2.ui.Notification_muscle_groups

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkr2.repository.MuscleGroupRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

class NotificationListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsViewModel(
                MuscleGroupRepositoryImpl(context, GlobalScope, Dispatchers.IO)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
