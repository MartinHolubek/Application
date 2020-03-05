package com.example.application.ui.place

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.application.FireStoreRepository
import com.example.application.Place
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.EventListener
import java.util.*

class PlaceViewModel : ViewModel() {
    val TAG = "PLACE_VIEW_MODEL"
    var firebaseRepository = FireStoreRepository()

    var savedPlace : MutableLiveData<Place> = MutableLiveData()

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
}
