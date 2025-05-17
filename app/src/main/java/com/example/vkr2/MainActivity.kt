package com.example.vkr2
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.vkr2.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val pref = getSharedPreferences("settings",0)
        val lightTheme = pref.getBoolean("lightTheme",false)
        AppCompatDelegate.setDefaultNightMode(
            if (lightTheme) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {}.launch(permission)
            }
        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        handleIntentExtras(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent!=null){
            handleIntentExtras(intent)
        }
    }
    private fun handleIntentExtras(intent: Intent){
        if (intent.hasExtra("navigateTo")){
            val destinationId=intent.getIntExtra("navigateTo",-1)
            if (destinationId !=-1&&findNavController(R.id.nav_host_fragment_activity_main).currentDestination?.id !=destinationId ){
                findNavController(R.id.nav_host_fragment_activity_main).navigate(destinationId)
            }
            intent.removeExtra("navigateTo")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_settings->{
                findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.settingsFragment)
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

}
