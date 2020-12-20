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
import com.projemanag.model.User
import com.projemanag.utils.Constants
import com.projemanag.utils.ImageChooserHelper
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException

class MyProfileActivity : BaseActivity() {
    private val store = FirestoreClass()
    private val imageStorage = FirebaseStorageClass()

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    // A global variable for user details.
    private lateinit var mUserDetails: User

    // A global variable for a user profile image URL
    private var mProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar(toolbar_my_profile_activity, resources.getString(R.string.my_profile))

        loadUserData()

        iv_profile_user_image.setOnClickListener {
            ImageChooserHelper.showImageChooserOrRequestPermission(this@MyProfileActivity)
        }

        btn_update.setOnClickListener {
            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                pleaseWait()
                // Call a function to update user details in the database.
                updateUserProfileData()
            }
        }
    }

    // region display user
    private fun loadUserData() {
        store.loadUserData(
            onSuccess = { user -> setUserDataInUI(user) },
            onFailure = { hideProgressDialog() })
    }

    /**
     * A function to set the existing details in UI.
     */
    private fun setUserDataInUI(user: User) {
        // Initialize the user details variable
        mUserDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_profile_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if (user.mobile != 0L) {
            et_mobile.setText(user.mobile.toString())
        }
    }
    // endregion

    // region choose image
    /**
     * This function will identify the result of runtime permission after the user allows or deny permission based on the unique code.
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
            this@MyProfileActivity,
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
            // The uri of selection image from phone storage.
            mSelectedImageFileUri = data.data!!

            try {
                // Load the user image in the ImageView.
                Glide
                    .with(this@MyProfileActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(iv_profile_user_image) // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    //endregion

    // region upload image
    /**
     * A function to upload the selected user image to firebase cloud storage.
     */
    private fun uploadUserImage() {
        pleaseWait()

        if (mSelectedImageFileUri != null) {
            //getting the storage reference
            val fileName = ("USER_IMAGE" + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(this@MyProfileActivity, mSelectedImageFileUri))

            imageStorage.uploadImage(
                mSelectedImageFileUri!!, fileName,
                onSuccess = { uri ->
                    Log.e("Downloadable Image URL", uri.toString())
                    // assign the image url to the variable.
                    mProfileImageURL = uri.toString()

                    // Call a function to update user details in the database.
                    updateUserProfileData()
                },
                onFailure = { exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
            )
        }
    }
    // endregion

    // region update profile
    /**
     * A function to update the user profile details into the database.
     */
    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (et_name.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        if (et_mobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
        }

        // Update the data in the database.
        updateUserProfileData(userHashMap)
    }

    private fun updateUserProfileData(userHashMap: HashMap<String, Any>) {
        store.updateUserProfileData(
            userHashMap = userHashMap,
            onSuccess = { profileUpdateSuccess() },
            onFailure = { hideProgressDialog() })
    }

    /**
     * A function to notify the user profile is updated successfully.
     */
    private fun profileUpdateSuccess() {
        hideProgressDialog()

        Toast.makeText(this@MyProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT)
            .show()

        setResult(Activity.RESULT_OK)
        finish()
    }
    // endregion
}
