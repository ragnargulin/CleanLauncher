package com.example.cleanlauncher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    private lateinit var launcherPreferences: LauncherPreferences
    private var isStatusBarCurrentlyVisible: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launcherPreferences = LauncherPreferences(this)

        AppCompatDelegate.setDefaultNightMode(
            if (launcherPreferences.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ScreenSlidePagerAdapter(this)
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL

        // Initial status bar visibility setup
        updateStatusBarVisibility()

        viewPager.setCurrentItem(HOME_SCREEN_INDEX, false)
        viewPager.isUserInputEnabled = true
    }

    override fun onResume() {
        super.onResume()
        updateStatusBarVisibility()
        viewPager.setCurrentItem(HOME_SCREEN_INDEX, false)
        viewPager.isUserInputEnabled = true
        val appDrawerFragment = supportFragmentManager.findFragmentByTag("f1") as? AppDrawerFragment
        appDrawerFragment?.scrollToTop()
    }

    private fun updateStatusBarVisibility() {
        val shouldStatusBarBeVisible = launcherPreferences.isStatusBarVisible()

        // Check if the current visibility state matches the preference
        if (isStatusBarCurrentlyVisible == null || isStatusBarCurrentlyVisible != shouldStatusBarBeVisible) {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                if (shouldStatusBarBeVisible) {
                    show(WindowInsetsCompat.Type.statusBars())
                } else {
                    hide(WindowInsetsCompat.Type.statusBars())
                }
            }
            isStatusBarCurrentlyVisible = shouldStatusBarBeVisible
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: AppCompatActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int) = when (position) {
            0 -> HomeFragment()
            1 -> AppDrawerFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    companion object {
        private const val HOME_SCREEN_INDEX = 0
    }
}