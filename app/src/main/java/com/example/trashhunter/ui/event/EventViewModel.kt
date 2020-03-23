package com.example.trashhunter.ui.event

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trashhunter.Comment
import com.example.trashhunter.Event
import com.example.trashhunter.firebase.FirebaseRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class EventViewModel : ViewModel() {
    var firebaseRepository = FirebaseRepository()

    var savedEvent : MutableLiveData<Event> = MutableLiveData()
    var savedComments : MutableLiveData<List<Comment>> = MutableLiveData()

    fun getPlace(eventID:String,userID:String): LiveData<Event> {

        var event:Event
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

    fun saveRating(eventID:String,userID:String, oldCount: Int,oldRating:Float,newRating:Float,comment:String){
        firebaseRepository.saveEventRatingByUser(eventID,userID, oldCount, oldRating, newRating,comment).addOnFailureListener {
            Log.e("eventViewModel", "Chyba pri ukladaní hodnotenia")
        }
    }

    fun getComments(placeID: String, userID : String):LiveData<List<Comment>>{

        firebaseRepository.getEventComments(placeID,userID).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
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