package com.holubek.trashhunter.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat

/**
 * Trieda na načítavanie a ukladanie fotografií z úložiska firebase
 */
class FirebaseStorage() {
    var mFirebaseAuth:FirebaseAuth
    var mFirebaseStorage:FirebaseStorage

    init {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()
    }

    /**
     * Metoda uloží titulný obrázok udalosti do firebase storage a vráti cestu v úložisku
     * @param ba Bajtové pole  reprezentujúce obrázok
     * @return UploadTask, z ktorého vieme zistiť či operácia skončila úspešne
     */
    fun saveImageEvent(ba: ByteArray): UploadTask{
        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = mFirebaseStorage.getReference("Pictures_events/" + mFirebaseAuth.currentUser!!.uid)
        var pictureRef = reference.child("picture_$timeStamp")
        var uploadTask = pictureRef.putBytes(ba)

        return uploadTask
    }

    /**
     * Metoda uloží profilovú fotku užívateľa do firebase storage a vráti cestu v úložisku
     *@param ba Bajtové pole  reprezentujúce obrázok
     * @return UploadTask, z ktorého vieme zistiť či operácia skončila úspešne
     */
    fun saveImageUser(ba: ByteArray?): UploadTask{

        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = mFirebaseStorage.getReference("Pictures_users/" + mFirebaseAuth.currentUser!!.uid)
        var pictureRef = reference.child("picture_$timeStamp")

        var uploadTask = pictureRef.putBytes(ba!!)

        return uploadTask
    }

    /**
     * Zmazanie obrázku v úložisku firebase
     * @param path cesta k obrázku v úložisku firebase
     */
    fun deleteImage(path: String):Task<Void>{
        val reference = mFirebaseStorage.getReference(path)
        var task = reference.delete()

        return task
    }

    /**
     * Uloží obrázok miesta do uložiska firebase
     * @param ba bajtové pole reprezentujúce obrázok
     * @param timeStamp časová pečiatka vytvorenia obrázka
     * @param after Ak je hodnota true, potom ukladáme obrázok po vyčistení, ak je hodnota false,
     * tak ukladáme obrazok pred čistením
     * @return UploadTask, z ktorého vieme zistiť či operácia skončila úspešne
     */
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

    /**
     * Uloží obrázok miesta po vyčistení
     * @param userID jedinečný identifikátor užívateľa, ktorý vytvoril príspevok
     * @param ba bajtové pole reprezenujúce obrázok
     * @return UploadTask, z ktorého vieme zistiť či operácia skončila úspešne
     */
    fun saveImageAfter(userID: String, ba: ByteArray): UploadTask{
        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = mFirebaseStorage.getReference("Pictures/" + userID)
        return reference.child("pictureAfter_$timeStamp").putBytes(ba)
    }

    /**
     * Získanie Bajtového poľa reprezentujúci obrázok z úložiska Firebase
     * @param path Cesta k fotke v úložisku firebase
     * @return Task s bajtovým poľom
     */
    fun getImage(path : String): Task<ByteArray>{
        val ONE_MEGABYTE: Long = 1024 * 1024
        var photoBeforeRef = mFirebaseStorage
            .reference
            .child(path)
        return photoBeforeRef.getBytes(ONE_MEGABYTE)
    }
}