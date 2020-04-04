package com.example.trashhunter.firebase

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat

class FirebaseStorage() {
    var mFirebaseAuth:FirebaseAuth
    var mFirebaseStorage:FirebaseStorage


    init {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()
    }

    /**
     * Metoda uloží obrazok do firebase storage a vráti cestu v úložisku
     *
     * @return cesta k obrázku v uložisku
     */
    fun saveImageEvent(uri: Uri): UploadTask{
        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = mFirebaseStorage.getReference("Pictures_events/" + mFirebaseAuth.currentUser!!.uid)
        var pictureRef = reference.child("picture_$timeStamp")

        var uploadTask = pictureRef.putFile(uri)


        return uploadTask
    }

    fun saveImageUser(ba: ByteArray?): UploadTask{

        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = mFirebaseStorage.getReference("Pictures_users/" + mFirebaseAuth.currentUser!!.uid)
        var pictureRef = reference.child("picture_$timeStamp")

        var uploadTask = pictureRef.putBytes(ba!!)

        return uploadTask
    }

    fun getImageUser(path : String): Task<ByteArray>{
        val ONE_MEGABYTE: Long = 1024 * 1024
        var photoBeforeRef = mFirebaseStorage
            .reference
            .child(path)
        return photoBeforeRef.getBytes(ONE_MEGABYTE)
    }

    fun deletePlaceImages(path: String):Task<Void>{
        val reference = mFirebaseStorage.getReference(path)

        var task = reference.delete()
        return task
    }

    fun saveImagePlace(ba: ByteArray?, timeStamp: String, after : Boolean): UploadTask{

        val reference = mFirebaseStorage.getReference("Pictures/" + mFirebaseAuth.currentUser!!.uid)
        var pictureRef : StorageReference? = null
        if (after){
            pictureRef = reference.child("pictureAfter_$timeStamp")
        }else{
            pictureRef = reference.child("pictureBefore_$timeStamp")
        }
        var uploadTask = pictureRef?.putBytes(ba!!)

        return uploadTask!!
    }

    fun saveImageAfter(userID: String, eventID: String, ba: ByteArray): UploadTask{
        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = mFirebaseStorage.getReference("Pictures/" + userID)
        return reference.child("pictureAfter_$timeStamp").putBytes(ba)
    }

    fun getImage(path : String): Task<ByteArray>{
        val ONE_MEGABYTE: Long = 1024 * 1024
        var photoBeforeRef = mFirebaseStorage
            .reference
            .child(path)
        return photoBeforeRef.getBytes(ONE_MEGABYTE)
    }
}