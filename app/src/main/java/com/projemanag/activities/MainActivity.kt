package com.projemanag.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.projemanag.R
import com.projemanag.adapters.BoardItemsAdapter
import com.projemanag.firebase.FirebaseAuthClass
import com.projemanag.firebase.FirestoreClass
import com.projemanag.model.Board
import com.projemanag.model.User
import com.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val store = FirestoreClass()
    private val authentication = FirebaseAuthClass()

    // A global variable for User Name
    private lateinit var mUserName: String

    // A global variable for SharedPreferences
    private lateinit var mSharedPreferences: SharedPreferences

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)

        // This is used to align the xml view to this class
        setContentView(R.layout.activity_main)

        setupActionBar()

        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        nav_view.setNavigationItemSelectedListener(this)

        mSharedPreferences =
            this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        // Variable is used get the value either token is updated in the database or not.
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        // Here if the token is already updated than we don't need to update it every time.
        if (tokenUpdated) {
            // Get the current logged in user details.
            // Show the progress dialog.
            loadUserData(true)
        } else {
            FirebaseInstanceId.getInstance()
                .instanceId.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult.token)
                }
        }

        fab_create_board.setOnClickListener {
            gotoCreateBoardScreen()
        }
    }

    // region Action bar
    private fun setupActionBar() {
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    /**
     * A function for opening and closing the Navigation Drawer.
     */
    private fun toggleDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }
    // endregion

    // region navigation item selected
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
                gotoMyProfileScreen()
            }

            R.id.nav_sign_out -> {
                // Here sign outs the user from firebase in this device.
                signOut()

                mSharedPreferences.edit().clear().apply()

                // Send the user to the intro screen of the application.
                gotoIntroScreen()
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun gotoMyProfileScreen() {
        val intent = Intent(this@MainActivity, MyProfileActivity::class.java)
        startActivityForResult(intent, MY_PROFILE_REQUEST_CODE)
    }

    private fun signOut() {
        authentication.signOut()
    }

    private fun gotoIntroScreen() {
        val intent = Intent(this, IntroActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    // endregion

    // region load user data
    private fun loadUserData(readBoardsList: Boolean = false) {
        pleaseWait()
        store.loadUserData(
            onSuccess = { user -> updateNavigationUserDetails(user, readBoardsList) },
            onFailure = { hideProgressDialog() })
    }

    /**
     * A function to get the current user details from firebase.
     */
    private fun updateNavigationUserDetails(user: User, readBoardsList: Boolean = false) {
        hideProgressDialog()

        mUserName = user.name

        // The instance of the header view of the navigation view.
        val headerView = nav_view.getHeaderView(0)
        // The instance of the user image of the navigation view.
        val navUserImage = headerView.findViewById<ImageView>(R.id.iv_user_image)

        // Load the user image in the ImageView.
        Glide
            .with(this@MainActivity)
            .load(user.image) // URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder) // A default place holder
            .into(navUserImage) // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername.text = user.name

        if (readBoardsList) {
            getBoardsList()
        }
    }

    private fun getBoardsList() {
        // Show the progress dialog.
        pleaseWait()
        store.getBoardsList(
            onSuccess = { boardsList -> populateBoardsListToUI(boardsList) },
            onFailure = { hideProgressDialog() })
    }

    /**
     * A function to populate the result of BOARDS list in the UI i.e in the recyclerView.
     */
    private fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()
        if (boardsList.isNotEmpty()) {
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.apply {
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(this@MainActivity)
                setHasFixedSize(true)
                adapter = BoardItemsAdapter(
                    this@MainActivity,
                    boardsList
                ) { position: Int, model: Board ->
                    gotoTaskListScreen(model)
                }
            }

        } else {
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    private fun gotoTaskListScreen(model: Board) {
        val intent = Intent(this@MainActivity, TaskListActivity::class.java)
        intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
        startActivity(intent)
    }
    // endregion

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MY_PROFILE_REQUEST_CODE) {
                // Get the user updated details.
                loadUserData()
            } else if (requestCode == CREATE_BOARD_REQUEST_CODE) {
                // Get the latest boards list.
                getBoardsList()
            }
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun gotoCreateBoardScreen() {
        val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
        intent.putExtra(Constants.NAME, mUserName)
        startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            // A double back press function is added in Base Activity.
            doubleBackToExit()
        }
    }

    // region update FCM Token
    /**
     * A function to update the user's FCM token into the database.
     */
    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        // Update the data in the database.
        // Show the progress dialog.
        updateUserProfileData(userHashMap)
    }

    private fun updateUserProfileData(userHashMap: HashMap<String, Any>) {
        pleaseWait()
        store.updateUserProfileData(
            userHashMap = userHashMap,
            onSuccess = { tokenUpdateSuccess() },
            onFailure = { hideProgressDialog() })
    }

    /**
     * A function to notify the token is updated successfully in the database.
     */
    private fun tokenUpdateSuccess() {
        hideProgressDialog()
        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        // Get the current logged in user details.
        // Show the progress dialog.
        loadUserData(true)
    }
    // endregion

    /**
     * A companion object to declare the constants.
     */
    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11

        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }
}
