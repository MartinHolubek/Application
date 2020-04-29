package com.holubek.trashhunter.ui.myEvents

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holubek.trashhunter.Event
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.firebase.FirebaseStorage
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

/**
 * View Model pre fragment na zobrazenie udalosti, ktoré vytvoril užívateľ
 */
class MyEventsViewModel : ViewModel() {
    val firebaseRepository =
        FirebaseRepository()
    val firebaseStorage = FirebaseStorage()

    var savedEvents : MutableLiveData<List<Event>> = MutableLiveData()

    private val _text = MutableLiveData<String>().apply {
        value = "This is tools Fragment"
    }
    val text: LiveData<String> = _text

    /**
     * Metóda, ktorá naplní zoznam udalosi z databázy
     * @return vráti zoznam s udalostami, ktoré vytvoril užívateľ
     */
    fun getSavedEvents() : LiveData<List<Event>> {
        firebaseRepository.getMyEventItems().addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null){
                Log.w(ContentValues.TAG,"LISTEN FAILED", e)
                savedEvents.value = null
                return@EventListener
            }

            var savedEventsList : MutableList<Event> = mutableListOf()
            for (doc in value!!){
                var eventItem = doc.toObject(Event::class.java)
                savedEventsList.add(eventItem)
            }

            savedEvents.value = savedEventsList
        })
        return savedEvents
    }

    /**
     * Odstráni udalosť z databázy
     * @param event objekt udalosti, ktorý sa odstráni
     */
    fun deleteEvent(event: Event){
        firebaseStorage.deleteImage(event.picture!!).addOnFailureListener{
            Log.e("DELIMAGE","Failed to delete event image")
        }
        firebaseRepository.deleteEventItem(event).addOnFailureListener{
            Log.e("DELIMAGE","Failed to delete event image")
        }
    }
}
