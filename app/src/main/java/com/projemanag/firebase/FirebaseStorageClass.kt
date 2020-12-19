package com.projemanag.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.Exception

class FirebaseStorageClass {

    fun uploadImage(
        mSelectedImageFileUri: Uri, fileName: String,
        onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit
    ) {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(fileName)

        //adding the file to reference
        sRef.putFile(mSelectedImageFileUri)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        onSuccess(uri)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}