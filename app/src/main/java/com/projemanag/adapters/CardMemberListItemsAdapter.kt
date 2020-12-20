package com.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.projemanag.R
import com.projemanag.model.SelectedMembers
import kotlinx.android.synthetic.main.item_card_selected_member.view.*

open class CardMemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>,
    private val assignMembers: Boolean,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<CardMemberListItemsAdapter.MyViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card_selected_member,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) =
        bind(holder.itemView, position)

    private fun bind(view: View, position: Int) {
        if (position == list.size && assignMembers) {
            view.iv_add_member.visibility = View.VISIBLE
            view.iv_selected_member_image.visibility = View.GONE

        } else {
            val model = list[position]
            view.iv_add_member.visibility = View.GONE
            view.iv_selected_member_image.visibility = View.VISIBLE

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(view.iv_selected_member_image)
        }

        view.setOnClickListener {
            onClick()
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount() =
        if (assignMembers) list.size + 1
        else list.size

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}