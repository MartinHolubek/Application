package com.example.trashhunter.ui.JoinedUsers

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trashhunter.User
import com.example.trashhunter.firebase.FirebaseRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class JoinedUsersViewModel : ViewModel() {
    val TAG = "JOINED_USERS_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()

    var savedUsers: MutableLiveData<List<User>> = MutableLiveData()

    fun getUsers(organizerID: String, eventID:String): LiveData<List<User>>{
        firebaseRepository.getJoinedUsers(organizerID,eventID).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null){
                Log.w(ContentValues.TAG,"LISTEN FAILED", e)
                savedUsers.value = null
                return@EventListener
            }

            var savedUsersList : MutableList<User> = mutableListOf()
            for (doc in value!!){
                var userDoc: DocumentReference =  doc["user"] as DocumentReference
                userDoc.addSnapshotListener(EventListener<DocumentSnapshot>{ value, e ->
                    if (e != null) {
                        Log.w(TAG, "Chyba pri načitaní eventu")

                        return@EventListener
                    }


                    var name = value!!["displayName"].toString()
                    var uid = value!!["uid"].toString()
                    var image = value!!["image"].toString()
                    var user = User(name,uid,image)
                    if (user != null){
                        savedUsersList.add(user)
                    }
                    savedUsers.value = ArrayList(savedUsersList)
                })
            }
        })
        return savedUsers
    }

}
