package com.example.application.ui.place

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.example.application.Comment
import com.example.application.FireStoreRepository
import com.example.application.Place
import com.example.application.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.EventListener
import java.util.*

class PlaceViewModel : ViewModel() {
    val TAG = "PLACE_VIEW_MODEL"
    var firebaseRepository = FireStoreRepository()

    private var _map = MutableLiveData<ArcGISMap>().apply {
        value = ArcGISMap(Basemap.Type.STREETS_VECTOR, 49.201476197, 18.870735168, 11)
    }
    val map: LiveData<ArcGISMap> = _map

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
