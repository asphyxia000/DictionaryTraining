package com.example.vkr2.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.app.backup.BackupDataOutput
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.text.format.DateUtils
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr2.DataBase.FitnessDatabase
import com.example.vkr2.R
import com.example.vkr2.databinding.FragmentSettingsBinding
import com.example.vkr2.ui.notifications.WorkoutReminderReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.E

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val requiredPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                val hour = prefs.getInt("notifHour", 16)
                val minute = prefs.getInt("notifMinute", 0)
                scheduleWorkoutNotification(hour, minute)
                Toast.makeText(requireContext(), "Уведомления включены", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Уведомления отключены", Toast.LENGTH_SHORT).show()
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        binding.toolbarSettings.findViewById<MaterialButton>(R.id.close_zamer).setOnClickListener {
            findNavController().popBackStack() // Возвращает назад по стеку
        }
        // === 1. Восстановление времени из настроек и отображение на кнопке ===
        val savedHour = prefs.getInt("notifHour", 16)
        val savedMinute = prefs.getInt("notifMinute", 0)
        updateButtonTime(savedHour, savedMinute)
        // Восстановление состояний переключателей из SharedPreferences
        binding.switchTheme.isChecked = prefs.getBoolean("lightTheme", false)
        binding.switchKeepScreenOn.isChecked = prefs.getBoolean("keepScreenOn", false)
        binding.switchNotifications.isChecked = prefs.getBoolean("notificationsOn", false)

        // Применить флаг удержания экрана
        if (binding.switchKeepScreenOn.isChecked) {
            activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // === 2. Запрос разрешения на уведомления ===
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissionLauncher.launch(permission)
            }
        }


        // === 3. Смена темы ===
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            prefs.edit().putBoolean("lightTheme", isChecked).apply()
        }

        // === 4. Держать экран включённым ===
        binding.switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("keepScreenOn", isChecked).apply()
            activity?.window?.apply {
                if (isChecked) addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        // === 5. Установка времени через диалог ===
        binding.btnSetTime.setOnClickListener {
            val currentHour = prefs.getInt("notifHour", 16)
            val currentMinute = prefs.getInt("notifMinute", 0)
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    prefs.edit()
                        .putInt("notifHour", hour)
                        .putInt("notifMinute", minute)
                        .apply()
                    updateButtonTime(hour, minute)
                    scheduleWorkoutNotification(hour, minute)
                    Toast.makeText(
                        requireContext(),
                        "Уведомление установлено на %02d:%02d".format(hour, minute),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                currentHour,
                currentMinute,
                true
            )
            timePicker.show()
        }

        // === 6. Переключатель уведомлений с запросом разрешения ===
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notificationsOn", isChecked).apply()

            if (isChecked) {
                // Android 13+ — запрос разрешения
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permission = Manifest.permission.POST_NOTIFICATIONS
                    if (ContextCompat.checkSelfPermission(requireContext(), permission)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        requiredPermissionLauncher.launch(permission)
                        return@setOnCheckedChangeListener
                    }
                }

                val hour = prefs.getInt("notifHour", 16)
                val minute = prefs.getInt("notifMinute", 0)
                scheduleWorkoutNotification(hour, minute)

            } else {
                cancelWorkoutNotification()

                // На Android 13+ разрешение остаётся — просто не используем
                // Программно снять его нельзя, но можно предложить пользователю:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(
                        requireContext(),
                        "Уведомления отключены",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        // === 7. Автовключение при первом запуске ===
        val isFirst = prefs.getBoolean("firstNotificationInit", true)
        if (isFirst && binding.switchNotifications.isChecked) {
            prefs.edit().putBoolean("firstNotificationInit", false).apply()
            val hour = prefs.getInt("notifHour", 16)
            val minute = prefs.getInt("notifMinute", 0)
            scheduleWorkoutNotification(hour, minute)
        }

        binding.btnBackup.setOnClickListener {
            showBackupDialog()
        }
        val channel = NotificationChannel(
            "backup_channel",
            "Резервные копии",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о сохранении резервных копий"
        }
        val manager = requireContext().getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

    }
    private fun scheduleWorkoutNotification(hour: Int, minute: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), WorkoutReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelWorkoutNotification() {
        val intent = Intent(requireContext(), WorkoutReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun updateButtonTime(hour: Int, minute: Int) {
        binding.btnSetTime.text = String.format("%02d:%02d", hour, minute)
    }

    private fun showDeleteDialog(recycler: RecyclerView, backup: BackupInfo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить резервную копию?")
            .setMessage("Файл ${backup.filename} будет удалён без возможности восстановления. Продолжить?")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                deleteBackup(backup) {
                    GlobalScope.launch(Dispatchers.Main) {
                        val refreshed = loadBackups()
                        recycler.adapter = BackupAdapter(
                            refreshed,
                            onRestoreClick = ::showRestoreConfirmDialog,
                            onDeleteClick = { b -> showDeleteDialog(recycler, b) },
                            onShareClick = { b -> sharedBackupFile(b) }
                        )
                    }
                }
            }
            .show()
    }


    private fun showBackupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_backup, null)
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
            .setView(dialogView)
            .create()


        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val backupEnabled = prefs.getBoolean("backupEnabled", false)

        val layoutBefore = dialogView.findViewById<LinearLayout>(R.id.layoutBeforeAccount)
        val layoutAfter = dialogView.findViewById<LinearLayout>(R.id.layoutAfterAccountAdded)
        val btnEnable = dialogView.findViewById<Button>(R.id.btnEnableBackup)
        val recycler = dialogView.findViewById<RecyclerView>(R.id.recyclerBackup)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        layoutBefore.visibility = if (backupEnabled) View.GONE else View.VISIBLE
        layoutAfter.visibility = if (backupEnabled) View.VISIBLE else View.GONE

        btnEnable.setOnClickListener {
            prefs.edit().putBoolean("backupEnabled", true).apply()
            layoutBefore.visibility = View.GONE
            layoutAfter.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Автоматическое резервное копирование включено", Toast.LENGTH_SHORT).show()
        }

        val btnAddBackup = dialogView.findViewById<ImageButton>(R.id.imageButtonAdd)
        btnAddBackup.setOnClickListener {
            createBackup(requireContext())
            GlobalScope.launch(Dispatchers.Main) {
                delay(500)
                updateBackupList(recycler)
            }
        }


        GlobalScope.launch(Dispatchers.Main) {
            val backups = loadBackups()
            val adapter = BackupAdapter(
                backups,
                onRestoreClick = { backup ->
                    showRestoreConfirmDialog(backup)
                },
                onDeleteClick = { backup ->
                    val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
                        .setTitle("Удалить резервную копию?")
                        .setMessage("Файл ${backup.filename} будет удалён без возможности восстановления. Продолжить?")
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Удалить") { _, _ ->
                            deleteBackup(backup) {
                                GlobalScope.launch(Dispatchers.Main) {
                                    val refreshed = loadBackups()
                                    recycler.adapter = BackupAdapter(
                                        refreshed,
                                        onRestoreClick = ::showRestoreConfirmDialog,
                                        onDeleteClick = { b -> showDeleteDialog(recycler, b) },
                                        onShareClick = { b -> sharedBackupFile(b) }
                                    )
                                }
                            }
                        }
                        .create()

                    dialog.setOnShowListener {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
                    }

                    dialog.show()
                },
                onShareClick = { backup ->
                    sharedBackupFile(backup)
                }
            )
            recycler.adapter = adapter
        }

        dialog.show()

    }


    private fun sharedBackupFile(backup: BackupInfo) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .resolve("MyFitnessBackups"),backup.filename
        )
        if (!file.exists()){
            Toast.makeText(requireContext(),"Файл не найден", Toast.LENGTH_SHORT).show()
            return
        }
        val uri =FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type =  "application/json"
            putExtra(Intent.EXTRA_STREAM,uri)
            flags= Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться резервной копией"))
    }

    private fun updateBackupList(recycler: RecyclerView) {
        GlobalScope.launch(Dispatchers.Main) {
            val update = loadBackups()
            recycler.adapter = BackupAdapter(
                update,
                onRestoreClick = ::showRestoreConfirmDialog,
                onDeleteClick = { b -> deleteBackup(b) { updateBackupList(recycler) } },
                onShareClick = { b -> sharedBackupFile(b) }
            )
        }
    }


    private fun deleteBackup(backup: BackupInfo,onDone: ()->Unit) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .resolve("MyFitnessBackups"),backup.filename
        )
        if (file.exists()) file.delete()
        Toast.makeText(requireContext(),"Удалено:  ${backup.filename}",Toast.LENGTH_SHORT).show()
        onDone()
    }


    private fun showRestoreConfirmDialog(backup: BackupInfo){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Восстановить резервную копию")
            .setMessage("""
            ${backup.trainingCount} тренировок
            ${DateUtils.getRelativeTimeSpanString(backup.createdAt)}
            Все текущие данные будут уничтожены. Продолжить?
            """.trimIndent())
            .setNegativeButton("Отмена",null)
            .setPositiveButton("Восстановить"){_,_->
                restoreBackup(requireContext(), backup)

            }
            .show()
    }

    private fun gsonWithJavaTime(): Gson{
        return GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate::class.java,LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime::class.java,LocalDateTimeAdapter())
            .create()
    }

    private fun createBackup(context: Context){
        GlobalScope.launch(Dispatchers.IO) {
            try{
                val db = FitnessDatabase.getInstance(context,this)?: return@launch
                val trainingDAO = db.TrainingDAO()
                val measurementsDAO = db.BodyMeasurementsDAO()
                val trainings = trainingDAO.getAllTrainingsList()
                val sets = trainingDAO.getAllSets()
                val measurements= measurementsDAO.getAllDirect()
                val crossRefs = trainingDAO.getAllCrossRefs()

                val backup = BackupData(
                    trainings = trainings,
                    sets = sets,
                    measurements = measurements,
                    crossRef = crossRefs
                )

                val gson = gsonWithJavaTime()
                val json = gson.toJson(backup)

                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val backupDir = File(downloadDir,"MyFitnessBackups")

                if (!backupDir.exists())backupDir.mkdirs()

                val filename = "backup_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())}.json"
                val file = File(backupDir,filename)

                file.writeText(json)

                withContext(Dispatchers.Main){
                    Toast.makeText(context,"Бэкап сохранён в ${file.name}",Toast.LENGTH_SHORT).show()
                    showbackupNotifi(file)
                }

            } catch (e: Exception){
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Ошибка при создании бэкапа: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun loadBackups(): List<BackupInfo> = withContext(Dispatchers.IO){
        val backup = mutableListOf<BackupInfo>()
        val gson = gsonWithJavaTime()

        val backupDir=File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "MyFitnessBackups"
        )
        if (!backupDir.exists()) return@withContext backup
        val  files = backupDir.listFiles{file -> file.extension =="json"}?: return@withContext backup
        for (file in files){
            try {
                val context = file.readText()
                val parsed = gson.fromJson(context,BackupData::class.java)
                val trainingCount = parsed.trainings.size
                backup.add(
                    BackupInfo(
                        filename = file.name,
                        sizeInBytes = file.length(),
                        createdAt = file.lastModified(),
                        trainingCount = trainingCount
                    )
                )
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        backup.sortedByDescending { it.createdAt }
    }

    private fun restoreBackup(context: Context,backup: BackupInfo){
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val backupDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "MyFitnessBackups"
                )
                val file = File(backupDir,backup.filename)
                if(!file.exists())return@launch

                val gson = gsonWithJavaTime()
                val fileContent = file.readText()
                val parsed = gson.fromJson(fileContent, BackupData::class.java)

                val db = FitnessDatabase.getInstance(context,this)?:return@launch
                val trainingDAO = db.TrainingDAO()
                val measurementsDAO = db.BodyMeasurementsDAO()

                trainingDAO.deleteAllSets()
                trainingDAO.deleteAllTrainings()
                trainingDAO.deleteAllCrossRefs()
                measurementsDAO.deleteAll()

                parsed.trainings.forEach{trainingDAO.insertTraining(it)}
                parsed.sets.forEach{trainingDAO.insertSet(it)}
                parsed.measurements.forEach{measurementsDAO.insert(it)}
                trainingDAO.insertTrainingExercises(parsed.crossRef)

                withContext(Dispatchers.Main){
                    Toast.makeText(context,"Данные восстановлены из ${backup.filename}",Toast.LENGTH_LONG).show()
                }
            }catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка восстановления: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showbackupNotifi(file: File) {
        val backupDirUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file.parentFile!!
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(backupDirUri, "resource/folder")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            requireContext(), 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(requireContext(), "backup_channel")
            .setSmallIcon(R.drawable.biceps)  // замени на свою иконку
            .setContentTitle("Бэкап создан")
            .setContentText("Файл сохранён в Downloads/MyFitnessBackups")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(requireContext()).notify(1, notification)
        }
    }

    private fun showBottomNav() {
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navView?.let {
            it.visibility = View.VISIBLE
            it.animate()
                .translationY(0f)
                .setDuration(200)
                .start()
        }
    }

    private fun hideBottomNav() {
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navView?.let {
            it.animate()
                .translationY(it.height.toFloat())
                .setDuration(200)
                .withEndAction {
                    it.visibility = View.GONE
                }
                .start()
        }
    }

    override fun onResume() {
        super.onResume()
        hideBottomNav()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        showBottomNav()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        _binding = null
    }
}
