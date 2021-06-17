package com.sidhow.skychannel.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.sidhow.skychannel.R

class SliderAdapter(context: Context): PagerAdapter() {

    private var context: Context? = null

    init {
        this.context = context
    }

    private var layoutInflater: LayoutInflater? = null

    private var images: IntArray = intArrayOf(
            R.drawable.screen_shot1,
            R.drawable.screen_shot2,
            R.drawable.screen_shot3
    )

    private var description = arrayOf(
        "Dashboard to check current coins",
        "Watch video and subscribe the channel to earn coins",
        "Create your own campaigns for subscribers, likes and views"
    )

    override fun getCount(): Int {
        return description.size
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context?.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view = layoutInflater?.inflate(R.layout.slider_layout,container,false)

        val imageView = view?.findViewById<ImageView>(R.id.imageView)
        val descriptionTv = view?.findViewById<TextView>(R.id.desc)

        imageView?.setImageResource(images[position])
        descriptionTv?.text = description[position]

        container.addView(view)
        return view!!
    }

    override fun destroyItem(container: ViewGroup, position: Int, o: Any) {
        container.removeView(o as View?)
    }
}

