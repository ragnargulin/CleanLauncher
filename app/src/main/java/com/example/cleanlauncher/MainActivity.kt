package com.example.cleanlauncher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    private lateinit var launcherPreferences: LauncherPreferences

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

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Use WindowInsets to adjust layout for the status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout)) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(0, statusBarHeight, 0, 0) // Apply padding to the root layout
            insets
        }

        // Apply status bar visibility based on preferences
        setStatusBarVisibility(launcherPreferences.isStatusBarVisible())

        viewPager.setCurrentItem(HOME_SCREEN_INDEX, false)
        viewPager.isUserInputEnabled = true

    }

    override fun onResume(){
        super.onResume()
        viewPager.setCurrentItem(HOME_SCREEN_INDEX, false)
        viewPager.isUserInputEnabled = true
        val appDrawerFragment = supportFragmentManager.findFragmentByTag("f1") as? AppDrawerFragment
        appDrawerFragment?.scrollToTop()
    }

    private fun setStatusBarVisibility(isVisible: Boolean) {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            if (isVisible) {
                show(WindowInsetsCompat.Type.statusBars())
            } else {
                hide(WindowInsetsCompat.Type.statusBars())
            }
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