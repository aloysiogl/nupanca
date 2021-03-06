package com.nupanca

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.lang.RuntimeException

class MainActivity : AppCompatActivity() {
    var androidId: String? = null
    var planningScroll: Int =
        0 // FIXME: I couldn't figure out how to do this without using the activity

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Getting android id
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        val db = FirebaseDatabase.getInstance()
        try {
            db.setPersistenceEnabled(true)
        } catch (e: RuntimeException) {
        }
    }

    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        )?.childFragmentManager?.fragments

        if (fragmentList != null) {
            for (f in fragmentList)
                if (f is BaseFragment && f.onBackPressed())
                    return
        }

        super.onBackPressed()
    }
}
