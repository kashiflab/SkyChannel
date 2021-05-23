package com.inventerit.skychannel.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.inventerit.skychannel.R
import com.inventerit.skychannel.constant.Constants
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.room.VideosDatabase
import com.inventerit.skychannel.room.model.Videos
import com.tramsun.libs.prefcompat.Pref
import java.util.*
import kotlin.collections.HashMap

class SplashActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var videosDatabase: VideosDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Pref.getBoolean(PrefKeys.isDarkMode,false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        setContentView(R.layout.activity_splash)
        setFullScreen()

        videosDatabase = VideosDatabase.getInstance(this)
        mAuth = FirebaseAuth.getInstance()

        goToNextActivity()
    }

    private fun goToNextActivity(){

        val user = mAuth.currentUser

        val isOnBoarding = Pref.getBoolean(PrefKeys.isOnBoarding,false)

        Handler(Looper.getMainLooper()).postDelayed({

            if(isOnBoarding) {
                if (user != null) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }else{
                startActivity(Intent(this, StartActivity::class.java))
            }

        }, 3000)
    }

    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }
}