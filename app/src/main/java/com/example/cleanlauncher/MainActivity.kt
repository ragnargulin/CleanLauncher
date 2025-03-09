package com.example.cleanlauncher

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var launcherPreferences: LauncherPreferences
    private lateinit var viewPager: ViewPager2

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == LauncherPreferences.KEY_FONT_SIZE) {
                // Notify fragments to update their UI
                (viewPager.adapter as? ScreenSlidePagerAdapter)?.notifyFontSizeChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launcherPreferences = LauncherPreferences(this)
        viewPager = findViewById(R.id.viewPager)
        val adapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = adapter

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        viewPager.setCurrentItem(HOME_SCREEN_INDEX, false)

        launcherPreferences.prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        launcherPreferences.prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onResume() {
        super.onResume()
        viewPager.setCurrentItem(HOME_SCREEN_INDEX, false)
    }

    private inner class ScreenSlidePagerAdapter(fa: AppCompatActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int) = when (position) {
            0 -> HomeFragment()
            1 -> AppDrawerFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }

        fun notifyFontSizeChanged() {
            // Notify both fragments to update their UI
            (supportFragmentManager.findFragmentByTag("f0") as? HomeFragment)?.updateFontSize()
            (supportFragmentManager.findFragmentByTag("f1") as? AppDrawerFragment)?.updateFontSize()
        }
    }

    companion object {
        private const val HOME_SCREEN_INDEX = 0 // Change this to your actual home screen index
    }
}