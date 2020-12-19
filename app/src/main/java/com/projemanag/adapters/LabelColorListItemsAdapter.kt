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
    private val onClick: (position: Int, color: String) -> Unit
) : RecyclerView.Adapter<LabelColorListItemsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_label_color,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = bind(holder.itemView, position)

    private fun bind(view:View, position:Int) {
        val item = list[position]

        view.view_main.setBackgroundColor(Color.parseColor(item))

        view.iv_selected_color.visibility =
            if (item == mSelectedColor) View.VISIBLE else View.GONE

        view.setOnClickListener {
            onClick(position, item)
        }
    }

    override fun getItemCount() = list.size

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}