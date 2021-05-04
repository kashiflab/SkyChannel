package com.inventerit.skychannel.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.inventerit.skychannel.R

class SliderAdapter(context: Context): PagerAdapter() {

    private var context: Context? = null

    init {
        this.context = context
    }

    private var layoutInflater: LayoutInflater? = null

    private var images: IntArray = intArrayOf(
            R.drawable.ic_subscribe,
            R.drawable.ic_campaign,
            R.drawable.ic_campaign
    )

    private var heading = arrayOf(
        "Subscribe",
        "Run Campaign",
        "Campaign Progress"
    )

    private var description = arrayOf(
        "Subscribe, view and like other channel to earn coins",
        "Use coins to run campaigns for your channel",
        "Check the progress of your campaigns"
    )

    override fun getCount(): Int {
        return heading.size
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return view == o
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context?.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val view = layoutInflater?.inflate(R.layout.slider_layout,container,false)

        val imageView = view?.findViewById<ImageView>(R.id.imageView)
        val headingTv = view?.findViewById<TextView>(R.id.heading)
        val descriptionTv = view?.findViewById<TextView>(R.id.desc)

        imageView?.setBackgroundResource(images[position])
        headingTv?.text = heading[position]
        descriptionTv?.text = description[position]

        container.addView(view)
        return view!!
    }

    override fun destroyItem(container: ViewGroup, position: Int, o: Any) {
        container.removeView(o as View?)
    }
}

