package com.example.application.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.application.FireStoreRepository
import com.example.application.Place
import com.example.application.RetrieveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList


class HomeViewModel : ViewModel() {

    val TAG = "HOME_VIEW_MODEL"
    var firebaseRepository = FireStoreRepository()

    var savedPlaces : MutableLiveData<List<Place>> = MutableLiveData()

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    var text: LiveData<String> = _text


    /**
     * Metóda, ktorá naplní z databázi pole miest
     * @return vráti pole s miestami
     */
    fun getSavedPlaces() : LiveData<List<Place>> {
        firebaseRepository.getPlaceItem().addSnapshotListener(EventListener<QuerySnapshot>{value,e ->
            if (e != null){
                Log.w(TAG,"LISTEN FAILED", e)
                savedPlaces.value = null
                return@EventListener
            }

            var savedPlacesList : MutableList<Place> = mutableListOf()
            for (doc in value!!){
                var placeItem = doc.toObject(Place::class.java)
                savedPlacesList.add(placeItem)
            }

            savedPlaces.value = savedPlacesList
        })
        return savedPlaces
    }

}