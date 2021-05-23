package com.inventerit.skychannel.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.inventerit.skychannel.R
import com.inventerit.skychannel.model.Campaign

class SubscriptionAdapter(val context: Context,
                          val subscriptions: List<Campaign>
                          ): RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.image)
        val title = itemView.findViewById<TextView>(R.id.title)
        val subscription = itemView.findViewById<TextView>(R.id.subscription)
        val parent: View = itemView
        init {
            parent.tag = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.subscribed_items, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sub = subscriptions[position]

        var imageUrl = ""
        when (sub.type) {
            "0" -> {
                holder.title.text = sub.channelTitle
                holder.subscription.text = "Subscribed"
            }
            "1" -> {
                imageUrl = "https://img.youtube.com/vi/${sub.video_id}/0.jpg"
                holder.title.text = sub.video_title
                holder.subscription.text = "Liked"
            }
            else -> {
                imageUrl = "https://img.youtube.com/vi/${sub.video_id}/0.jpg"
                holder.title.text = sub.video_title
                holder.subscription.text = "Viewed"
            }
        }

        Glide.with(context).load(imageUrl).into(holder.image)

    }

    override fun getItemCount(): Int {
        return subscriptions.size
    }
}