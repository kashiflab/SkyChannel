package com.inventerit.skychannel.activities

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.inventerit.skychannel.R
import com.inventerit.skychannel.constant.Constants
import com.inventerit.skychannel.interfaces.OnComplete
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.interfaces.OnCampaignCompleted
import com.inventerit.skychannel.interfaces.OnFetchSkuDetails
import com.inventerit.skychannel.model.SkuProducts
import com.inventerit.skychannel.model.User
import com.inventerit.skychannel.room.VideosDatabase
import com.inventerit.skychannel.room.model.Videos
import com.inventerit.skychannel.viewModel.CreditsViewModel
import com.inventerit.skychannel.viewModel.MainViewModel
import com.tbruyelle.rxpermissions3.RxPermissions
import com.tramsun.libs.prefcompat.Pref

class MainActivity : AppCompatActivity() {

    private lateinit var creditsViewModel: CreditsViewModel
    private lateinit var videoDatabase: VideosDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        creditsViewModel =
            ViewModelProvider(this).get(CreditsViewModel::class.java)

        videoDatabase = VideosDatabase.getInstance(this)
        val user = FirebaseAuth.getInstance().currentUser

        if(user!=null && Pref.getBoolean(PrefKeys.isFirstTime,false)){
            Log.i("RoomDB","First")
            getAllHistory(user)
        }
        creditsViewModel.getData(object : OnComplete<User> {
            override fun onCompleteTask(status: Boolean, result: User) {
                if(status){
                    Pref.putString(PrefKeys.username,result.username)
                    Pref.putString(PrefKeys.email,result.email)
                    Pref.putString(PrefKeys.coins,result.coins)
                    Pref.putString(PrefKeys.photoUrl,result.photoUrl)
                }
            }

        })

        Thread{
            creditsViewModel.endCompletedCampaigns(object :OnCampaignCompleted{
                override fun onCampaignCompleted(status: Boolean, message: String) {
                    if(status){
                        Log.i("MainActivity",message)
                    }else{
                        Log.i("MainActivity",message)
                    }
                }
            })
        }.start()

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                    R.id.nav_subscribe, R.id.nav_like, R.id.nav_view, R.id.nav_campaign, R.id.nav_more
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun getAllHistory(user: FirebaseUser){

        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        database.child(Constants.users).child(user.uid).child(Constants.history).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snapshot1 in snapshot.children){

                    val videos = Videos()

                    videos.videoId = snapshot1.child(Constants.video_id).value.toString()
                    videos.created_at = snapshot1.child(Constants.created_at).value.toString()
                    videos.type = snapshot1.child(Constants.type).value.toString()
                    videos.userId = snapshot1.child(Constants.user_id).value.toString()
                    videos.channelId = snapshot1.child(Constants.channel_id).value.toString()

                    Log.i("RoomDB","Fetching Data")
                    Thread{
                        Log.i("RoomDB","Inserting Data")
                        videoDatabase.videosDao().insert(videos)
                    }.start()
                }
                Pref.putBoolean(PrefKeys.isFirstTime,false)
            }

            override fun onCancelled(error: DatabaseError) {

                Log.i("RoomDB","${error.message}")
            }
        })
    }
}