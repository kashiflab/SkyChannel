package com.inventerit.skychannel

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
import com.inventerit.skychannel.constant.PrefKeys
import com.tramsun.libs.prefcompat.Pref

class SplashActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Pref.getBoolean(PrefKeys.isDarkMode,false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        setContentView(R.layout.activity_splash)
        setFullScreen()

        mAuth = FirebaseAuth.getInstance()
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