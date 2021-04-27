package com.inventerit.skychannel

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

class SliderAdapter(context: Context): PagerAdapter() {

    private var context: Context? = null

    init {
        this.context = context
    }

    private var layoutInflater: LayoutInflater? = null

    private var images: IntArray = intArrayOf(
        R.drawable.ic_menu_camera,
        R.drawable.ic_menu_gallery,
        R.drawable.ic_menu_slideshow
    )

    private var heading = arrayOf(
        "Title 1",
        "Title 2",
        "Title 3"
    )

    private var description = arrayOf(
        "Here is the sample description",
        "Here is the sample description",
        "Here is the sample description"
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

