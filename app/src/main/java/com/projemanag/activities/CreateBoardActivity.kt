package com.projemanag.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.projemanag.R
import com.projemanag.firebase.FirebaseStorageClass
import com.projemanag.firebase.FirestoreClass
import com.projemanag.model.Board
import com.projemanag.utils.Constants
import com.projemanag.utils.ImageChooserHelper
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private val store = FirestoreClass()
    private val imageStorage = FirebaseStorageClass()

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    // A global variable for Username
    private lateinit var mUserName: String

    // A global variable for a board image URL
    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        iv_board_image.setOnClickListener { view ->
            ImageChooserHelper.showImageChooserOrRequestPermission(this@CreateBoardActivity)
        }

        btn_create.setOnClickListener {
            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            } else {
                pleaseWait()
                // Call a function to update create a board.
                createBoard()
            }
        }
    }

    /**
     * This function will notify the user after tapping on allow or deny
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ImageChooserHelper.onRequestPermissionsResultForImageChooser(
            this@CreateBoardActivity,
            requestCode,
            grantResults
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data

            try {
                // Load the board image in the ImageView.
                Glide
                    .with(this@CreateBoardActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(iv_board_image) // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * A function to setup action bar
     */
    private fun setupActionBar() {
        setupActionBar(toolbar_create_board_activity)
    }

    /**
     * A function to upload the Board Image to storage and getting the downloadable URL of the image.
     */
    private fun uploadBoardImage() {
        pleaseWait()
        //getting the storage reference
        val fileName = ("BOARD_IMAGE" + System.currentTimeMillis() + "."
                + Constants.getFileExtension(this@CreateBoardActivity, mSelectedImageFileUri))
        imageStorage.uploadImage(
            mSelectedImageFileUri = mSelectedImageFileUri!!,
            fileName = fileName,
            onSuccess = { uri ->
                Log.e("Downloadable Image URL", uri.toString())

                // assign the image url to the variable.
                mBoardImageURL = uri.toString()

                // Call a function to create the board.
                createBoard()
            },
            onFailure = { exception ->
                Toast.makeText(
                    this@CreateBoardActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
        )
    }

    /**
     * A function to make an entry of a board in the database.
     */
    private fun createBoard() {
        //  A list is created to add the assigned menu_members.
        //  This can be modified later on as of now the user itself will be the member of the board.
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID()) // adding the current user id.

        // Creating the instance of the Board and adding the values as per parameters.
        val board = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        createBoard(board)
    }

    /**
     * A function for notifying the board is created successfully.
     */
    private fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun createBoard(board: Board) {
        store.createBoard(
            board = board,
            onSuccess = {
                Toast.makeText(
                    this@CreateBoardActivity,
                    "Board created successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                boardCreatedSuccessfully()
            },
            onFailure = { hideProgressDialog() })
    }
}
