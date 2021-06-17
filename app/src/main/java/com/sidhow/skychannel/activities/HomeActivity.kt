package com.sidhow.skychannel.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.databinding.ActivityHomeBinding
import com.tramsun.libs.prefcompat.Pref

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var coins = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coins = Pref.getString(PrefKeys.coins)


    }
}