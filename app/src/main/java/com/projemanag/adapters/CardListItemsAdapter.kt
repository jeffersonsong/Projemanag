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
import com.projemanag.utils.SelectedMembersHelper
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
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) =
        bind(holder.itemView, position)

    private fun bind(view: View, position: Int) {
        val model = list[position]

        if (model.labelColor.isNotEmpty()) {
            view.view_label_color.visibility = View.VISIBLE
            view.view_label_color.setBackgroundColor(Color.parseColor(model.labelColor))
        } else {
            view.view_label_color.visibility = View.GONE
        }

        view.tv_card_name.text = model.name

        if ((context as TaskListActivity).mAssignedMembersDetailList.isNotEmpty()) {
            // A instance of selected members list.
            val selectedMembersList: ArrayList<SelectedMembers> =
                SelectedMembersHelper.selectedMembersList(
                    context.mAssignedMembersDetailList,
                    model.assignedTo
                )

            if (selectedMembersList.isNotEmpty()) {
                if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                    view.rv_card_selected_members_list.visibility = View.GONE
                } else {
                    view.rv_card_selected_members_list.apply {
                        visibility = View.VISIBLE
                        layoutManager = GridLayoutManager(context, 4)
                        adapter = CardMemberListItemsAdapter(context, selectedMembersList, false) {
                            onClick(position)
                        }
                    }
                }
            } else {
                view.rv_card_selected_members_list.visibility = View.GONE
            }
        }

        view.setOnClickListener {
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
