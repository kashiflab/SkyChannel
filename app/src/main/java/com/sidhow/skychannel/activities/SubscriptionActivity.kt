package com.sidhow.skychannel.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import com.sidhow.skychannel.Adapter.ViewPagerAdapter
import com.sidhow.skychannel.databinding.ActivitySubscriptionBinding
import com.sidhow.skychannel.ui.subscriptions.LikesFragment
import com.sidhow.skychannel.ui.subscriptions.SubsFragment
import com.sidhow.skychannel.ui.subscriptions.ViewsFragment

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val adapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )

        adapter.addFragment(SubsFragment(), "Subscriptions")
        adapter.addFragment(LikesFragment(), "Liked")
        adapter.addFragment(ViewsFragment(), "Viewed")
        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}