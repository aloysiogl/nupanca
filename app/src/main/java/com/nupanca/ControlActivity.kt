package com.nupanca

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ControlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment)?.childFragmentManager?.fragments

        if (fragmentList != null) {
            for (f in fragmentList)
                if (f is BaseFragment && f.onBackPressed())
                    return
        }

        super.onBackPressed()
    }
}