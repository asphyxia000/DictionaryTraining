package com.example.vkr2.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import com.example.vkr2.MainActivity
import com.example.vkr2.R

class WorkoutReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val pref = context.getSharedPreferences("settings",Context.MODE_PRIVATE)
        val notificationsEnabled = pref.getBoolean("notificationsOn",false)

        if (!notificationsEnabled){
            return
        }

        val channelId = "workout_channel"
        // Создание канала (только для Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Уведомления о тренировке",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Проверка разрешения на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Разрешение не выдано — не отправляем уведомление
                Toast.makeText(context, "Нет разрешения на уведомления", Toast.LENGTH_SHORT).show()
                return
            }
        }
        val resultIntent = Intent(context,MainActivity::class.java).apply {
            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigateTo", R.id.navigation_home)
        }

        val resultPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        )

        // Создание и отправка уведомления
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.biceps)
            .setContentTitle("Тренировка")
            .setContentText("Не забудь о тренировке!")
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)
    }
}
