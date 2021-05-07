package com.inventerit.skychannel.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.inventerit.skychannel.databinding.ActivityBuyPointsBinding

class BuyPointsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuyPointsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyPointsBinding.inflate(layoutInflater)

        binding.root
    }
}