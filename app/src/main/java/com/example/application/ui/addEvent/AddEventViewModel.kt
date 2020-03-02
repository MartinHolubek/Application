package com.example.application.ui.addEvent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.example.application.Event
import com.example.application.FireStoreRepository

class AddEventViewModel : ViewModel() {

    val TAG = "ADDEVENT_VIEW_MODEL"
    var firebaseRepository = FireStoreRepository()

    private val _text = MutableLiveData<String>().apply {
        value = "This is slideshow Fragment"
    }
    val text: LiveData<String> = _text

    private var _map = MutableLiveData<ArcGISMap>().apply {
        value = ArcGISMap(Basemap.Type.STREETS_VECTOR, 49.201476197, 18.870735168, 11)
    }
    val map: LiveData<ArcGISMap> = _map

    fun saveEventToFirebase(event : Event){
        firebaseRepository.saveEventItem(event).addOnFailureListener {
            Log.e(TAG, "Chyba pri ukladaní priatela")
        }
    }
}