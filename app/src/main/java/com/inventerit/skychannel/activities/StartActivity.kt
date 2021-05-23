package com.inventerit.skychannel.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.inventerit.skychannel.Adapter.SliderAdapter
import com.inventerit.skychannel.R
import com.inventerit.skychannel.constant.Constants
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.databinding.ActivityStartBinding
import com.inventerit.skychannel.room.model.Videos
import com.inventerit.skychannel.room.VideosDatabase
import com.tramsun.libs.prefcompat.Pref
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {


    private lateinit var mDots: Array<TextView?>
    private lateinit var binding: ActivityStartBinding
    private var sliderAdapter: SliderAdapter? = null

    private var mCurrentPage: Int = 0

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


    @SuppressWarnings("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setFullScreen()

        sliderAdapter = SliderAdapter(this)
        binding.viewPager.adapter = sliderAdapter

        binding.backBtn.text = ""
        addDots(0)

        binding.backBtn.setOnClickListener {
            binding.viewPager.currentItem = mCurrentPage - 1
        }
        binding.nextBtn.setOnClickListener {
            when (mCurrentPage){
                0 -> {
                    binding.viewPager.currentItem = mCurrentPage + 1
                }
                1 -> {
                    binding.viewPager.currentItem = mCurrentPage + 1
                }
                2 -> {

                    Pref.putBoolean(PrefKeys.isOnBoarding,true)
                    Pref.putBoolean(PrefKeys.isFirstTime,true)
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                addDots(position)
                mCurrentPage = position

                when (position) {
                    0 -> {
                        binding.backBtn.text = ""
                        nextBtn.text = "Next"
                    }
                    1 -> {
                        binding.backBtn.text = "Back"
                        nextBtn.text = "Next"
                    }
                    2 -> {
                        binding.backBtn.text = "Back"
                        nextBtn.text = "Finish"
                    }
                }
            }

        })
    }

    fun addDots(i:Int){
        mDots = arrayOfNulls<TextView>(3)
        binding.dotsLayout.removeAllViews()

        for (i in mDots.indices) {
            mDots[i] = TextView(this)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mDots[i]?.text = Html.fromHtml("&#8226",0)
            }else{
                mDots[i]?.text = Html.fromHtml("&#8226")
            }
            mDots[i]?.textSize = 35f
            mDots[i]?.setTextColor(ContextCompat.getColor(this, R.color.teal_200))

            binding.dotsLayout.addView(mDots[i])
        }

        if(mDots.isNotEmpty()){
            mDots[i]?.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

}