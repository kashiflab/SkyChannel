package com.sidhow.skychannel.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.sidhow.skychannel.R
import com.sidhow.skychannel.constant.Constants
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.interfaces.OnComplete
import com.sidhow.skychannel.model.User
import com.sidhow.skychannel.room.VideosDatabase
import com.sidhow.skychannel.viewModel.SaveCampaignViewModel
import com.tramsun.libs.prefcompat.Pref
import java.util.*
import kotlin.collections.HashMap

class SplashActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var videosDatabase: VideosDatabase

    private lateinit var saveCampaignViewModel: SaveCampaignViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Pref.getBoolean(PrefKeys.isDarkMode,false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        setContentView(R.layout.activity_splash)
        setFullScreen()
        videosDatabase = VideosDatabase.getInstance(this)
        Thread{
            videosDatabase.campaignDAO().deleteAllCampaigns()
            Log.i("Deleted","Deleted")
        }.start()

        mAuth = FirebaseAuth.getInstance()

        getLatestVersion()

        val user = mAuth.currentUser
        val isOnBoarding = Pref.getBoolean(PrefKeys.isOnBoarding,false)

        if(mAuth.currentUser!=null) {
            saveCampaignViewModel = ViewModelProvider(this).get(SaveCampaignViewModel::class.java)

//            Thread {
//                saveCampaignViewModel.saveLikesCampaigns()
//                saveCampaignViewModel.saveViewsCampaigns()
//            }.start()

            saveCampaignViewModel.saveSubsCampaigns(object: OnComplete<User>{
                override fun onCompleteTask(status: Boolean, result: User) {
                    nextActivity(isOnBoarding,user)
                }
            })
        }else {

            Handler(Looper.getMainLooper()).postDelayed({

                nextActivity(isOnBoarding,user)

            }, 3000)
        }
    }

    private fun getLatestVersion() {

        val databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference.child("Android").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("versionName",snapshot.child("versionName").value.toString())
                Pref.putString(Constants.versionName,snapshot.child("versionName").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun nextActivity(isOnBoarding: Boolean, user: FirebaseUser?) {
        if(isOnBoarding) {
            if (user != null) {
                startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
        }else{
            startActivity(Intent(this@SplashActivity, StartActivity::class.java))
        }
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