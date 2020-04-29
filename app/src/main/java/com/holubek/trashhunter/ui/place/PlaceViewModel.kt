package com.holubek.trashhunter.ui.place

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holubek.trashhunter.Comment
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.Place
import com.holubek.trashhunter.User
import com.holubek.trashhunter.firebase.FirebaseStorage
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat

/**
 * View Model pre fragment na zobrazenie informácií o označenom mieste
 */
class PlaceViewModel : ViewModel() {
    val TAG = "PLACE_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()
    var firebaseStorage = FirebaseStorage()

    var savedPlace : MutableLiveData<Place> = MutableLiveData()
    var savedComments : MutableLiveData<List<Comment>> = MutableLiveData()
    var savedUser:MutableLiveData<User> = MutableLiveData()

    /**
     * Vráti miesto z databázy
     * @param placeID jedinečný identifikátor označeného miesta
     * @return objekt označeného miesta
     */
    fun getPlace(placeID:String): LiveData<Place>{
        firebaseRepository.getPlaceItem(placeID).addSnapshotListener(EventListener<DocumentSnapshot>{ value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní priatelov")

                return@EventListener
            }
            savedPlace.value = value?.toObject(Place::class.java)
        })
        return savedPlace
    }

    /**
     * uloží hodnotenie od užívateľa do databázy
     * @param ID jedinečný identifikátor miesta
     * @param oldCount počet hodnotení miesta
     * @param oldRating starý súčet hodnotení miesta
     * @param newRating nová hodnota hodnotenia miesta
     * @param comment objekt komentár k miestu
     */
    fun saveRating(placeID:String, oldCount: Int,oldRating:Float,newRating:Float,comment:String){
        firebaseRepository.saveRatingByUser(placeID, oldCount, oldRating, newRating,comment).addOnFailureListener {
            Log.e(TAG, "Chyba pri ukladaní hodnotenia")
        }
    }

    /**
     * Vráti zoznam komentárov z databázy pre konkrétne miesto
     * @param placeID jedinečný identifikátor označeného miesta
     * @return zoznam komentárov
     */
    fun getComments(placeID: String):LiveData<List<Comment>>{

        firebaseRepository.getPlaceComments(placeID).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní komentárov")
                savedComments.value = null

                return@EventListener
            }

            val savedCommentList : MutableList<Comment> = mutableListOf()
            for (doc in value!!) {
                var comment = doc.toObject(Comment::class.java)

                savedCommentList.add(comment)
            }
            savedComments.value = savedCommentList
        })

        return savedComments
    }

    /**
     * Aktualizuje hodnoty označeného miesta po vyčistení
     * @param placeID jedinečný identifikátor označeného miesta
     * @param ba bajtové pole reprezentujúce fotku po vyčistení
     */
    fun clearPlace(placeID: String, ba : ByteArray){
        var userID = placeID.dropLast(15)
        var path: String
        firebaseStorage.saveImageAfter(userID,ba).addOnSuccessListener { taskSnapshot ->
            path = taskSnapshot.metadata?.path!!

            firebaseRepository.updatePlace(placeID, path).addOnFailureListener{
                Log.e("placeViewModel", "Chyba pri aktualizovani prispevku")
            }
        }
    }
}
