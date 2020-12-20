package com.projemanag.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.projemanag.R
import com.projemanag.adapters.TaskListItemsAdapter
import com.projemanag.firebase.FirestoreClass
import com.projemanag.model.Board
import com.projemanag.model.Card
import com.projemanag.model.Task
import com.projemanag.model.User
import com.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*


class TaskListActivity : BaseActivity() {
    private val firestore = FirestoreClass()

    // A global variable for Board Details.
    private lateinit var mBoardDetails: Board

    // A global variable for board document id as mBoardDocumentId
    private lateinit var mBoardDocumentId: String

    // A global variable for Assigned Members List.
    lateinit var mAssignedMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
            // Show the progress dialog.
            getBoardDetails(mBoardDocumentId)
        }
    }

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        setupActionBar(toolbar_task_list_activity, mBoardDetails.name)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_members -> {
                gotoMembersScreen()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun gotoMembersScreen() {
        val intent = Intent(this@TaskListActivity, MembersActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        startActivityForResult(intent, MEMBERS_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && (requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE)
        ) {
            getBoardDetails(mBoardDocumentId)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    /**
     * A function to get the result of Board Detail.
     */
    private fun boardDetails(board: Board) {
        mBoardDetails = board

        hideProgressDialog()
        // Call the function to setup action bar.
        setupActionBar()
        getAssignedMembersListDetails()
    }

    /**
     * A function to get the task list name from the adapter class which we will be using to create a new task list in the database.
     */
    fun createTaskList(taskListName: String) {

        Log.e("Task List Name", taskListName)

        // Create and Assign the task details
        val task = Task(taskListName, getCurrentUserID())

        mBoardDetails.taskList.add(0, task) // Add task to the first position of ArrayList
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1) // Remove the last position as we have added the item manually for adding the TaskList.

        addUpdateTaskList()
    }

    /**
     * A function to update the taskList
     */
    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        addUpdateTaskList()
    }

    /**
     * A function to delete the task list from database.
     */
    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        addUpdateTaskList()
    }

    /**
     * A function to get the result of add or updating the task list.
     */
    private fun addUpdateTaskListSuccess() {

        hideProgressDialog()

        // Here get the updated board details.
        // Show the progress dialog.
        getBoardDetails(mBoardDetails.documentId)
    }

    /**
     * A function to create a card and update it in the task list.
     */
    fun addCardToTaskList(position: Int, cardName: String) {

        // Remove the last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val card = Card(cardName, getCurrentUserID(), arrayListOf(getCurrentUserID()))

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        mBoardDetails.taskList[position] = task

        addUpdateTaskList()
    }

    /**
     * A function for viewing and updating card details.
     */
    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        gotoCardDetailsScreen(taskListPosition, cardPosition)
    }

    private fun gotoCardDetailsScreen(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    /**
     * A function to get assigned members detail list.
     */
    private fun boardMembersDetailList(list: ArrayList<User>) {
        mAssignedMembersDetailList = list

        hideProgressDialog()

        // Here we are appending an item view for adding a list task list for the board.
        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.apply {
            layoutManager =
                LinearLayoutManager(
                    this@TaskListActivity,
                    LinearLayoutManager.HORIZONTAL, false
                )

            setHasFixedSize(true)

            adapter = TaskListItemsAdapter(
                this@TaskListActivity,
                mBoardDetails.taskList,
                mAssignedMembersDetailList,
                createTaskList = { taskListName -> createTaskList(taskListName) },
                updateTaskList = { position, listName, task ->
                    updateTaskList(
                        position,
                        listName,
                        task
                    )
                },
                deleteTaskList = { position -> deleteTaskList(position) },
                addCardToTaskList = { position, cardName -> addCardToTaskList(position, cardName) },
                updateCardsInTaskList = { taskListPosition, cards ->
                    updateCardsInTaskList(
                        taskListPosition,
                        cards
                    )
                },
                cardDetails = { taskListPosition, cardPosition ->
                    cardDetails(
                        taskListPosition,
                        cardPosition
                    )
                }
            )
        }
    }

    /**
     * A function to update the card list in the particular task list.
     */
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {

        // Remove the last item
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        mBoardDetails.taskList[taskListPosition].cards = cards

        addUpdateTaskList()
    }

    private fun getAssignedMembersListDetails() {
        pleaseWait()
        firestore.getAssignedMembersListDetails(
            assignedTo = mBoardDetails.assignedTo,
            onSuccess = { usersList -> boardMembersDetailList(usersList) },
            onFailure = { hideProgressDialog() }
        )
    }

    private fun getBoardDetails(boardId: String) {
        pleaseWait()
        firestore.getBoardDetails(
            documentId = boardId,
            onSuccess = { board -> boardDetails(board) },
            onFailure = { hideProgressDialog() })
    }

    private fun addUpdateTaskList() {
        pleaseWait()
        firestore.addUpdateTaskList(board = mBoardDetails,
            onSuccess = { addUpdateTaskListSuccess() },
            onFailure = { hideProgressDialog() })
    }

    /**
     * A companion object to declare the constants.
     */
    companion object {
        //A unique code for starting the activity for result
        const val MEMBERS_REQUEST_CODE: Int = 13

        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }
}
