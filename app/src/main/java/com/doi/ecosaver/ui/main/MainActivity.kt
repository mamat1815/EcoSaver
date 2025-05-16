package com.doi.ecosaver.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.doi.ecosaver.ui.notifications.NotificationsFragment
import com.doi.ecosaver.R
import com.doi.ecosaver.ui.scan.ScanFragment
import com.doi.ecosaver.ui.settings.SettingsFragment
import com.doi.ecosaver.databinding.ActivityMainBinding
import com.doi.ecosaver.ui.analysis.AnalysisFragment
import com.doi.ecosaver.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        replaceFragment(HomeFragment())

        binding.apply {
            bottomNavigationView.setOnItemSelectedListener {
                when(it.itemId) {
                    R.id.menu_home -> replaceFragment(HomeFragment())
                    R.id.menu_analysis -> replaceFragment(AnalysisFragment())
                    R.id.menu_scan -> replaceFragment(ScanFragment())
//                    R.id.menu_notifications -> replaceFragment(NotificationsFragment())
                    R.id.menu_settings -> replaceFragment(SettingsFragment())

                    else -> {

                    }
                }
                true
            }


        }
    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction =fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_main,fragment)
        fragmentTransaction.commit()
    }
}