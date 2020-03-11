package com.example.application.ui.events

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.application.Event
import com.example.application.FireStoreRepository
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class EventsViewModel : ViewModel() {
    val firebaseRepository = FireStoreRepository()

    var savedEvents : MutableLiveData<List<Event>> = MutableLiveData()

    private val _text = MutableLiveData<String>().apply {
        value = "This is tools Fragment"
    }
    val text: LiveData<String> = _text

    /**
     * Metóda, ktorá naplní z databázi pole miest
     * @return vráti pole s miestami
     */
    fun getSavedEvents() : LiveData<List<Event>> {
        firebaseRepository.getAllEventItems().addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null){
                Log.w(TAG,"LISTEN FAILED", e)
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
}