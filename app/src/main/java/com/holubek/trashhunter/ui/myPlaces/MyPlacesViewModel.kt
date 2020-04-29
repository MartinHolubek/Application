package com.holubek.trashhunter.ui.myPlaces

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.firebase.FirebaseStorage
import com.holubek.trashhunter.Place
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

/**
 * View Model pre fragment na zobrazovanie udalosti, ktoré vytvoril užívateľ
 */
class MyPlacesViewModel : ViewModel() {
    val TAG = "MY_PLACES_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()
    var firebaseStorage = FirebaseStorage()
    var savedPlaces : MutableLiveData<List<Place>> = MutableLiveData()

    /**
     * Vráti zoznam príspevkov z databázy, ktoré vytvoril užívateľ
     */
    fun getPlaces(): LiveData<List<Place>> {
        firebaseRepository.getPlaceItems().addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
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

    /**
     * Odstráni príspevok z databázy
     * @param place objekt príspevku, ktorý sa odstráni z databázy
     */
    fun deletePlace(place:Place){
        firebaseStorage.deleteImage(place.photoBefore!!).addOnFailureListener{
            Log.e(TAG,"Failed to delete image before")
        }
        firebaseStorage.deleteImage(place.photoAfter!!).addOnFailureListener {
            Log.e(TAG,"Failed to delete image after")
        }
        firebaseRepository.deletePlaceItem(place).addOnFailureListener{
            Log.e(TAG,"Failed to delete Address")
        }
    }
}
