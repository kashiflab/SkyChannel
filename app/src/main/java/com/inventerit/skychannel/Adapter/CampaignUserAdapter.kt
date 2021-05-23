package com.inventerit.skychannel.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.inventerit.skychannel.R
import com.inventerit.skychannel.model.User

class CampaignUserAdapter(val context: Context,
                          val userList: List<User>
                          ): RecyclerView.Adapter<CampaignUserAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        val parent: View = itemView
        init {
            parent.tag = this
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CampaignUserAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CampaignUserAdapter.ViewHolder, position: Int) {

        val user = userList[position]

        Glide.with(context).load(user.photoUrl).into(holder.imageView)
        holder.name.text = user.username

    }

    override fun getItemCount(): Int {
        return userList.size
    }
}