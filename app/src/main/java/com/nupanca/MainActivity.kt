package com.nupanca

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel

class MainActivity : AppCompatActivity() {
    var androidId: String? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Getting android id
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
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
