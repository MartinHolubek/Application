package com.example.trashhunter.ui.place

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trashhunter.Comment
import com.example.trashhunter.firebase.FirebaseRepository
import com.example.trashhunter.Place
import com.example.trashhunter.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.EventListener

class PlaceViewModel : ViewModel() {
    val TAG = "PLACE_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()

    var savedPlace : MutableLiveData<Place> = MutableLiveData()
    var savedComments : MutableLiveData<List<Comment>> = MutableLiveData()
    var savedUser:MutableLiveData<User> = MutableLiveData()

    fun getPlace(placeID:String): LiveData<Place>{

        var place:Place
        firebaseRepository.getPlaceItem(placeID).addSnapshotListener(EventListener<DocumentSnapshot>{ value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní priatelov")

                return@EventListener
            }

            var place = value?.toObject(Place::class.java)

            savedPlace.value = place
        })
        return savedPlace
    }

    fun saveRating(placeID:String, oldCount: Int,oldRating:Float,newRating:Float,comment:String){
        firebaseRepository.saveRatingByUser(placeID, oldCount, oldRating, newRating,comment).addOnFailureListener {
            Log.e(TAG, "Chyba pri ukladaní hodnotenia")
        }
    }

    fun getComments(placeID: String):LiveData<List<Comment>>{

        firebaseRepository.getComments(placeID).addSnapshotListener(EventListener<QuerySnapshot>{value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní komentárov")
                savedComments.value = null

                return@EventListener
            }

            var savedCommentList : MutableList<Comment> = mutableListOf()
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
}
