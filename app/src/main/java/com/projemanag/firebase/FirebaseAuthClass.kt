package com.projemanag.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.projemanag.model.User
import java.lang.Exception

class FirebaseAuthClass {

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Calling the FirestoreClass signInUser function to get the data of user from database.
                    onSuccess()
                } else {
                    onFailure(task.exception!!)
                }
            }
    }

    fun createUserWithEmailAndPassword(
        name: String,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // If the registration is successfully done
                if (task.isSuccessful) {
                    // Firebase registered user
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    // Registered Email
                    val registeredEmail = firebaseUser.email!!
                    val user = User(
                        firebaseUser.uid, name, registeredEmail
                    )

                    // call the registerUser function of FirestoreClass to make an entry in the database.
                    onSuccess(user)
                } else {
                    onFailure(task.exception!!)
                }
            }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}
