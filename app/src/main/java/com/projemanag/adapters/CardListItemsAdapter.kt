package com.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projemanag.R
import com.projemanag.activities.TaskListActivity
import com.projemanag.model.Card
import com.projemanag.model.SelectedMembers
import kotlinx.android.synthetic.main.item_card.view.*

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>,
    private val onClick: (cardPosition: Int) -> Unit
) : RecyclerView.Adapter<CardListItemsAdapter.MyViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
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
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        if (model.labelColor.isNotEmpty()) {
            holder.itemView.view_label_color.visibility = View.VISIBLE
            holder.itemView.view_label_color.setBackgroundColor(Color.parseColor(model.labelColor))
        } else {
            holder.itemView.view_label_color.visibility = View.GONE
        }

        holder.itemView.tv_card_name.text = model.name

        if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0) {
            // A instance of selected members list.
            val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

            // Here we got the detail list of members and add it to the selected members list as required.
            for (i in context.mAssignedMembersDetailList.indices) {
                for (j in model.assignedTo) {
                    if (context.mAssignedMembersDetailList[i].id == j) {
                        val selectedMember = SelectedMembers(
                            context.mAssignedMembersDetailList[i].id,
                            context.mAssignedMembersDetailList[i].image
                        )

                        selectedMembersList.add(selectedMember)
                    }
                }
            }

            if (selectedMembersList.size > 0) {
                if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                    holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                } else {
                    holder.itemView.rv_card_selected_members_list.visibility = View.VISIBLE

                    holder.itemView.rv_card_selected_members_list.layoutManager =
                        GridLayoutManager(context, 4)
                    val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false) {
                        onClick(position)
                    }
                    holder.itemView.rv_card_selected_members_list.adapter = adapter
                }
            } else {
                holder.itemView.rv_card_selected_members_list.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onClick(position)
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
