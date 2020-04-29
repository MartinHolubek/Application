package com.holubek.trashhunter.ui.addEvent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.firebase.FirebaseStorage
import com.holubek.trashhunter.Event

/**
 * View Model pre fragment vytvorenia udalosti
 */
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

    /**
     * Uloží objekt udalosť do databázy
     * @param event objekt udalosti
     * @param data  bajtové pole reprezentujúce obrázok udalosti
     */
    fun saveEventToFirebase(event : Event, data:ByteArray){

        firebaseStorage.saveImageEvent(data).addOnFailureListener {
            Log.e(TAG, "Chyba pri ukladaní obrázka do úložiska")

        }.addOnSuccessListener {
            // taskSnapshot.metadata obsahuje cestu k obrázku v úložisku
            event.picture = it.metadata?.path

            firebaseRepository.saveEventItem(event).addOnFailureListener {
                Log.e(TAG, "Chyba pri ukladaní priatela")
            }
        }

    }
}