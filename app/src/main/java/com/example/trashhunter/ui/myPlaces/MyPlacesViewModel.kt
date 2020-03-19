package com.example.trashhunter.ui.myPlaces

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trashhunter.firebase.FirebaseRepository
import com.example.trashhunter.firebase.FirebaseStorage
import com.example.trashhunter.Place
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class MyPlacesViewModel : ViewModel() {
    val TAG = "MY_PLACES_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()
    var firebaseStorage = FirebaseStorage()
    var savedPlaces : MutableLiveData<List<Place>> = MutableLiveData()

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

    fun deletePlace(place:Place){
        firebaseStorage.deletePlaceImages(place.photoBefore!!).addOnFailureListener{
            Log.e(TAG,"Failed to delete image before")
        }
        firebaseStorage.deletePlaceImages(place.photoAfter!!).addOnFailureListener {
            Log.e(TAG,"Failed to delete image after")
        }
        firebaseRepository.deletePlaceItem(place).addOnFailureListener{
            Log.e(TAG,"Failed to delete Address")
        }
    }

    fun getImagePlace(path: String): LiveData<Bitmap>{
        var bmp :MutableLiveData<Bitmap> = MutableLiveData()
        firebaseStorage.getImagePlace(path).addOnSuccessListener {
            // Konvertujeme byteArray na bitmap
            bmp.value = BitmapFactory.decodeByteArray(it, 0, it.size)
            //imageBeforeView.setImageBitmap(Bitmap.createScaledBitmap(bmp, imageBeforeView.width,imageBeforeView.height,false))
            return@addOnSuccessListener
        }.addOnFailureListener {
            // Handle any errors
        }
        return bmp
    }
}
