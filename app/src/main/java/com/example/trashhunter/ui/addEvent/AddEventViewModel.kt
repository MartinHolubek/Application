package com.example.trashhunter.ui.addEvent

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.example.trashhunter.Event
import com.example.trashhunter.firebase.FirebaseRepository
import com.example.trashhunter.firebase.FirebaseStorage

class AddEventViewModel : ViewModel() {

    val TAG = "ADDEVENT_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()

    var firebaseStorage = FirebaseStorage()

    private val _text = MutableLiveData<String>().apply {
        value = "This is slideshow Fragment"
    }
    val text: LiveData<String> = _text

    private var _map = MutableLiveData<ArcGISMap>().apply {
        value = ArcGISMap(Basemap.Type.STREETS_VECTOR, 49.201476197, 18.870735168, 11)
    }
    val map: LiveData<ArcGISMap> = _map

    fun saveEventToFirebase(event : Event){
        var uri = Uri.parse(event.picture)
        firebaseStorage.saveImageEvent(uri).addOnFailureListener {
            // Handle unsuccessful uploads


        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            event.picture = it.metadata?.path

            firebaseRepository.saveEventItem(event).addOnFailureListener {
                Log.e(TAG, "Chyba pri ukladaní priatela")
            }
        }
    }
}