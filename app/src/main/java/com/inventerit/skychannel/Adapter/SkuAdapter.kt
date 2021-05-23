package com.inventerit.skychannel.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.inventerit.skychannel.R

class SkuAdapter(
    private var context: Context,
    var skuDetails: List<SkuDetails>,
    var listener: OnItemClickListener
): RecyclerView.Adapter<SkuAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.title)
        val price = itemView.findViewById<TextView>(R.id.price)
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

        holder.title.text = sku.title
        holder.price.text = sku.price

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