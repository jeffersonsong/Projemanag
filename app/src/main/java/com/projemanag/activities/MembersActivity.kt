package com.projemanag.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.projemanag.R
import com.projemanag.adapters.MemberListItemsAdapter
import com.projemanag.firebase.FirestoreClass
import com.projemanag.model.Board
import com.projemanag.model.User
import com.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.dialog_search_member.*

class MembersActivity : BaseActivity() {
    private val store = FirestoreClass()

    // A global variable for Board Details.
    private lateinit var mBoardDetails: Board

    // A global variable for Assigned Members List.
    private lateinit var mAssignedMembersList: ArrayList<User>

    // A global variable for notifying any changes done or not in the assigned members list.
    private var anyChangesDone: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        setupActionBar()
        getAssignedMembersListDetails()
    }

    override fun onBackPressed() {
        if (anyChangesDone) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        setupActionBar(toolbar_members_activity)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * A function to setup assigned members list into recyclerview.
     */
    private fun setupMembersList(list: ArrayList<User>) {
        mAssignedMembersList = list
        hideProgressDialog()
        rv_members_list.layoutManager = LinearLayoutManager(this@MembersActivity)
        rv_members_list.setHasFixedSize(true)
        val adapter = MemberListItemsAdapter(this@MembersActivity, list) { position, user, action ->
        }
        rv_members_list.adapter = adapter
    }

    /**
     * Method is used to show the Custom Dialog.
     */
    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        /*Set the screen content from a layout resource.
          The resource will be inflated, adding all top-level views to the screen.*/
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener {
            val email = dialog.et_email_search_member.text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                getMemberDetails(email)
            } else {
                showErrorSnackBar("Please enter members email address.")
            }
        }
        dialog.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }
        //Start the dialog and display it on screen.
        dialog.show()
    }

    private fun memberDetails(user: User) {
        mBoardDetails.assignedTo.add(user.id)
        assignMemberToBoard(user)
    }

    /**
     * A function to get the result of assigning the members.
     */
    private fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesDone = true
        setupMembersList(mAssignedMembersList)
        SendNotificationToUserAsyncTask(mBoardDetails.name,
            user.fcmToken,
            mAssignedMembersList[0].name,
            { pleaseWait() },
            { hideProgressDialog() }).execute()
    }

    private fun assignMemberToBoard(user: User) {
        store.assignMemberToBoard(
            board = mBoardDetails, user = user,
            onSuccess = { user -> memberAssignSuccess(user) },
            onFailure = { hideProgressDialog() })
    }

    private fun getAssignedMembersListDetails() {
        // Show the progress dialog.
        pleaseWait()
        store.getAssignedMembersListDetails(
            assignedTo = mBoardDetails.assignedTo,
            onSuccess = { usersList -> setupMembersList(usersList) },
            onFailure = { hideProgressDialog() }
        )
    }

    private fun getMemberDetails(email: String) {
        // Show the progress dialog.
        pleaseWait()
        store.getMemberDetails(
            email = email,
            onUserFound = { user -> memberDetails(user) },
            noUserFound = {
                hideProgressDialog()
                showErrorSnackBar("No such member found.")
            },
            onFailure = { hideProgressDialog() })
    }
}
