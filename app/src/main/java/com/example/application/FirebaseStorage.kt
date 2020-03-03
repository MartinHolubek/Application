package com.example.application

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat

class FirebaseStorage{
    var mFirebaseAuth:FirebaseAuth
    var mFirebaseUser:FirebaseUser
    var mFirebaseStorage:FirebaseStorage



    constructor(){
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth.currentUser!!
        mFirebaseStorage = FirebaseStorage.getInstance()
    }

    /**
     * Metoda uloží obrazok do firebase storage a vráti cestu v úložisku
     *
     * @return cesta k obrázku v uložisku
     */
    fun saveImageEvent(uri: Uri): UploadTask{
        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = mFirebaseStorage.getReference("Pictures_events/" + mFirebaseUser!!.uid)
        var pictureRef = reference.child("picture_$timeStamp")

        var uploadTask = pictureRef.putFile(uri)

        return uploadTask
    }
}