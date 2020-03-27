package com.example.trashhunter.ui.place

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trashhunter.Comment
import com.example.trashhunter.firebase.FirebaseRepository
import com.example.trashhunter.Place
import com.example.trashhunter.User
import com.example.trashhunter.firebase.FirebaseStorage
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat

class PlaceViewModel : ViewModel() {
    val TAG = "PLACE_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()
    var firebaseStorage = FirebaseStorage()

    var savedPlace : MutableLiveData<Place> = MutableLiveData()
    var savedComments : MutableLiveData<List<Comment>> = MutableLiveData()
    var savedUser:MutableLiveData<User> = MutableLiveData()

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

    fun saveRating(placeID:String, oldCount: Int,oldRating:Float,newRating:Float,comment:String){
        firebaseRepository.saveRatingByUser(placeID, oldCount, oldRating, newRating,comment).addOnFailureListener {
            Log.e(TAG, "Chyba pri ukladaní hodnotenia")
        }
    }

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

    fun getUser(userID: String): LiveData<User> {
        var user:User
        firebaseRepository.getPlaceItem(userID).addSnapshotListener(EventListener<DocumentSnapshot>{ value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní uživatela")

                return@EventListener
            }

            user = value?.toObject(User::class.java)!!

            savedUser.value = user
        })
        return savedUser
    }
    fun clearPlace(placeID: String, ba : ByteArray){
        var userID = placeID.dropLast(15)
        var path: String
        firebaseStorage.saveImageAfter(userID,placeID,ba).addOnSuccessListener { taskSnapshot ->
            path = taskSnapshot.metadata?.path!!

            firebaseRepository.updatePlace(placeID, path).addOnFailureListener{
                Log.e("placeViewModel", "Chyba pri aktualizovani prispevku")
            }
        }
    }
}
