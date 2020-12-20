package com.projemanag.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.projemanag.R
import com.projemanag.adapters.CardMemberListItemsAdapter
import com.projemanag.dialogs.LabelColorListDialog
import com.projemanag.dialogs.MembersListDialog
import com.projemanag.firebase.FirestoreClass
import com.projemanag.model.*
import com.projemanag.utils.Constants
import com.projemanag.utils.SelectedMembersHelper
import kotlinx.android.synthetic.main.activity_card_details.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {
    private val store = FirestoreClass()

    // A global variable for board details
    private lateinit var mBoardDetails: Board

    // A global variable for task item position
    private var mTaskListPosition: Int = -1

    // A global variable for card item position
    private var mCardPosition: Int = -1

    // A global variable for selected label color
    private var mSelectedColor: String = ""

    // A global variable for Assigned Members List.
    private lateinit var mMembersDetailList: ArrayList<User>

    // A global variable for selected due date
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()

        setupActionBar()

        val card = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]
        et_name_card_details.setText(card.name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length) // The cursor after the string length

        if (card.labelColor.isNotEmpty()) {
            setColor(card.labelColor)
        }

        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }

        setupSelectedMembersList()

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        mSelectedDueDateMilliSeconds = card.dueDate
        if (mSelectedDueDateMilliSeconds > 0) {
            val selectedDate = dateFormat().format(Date(mSelectedDueDateMilliSeconds))
            tv_select_due_date.text = selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDataPicker()
        }

        btn_update_card_details.setOnClickListener {
            if (et_name_card_details.text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                Toast.makeText(this@CardDetailsActivity, "Enter card name.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_delete_card -> {
                val card = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]
                alertDialogForDeleteCard(card.name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        val card = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]
        setupActionBar(toolbar_card_details_activity, card.name)
    }

    // A function to get all the data that is sent through intent.
    private fun getIntentData() {
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL) as Board
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    /**
     * A function to get the result of add or updating the task list.
     */
    private fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * A function to update card details.
     */
    private fun updateCardDetails() {
        val name = et_name_card_details.text.toString()
        val model = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]

        // Here we have updated the card name using the data model class.
        val card = Card(
            name,
            model.createdBy,
            model.assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        // Here we have assigned the update card details to the task list using the card position.
        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        addUpdateTaskList()
    }

    /**
     * A function to show an alert dialog for the confirmation to delete the card.
     */
    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed
            deleteCard()
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    /**
     * A function to delete the card from the task list.
     */
    private fun deleteCard() {
        // Here we have got the cards list from the task item list using the task list position.
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        // Here we will remove the item from cards list using the card position.
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardsList

        addUpdateTaskList()
    }

    /**
     * A function to remove the text and set the label color to the TextView.
     */
    private fun setColor(color: String) {
        mSelectedColor = color
        tv_select_label_color.text = ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(color))
    }

    /**
     * A function to add some static label colors in the list.
     */
    private fun colorsList(): ArrayList<String> {
        return arrayListOf(
            "#43C86F", "#0C90F1", "#F72400", "#7A8089", "#D57C1D", "#770000", "#0022F8"
        )
    }

    /**
     * A function to launch the label color list dialog.
     */
    private fun labelColorsListDialog() {
        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
            this@CardDetailsActivity,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                setColor(color)
            }
        }
        listDialog.show()
    }

    /**
     * A function to launch and setup assigned members detail list into recyclerview.
     */
    private fun membersListDialog() {
        // Here we get the updated assigned members list
        val card = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]

        flagMemberSelectedStatus(card.assignedTo)

        val listDialog = object : MembersListDialog(
            this@CardDetailsActivity,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!card.assignedTo.contains(user.id)) {
                        card.assignedTo.add(user.id)
                    }

                } else {
                    card.assignedTo.remove(user.id)
                    for (i in mMembersDetailList.indices) {
                        if (mMembersDetailList[i].id == user.id) {
                            mMembersDetailList[i].selected = false
                        }
                    }
                }

                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun flagMemberSelectedStatus(cardAssignedMembersList: ArrayList<String>) {
        if (cardAssignedMembersList.isNotEmpty()) {
            // Here we got the details of assigned members list from the global members list which is passed from the Task List screen.
            for (i in mMembersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }
    }

    /**
     * A function to setup the recyclerView for card assigned members.
     */
    private fun setupSelectedMembersList() {
        // Assigned members of the Card.
        val card = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition]

        val selectedMembersList  =
            SelectedMembersHelper.selectedMembersList(mMembersDetailList, card.assignedTo)

        if (selectedMembersList.isNotEmpty()) {
            // This is for the last item to show.
            selectedMembersList.add(SelectedMembers("", ""))
            tv_select_members.visibility = View.GONE

            rv_selected_members_list.apply {
                visibility = View.VISIBLE
                layoutManager = GridLayoutManager(this@CardDetailsActivity, 6)
                adapter = CardMemberListItemsAdapter(
                    this@CardDetailsActivity,
                    selectedMembersList,
                    true
                ) {
                    membersListDialog()
                }
            }
        } else {
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility = View.GONE
        }
    }

    /**
     * The function to show the DatePicker Dialog and select the due date.
     */
    private fun showDataPicker() {
        val onDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth, 0, 0)
                val theDate = calendar.time

                val sdf = dateFormat()
                val selectedDate = sdf.format(theDate)
                // Selected date it set to the TextView to make it visible to user.
                tv_select_due_date.text = selectedDate

                mSelectedDueDateMilliSeconds = theDate.time
            }

        val c = Calendar.getInstance()
        if (mSelectedDueDateMilliSeconds > 0L) {
            c.timeInMillis = mSelectedDueDateMilliSeconds
        }
        val dpd = DatePickerDialog(
            this,
            onDateSetListener,
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show()
    }

    private fun dateFormat() = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())

    private fun addUpdateTaskList() {
        // Show the progress dialog.
        pleaseWait()
        store.addUpdateTaskList(board = mBoardDetails,
            onSuccess = { addUpdateTaskListSuccess() },
            onFailure = { hideProgressDialog() })
    }
}
