package com.inventerit.skychannel

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.inventerit.skychannel.`interface`.OnComplete
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.model.User
import com.inventerit.skychannel.viewModel.MainViewModel
import com.tramsun.libs.prefcompat.Pref

class MainActivity : AppCompatActivity() {

    lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        mainViewModel =
            ViewModelProvider(this).get(MainViewModel::class.java)

        mainViewModel.getData(object : OnComplete<User> {
            override fun onCompleteTask(status: Boolean, result: User) {
                if(status){
                    Pref.putString(PrefKeys.username,result.username)
                    Pref.putString(PrefKeys.email,result.email)
                    Pref.putString(PrefKeys.coins,result.coins)
                    Pref.putString(PrefKeys.photoUrl,result.photoUrl)
                }
            }

        })

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
}