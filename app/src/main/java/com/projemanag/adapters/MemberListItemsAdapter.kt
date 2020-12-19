package com.projemanag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.projemanag.R
import com.projemanag.model.User
import com.projemanag.utils.Constants
import kotlinx.android.synthetic.main.item_member.view.*

open class MemberListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<User>,
    private val onClick: (position: Int, user: User, action: String) -> Unit
) : RecyclerView.Adapter<MemberListItemsAdapter.MyViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_member,
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
        val model = list[position]

        Glide
            .with(context)
            .load(model.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(view.iv_member_image)

        view.tv_member_name.text = model.name
        view.tv_member_email.text = model.email

        view.iv_selected_member.visibility =
            if (model.selected) View.VISIBLE else View.GONE

        view.setOnClickListener {
            onClick(
                position, model,
                if (model.selected) Constants.UN_SELECT else Constants.SELECT
            )
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount() = list.size

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
