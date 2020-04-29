package com.holubek.trashhunter.ui.events

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holubek.trashhunter.Event
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.Friend
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

/**
 * View Model fragmentu na zobrazovanie udalostí od priateľov
 */
class EventsViewModel : ViewModel() {
    val firebaseRepository =
        FirebaseRepository()

    var savedEvents : MutableLiveData<List<Event>> = MutableLiveData()
    var savedFriends: MutableLiveData<List<String>> = MutableLiveData()

    private val _text = MutableLiveData<String>().apply {
        value = "This is tools Fragment"
    }
    val text: LiveData<String> = _text

    /**
     * Metóda, ktorá naplní zoznam udalostí z databázi
     * @return vráti zoznam s udalostami
     */
    fun getSavedEvents(): LiveData<List<Event>>{
        firebaseRepository.getFriendItems().addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní priatelov")
                savedFriends.value = null

                return@EventListener
            }

            var savedFriendList : MutableList<String> = mutableListOf()
            for (doc in value!!) {
                var friend = doc.toObject(Friend::class.java)
                savedFriendList.add(friend.uid.toString())
            }

            if (savedFriendList.isEmpty()){
                savedEvents.value = listOf<Event>()
            }else{
                firebaseRepository.getEvents(savedFriendList).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
                    if (e != null) {
                        Log.w(TAG, "Chyba pri načitaní udalosti")
                        savedFriends.value = null

                        return@EventListener
                    }
                    var savedEventsList : MutableList<Event> = mutableListOf()
                    for (doc in value!!){
                        var eventItem = doc.toObject(Event::class.java)
                        savedEventsList.add(eventItem)
                    }

                    savedEvents.value = savedEventsList
                })
            }
            savedFriends.value = savedFriendList
        })
        return savedEvents
    }

    /**
     * Uloží udalosť do zoznamu udalostí na ktorých sa užívateľ zúčastní
     * @param event objekt udalosti, ktorá sa uloží do zoznamu
     */
    fun saveAttendEvent(event: Event) {
        firebaseRepository.saveAttendEvent(event).addOnSuccessListener {
            firebaseRepository.saveJoinedUser(event)
        }
    }
}