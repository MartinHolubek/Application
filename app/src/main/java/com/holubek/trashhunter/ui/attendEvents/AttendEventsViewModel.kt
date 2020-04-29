package com.holubek.trashhunter.ui.attendEvents

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holubek.trashhunter.Event
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

/**
 * View Model pre fragment na zobrazovanie udalosti na ktorých sa užívateľ zúčastní
 */
class AttendEventsViewModel : ViewModel() {
    val TAG = "ATTEND_VIEW_MODEL"
    val firebaseRepository =
        FirebaseRepository()

    var savedEvents : MutableLiveData<ArrayList<Event>> = MutableLiveData()

    /**
     * Odoberie udalosť zo zoznamu udalosti na ktorých sa užívateľ zúčastní
     * @param event objekt udalosti
     */
    fun deleteEvent(event:Event){
        firebaseRepository.deleteAttendEvent(event).addOnSuccessListener {
            firebaseRepository.deleteJoinedUser(event)
        }
    }

    /**
     * Vráti zoznam udalosti na ktorých sa užívateľ zúčastní
     */
    fun getSavedEvents() : LiveData<ArrayList<Event>> {
        firebaseRepository.getAttendEvents().addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null){
                Log.w(ContentValues.TAG,"LISTEN FAILED", e)
                savedEvents.value = null
                return@EventListener
            }

            var savedEventsList : MutableList<Event> = mutableListOf()
            for (doc in value!!){
                var event: DocumentReference =  doc["event"] as DocumentReference
                event.addSnapshotListener(EventListener<DocumentSnapshot>{ value, e ->
                    if (e != null) {
                        Log.w(TAG, "Chyba pri načitaní eventu")

                        return@EventListener
                    }
                    var event = value?.toObject(Event::class.java)

                    if (event != null){
                        savedEventsList.add(event)
                    }
                    savedEvents.value = ArrayList(savedEventsList)
                })
            }
        })
        return savedEvents
    }
}
