package com.sidhow.skychannel.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.sidhow.skychannel.R

class SkuAdapter(
    private var context: Context,
    var skuDetails: List<SkuDetails>,
    private var isVIP: Boolean,
    var listener: OnItemClickListener
): RecyclerView.Adapter<SkuAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)
        val price = itemView.findViewById<TextView>(R.id.price)
        val freeTrial = itemView.findViewById<TextView>(R.id.freeTrial)
        val gracePeriod = itemView.findViewById<TextView>(R.id.gracePeriod)
        var parent = itemView
        init {
            parent.tag = this
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.sku_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sku = skuDetails[position]

        val mainTitle = sku.title.split("(")[0]
        holder.title.text = mainTitle
        holder.price.text = "${sku.price}"

        if(isVIP){
            holder.freeTrial.visibility = View.VISIBLE
            if(sku.freeTrialPeriod.isNotEmpty()) {
                when(sku.freeTrialPeriod){
                    "P1W"->{
                        holder.freeTrial.text = "1 Week free trial"
                    }
                    "P3D"->{
                        holder.freeTrial.text = "3 Days free trial"
                    }
                }

            }else{
                holder.freeTrial.text = "No free trial"
            }
            holder.gracePeriod.visibility = View.VISIBLE
            if(mainTitle.contains("Weekly") ||
                mainTitle.contains("Monthly")
            ) {
                holder.gracePeriod.text = "3 days grace period"
            }else{
                holder.gracePeriod.text = "7 days grace period"
            }
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(sku)
        }
    }
    interface OnItemClickListener {
        fun onItemClick(item: SkuDetails)
    }

    override fun getItemCount(): Int {
        return skuDetails.size
    }
}