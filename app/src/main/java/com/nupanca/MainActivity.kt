package com.nupanca

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        val db = FirebaseDatabase.getInstance()
        db.setPersistenceEnabled(true)
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
