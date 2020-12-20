package com.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.projemanag.R
import com.projemanag.firebase.FirebaseAuthClass
import com.projemanag.firebase.FirestoreClass
import com.projemanag.model.User
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {
    private val firestore = FirestoreClass()
    private val authentication = FirebaseAuthClass()

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_sign_in)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {
        setupActionBar(toolbar_sign_in_activity)
    }

    /**
     * A function for Sign-In using the registered user using the email and password.
     */
    private fun signInRegisteredUser() {
        // Here we get the text from editText and trim the space
        val email: String = et_email.text.toString().trim { it <= ' ' }
        val password: String = et_password.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            // Show the progress dialog.
            pleaseWait()

            // Sign-In using FirebaseAuth
            signInWithEmailAndPassword(email, password)
        }
    }

    /**
     * A function to validate the entries of a user.
     */
    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to get the user details from the firestore database after authentication.
     */
    private fun signInSuccess(user: User) {
        hideProgressDialog()
        gotoMainScreen()
        this.finish()
    }

    private fun gotoMainScreen() {
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        authentication.signInWithEmailAndPassword(
            email, password,
            { loadUserData() },
            { e ->
                Toast.makeText(
                    this@SignInActivity,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            })
    }

    private fun loadUserData() {
        firestore.loadUserData(
            onSuccess = { user -> signInSuccess(user) },
            onFailure = { hideProgressDialog() })
    }
}
