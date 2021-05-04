package com.inventerit.skychannel.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.inventerit.skychannel.R
import com.inventerit.skychannel.model.Campaign

class CampaignAdapter(val context: Context,
                      var mList: List<Campaign>
): RecyclerView.Adapter<CampaignAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cp_Image = itemView.findViewById<ImageView>(R.id.cp_image)
        var cp_title = itemView.findViewById<TextView>(R.id.videoTitle)
        var cp_time = itemView.findViewById<TextView>(R.id.time)
        var cp_progress = itemView.findViewById<ProgressBar>(R.id.progress)
        var cp_views = itemView.findViewById<TextView>(R.id.views)
        val parent: View = itemView
        init {
            parent.tag = this
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.campaign_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val campaign = mList[position]
        val imageUrl = "https://img.youtube.com/vi/"+campaign.video_id+"/0.jpg"
        var view = ""

        if(campaign.type == "0"){
            view = "Subscriber(s)"
        }else if (campaign.type=="1"){
            view = "Like(s)"
        }else if (campaign.type=="2"){
            view = "View(s)"
        }

        Glide.with(context).load(imageUrl).into(holder.cp_Image)
        holder.cp_title.text = campaign.video_title
        holder.cp_time.text = campaign.total_time
        holder.cp_views.text = "${campaign.current_number}/${campaign.total_number} $view"
        holder.cp_progress.max = campaign.total_number.toInt()
        holder.cp_progress.progress = campaign.current_number.toInt()

    }

    private fun getProgress(totalNumber: String, currentNumber: String): Int {
        var currentProgress: Int = totalNumber.toInt() * 100 / currentNumber.toInt()

        return currentProgress
    }

    override fun getItemCount(): Int {
        if (mList.isNullOrEmpty()) {
            return 0
        }
        return mList.size
    }
}