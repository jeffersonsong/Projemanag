package com.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projemanag.R
import kotlinx.android.synthetic.main.item_label_color.view.*
import java.util.*

class LabelColorListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String,
    private val onClick: (position: Int, color: String)-> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_label_color,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        if (holder is MyViewHolder) {

            holder.itemView.view_main.setBackgroundColor(Color.parseColor(item))

            if (item == mSelectedColor) {
                holder.itemView.iv_selected_color.visibility = View.VISIBLE
            } else {
                holder.itemView.iv_selected_color.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                onClick(position, item)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}