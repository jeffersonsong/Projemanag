package com.projemanag.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.projemanag.model.Board
import com.projemanag.model.User
import com.projemanag.utils.Constants
import java.lang.Exception

/**
 * A custom class where we will add the operation performed for the firestore database.
 */
class FirestoreClass {
    private val authentication = FirebaseAuthClass()
    // Create a instance of Firebase Firestore
    private val mFireStore = FirebaseFirestore.getInstance()

    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(userInfo: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it.
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e(javaClass.simpleName, "Error writing document", e)
            }
    }

    /**
     * A function to SignIn using firebase and get the user details from Firestore Database.
     */
    fun loadUserData(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = document.toObject(User::class.java)!!

                // Here call a function of base activity for transferring the result to it.
                onSuccess(loggedInUser)
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
                onFailure(e)
                Log.e(javaClass.simpleName, "Error while getting loggedIn user details", e)
            }
    }

    /**
     * A function to update the user profile data into the database.
     */
    fun updateUserProfileData(
        userHashMap: HashMap<String, Any>,
        onSuccess: () -> Unit, onFailure: () -> Unit
    ) {
        mFireStore.collection(Constants.USERS) // Collection Name
            .document(getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                Log.e(javaClass.simpleName, "Data updated successfully!")
                // Notify the success result.
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure()
                Log.e(javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /**
     * A function for creating a board and making an entry in the database.
     */
    fun createBoard(board: Board, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(javaClass.simpleName, "Board created successfully.")

                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e(javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /**
     * A function to get the list of created boards from the database.
     */
    fun getBoardsList(onSuccess: (ArrayList<Board>) -> Unit, onFailure: (Exception) -> Unit) {

        // The collection name for BOARDS
        mFireStore.collection(Constants.BOARDS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                Log.e(javaClass.simpleName, document.documents.toString())
                // Here we have created a new instance for Boards ArrayList.
                val boardsList: ArrayList<Board> = ArrayList()

                // A for loop as per the list of documents to convert them into Boards ArrayList.
                for (i in document.documents) {

                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id

                    boardsList.add(board)
                }

                // Here pass the result to the base activity.
                onSuccess(boardsList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e(javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /**
     * A function to get the Board Details.
     */
    fun getBoardDetails(
        documentId: String, onSuccess: (Board) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id

                // Send the result of board to the base activity.
                onSuccess(board)
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e(javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /**
     * A function to create a task list in the board detail.
     */
    fun addUpdateTaskList(board: Board, onSuccess: ()->Unit, onFailure: ()->Unit) {

        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(javaClass.simpleName, "TaskList updated successfully.")

                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure()
                Log.e(javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /**
     * A function to get the list of user details which is assigned to the board.
     */
    fun getAssignedMembersListDetails(
        assignedTo: ArrayList<String>,
        onSuccess: (ArrayList<User>) -> Unit, onFailure: (Exception) -> Unit
    ) {

        mFireStore.collection(Constants.USERS) // Collection Name
            .whereIn(
                Constants.ID,
                assignedTo
            ) // Here the database field name and the id's of the members.
            .get()
            .addOnSuccessListener { document ->
                Log.e(javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    // Convert all the document snapshot to the object using the data model class.
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                onSuccess(usersList)
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e(javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /**
     * A function to get the user details from Firestore Database using the email address.
     */
    fun getMemberDetails(
        email: String,
        onUserFound: (User) -> Unit,
        noUserFound: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                Log.e(javaClass.simpleName, document.documents.toString())

                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    // Here call a function of base activity for transferring the result to it.
                    onUserFound(user)
                } else {
                    noUserFound()
                }

            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e(javaClass.simpleName, "Error while getting user details", e)
            }
    }

    /**
     * A function to assign a updated members list to board.
     */
    fun assignMemberToBoard(
        board: Board, user: User,
        onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit
    ) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(javaClass.simpleName, "TaskList updated successfully.")
                onSuccess(user)
            }
            .addOnFailureListener { e ->
                onFailure(e)
                Log.e(javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    /**
     * A function for getting the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        return authentication.getCurrentUserID()
    }
}
