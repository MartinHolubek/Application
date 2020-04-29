package com.holubek.trashhunter.ui.event

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holubek.trashhunter.Comment
import com.holubek.trashhunter.Event
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot


/**
 * View Model fragmentu na zobrazenie detailu udalosti
 */
class EventViewModel : ViewModel() {
    var firebaseRepository = FirebaseRepository()
    var savedEvent : MutableLiveData<Event> = MutableLiveData()
    var savedComments : MutableLiveData<List<Comment>> = MutableLiveData()

    /**
     * Získanie udalosti z databázy
     * @param eventID jedinečný identifikátor udalosti
     * @param userID jedinečný identifikátor užívateľa
     * @return Ak stahovanie udalosti z databázy skončí úspešne, tak vráti udalosť
     */
    fun getPlace(eventID:String,userID:String): LiveData<Event> {
        firebaseRepository.getEventItem(eventID, userID).addSnapshotListener(EventListener<DocumentSnapshot>{ value, e ->
            if (e != null) {
                Log.w("Event", "Chyba pri načitaní priatelov")

                return@EventListener
            }

            var event = value?.toObject(Event::class.java)

            savedEvent.value = event
        })
        return savedEvent
    }

    /**
     * Uloží hodnotenie od užívateľa do databázy
     * @param eventID jedinečný identifikátor udalosti
     * @param userID jedinečný identifikátor užívateľa
     * @param oldCount počet hodnotení udalosti
     * @param oldRating starý súčet hodnotení udalosti
     * @param newRating nová hodnota hodnotena udalosti
     * @param comment objekt komentár k udalosti
     */
    fun saveRating(eventID:String,userID:String, oldCount: Int,oldRating:Float,newRating:Float,comment:String){
        firebaseRepository.saveEventRatingByUser(eventID,userID, oldCount, oldRating, newRating,comment).addOnFailureListener {
            Log.e("eventViewModel", "Chyba pri ukladaní hodnotenia")
        }
    }

    /**
     * Získanie zoznamu komentárov
     * @param eventID jedinečný identifikátor udalosti
     * @param userID jedinečný identifikátor užívateľa
     * @return Zoznam komentárov
     */
    fun getComments(eventID: String, userID : String):LiveData<List<Comment>>{
        firebaseRepository.getEventComments(eventID,userID).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null) {
                Log.w("eventViewModel", "Chyba pri načitaní komentárov")
                savedComments.value = null

                return@EventListener
            }

            var savedCommentList : MutableList<Comment> = mutableListOf()
            for (doc in value!!) {
                var comment = doc.toObject(Comment::class.java)
                savedCommentList.add(comment)
            }
            savedComments.value = savedCommentList
        })

        return savedComments
    }

}