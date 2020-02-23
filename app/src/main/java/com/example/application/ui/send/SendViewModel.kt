package com.example.application.ui.send

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.application.FireStoreRepository
import com.example.application.Friend
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class SendViewModel : ViewModel() {

    val TAG = "FIRESTORE_VIEW_MODEL"
    var firebaseRepository = FireStoreRepository()

    var potentionalFriends: MutableLiveData<List<Friend>> = MutableLiveData()
    var savedFriends: MutableLiveData<List<Friend>> = MutableLiveData()


    fun saveFriendsToFirebase(friend: Friend) {
        firebaseRepository.saveFriendItem(friend).addOnFailureListener {
            Log.e(TAG, "Chyba pri ukladaní priatela")
        }
    }

    fun deleteFriendItem(friend: Friend){
        firebaseRepository.deleteFriendItem(friend).addOnFailureListener{
            Log.e(TAG,"Failed to delete Address")
        }
    }

    fun getFriends() : LiveData<List<Friend>>{
        firebaseRepository.getFriendsItem().addSnapshotListener(EventListener<QuerySnapshot>{value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní priatelov")
                savedFriends.value = null

                return@EventListener
            }

            var savedFriendList : MutableList<Friend> = mutableListOf()
            for (doc in value!!) {
                var friend = doc.toObject(Friend::class.java)
                friend.uid = doc.id
                savedFriendList.add(friend)
            }
            savedFriends.value = savedFriendList
        })
        return savedFriends
    }

    fun getPotentionalFriends(name: String) : LiveData<List<Friend>>{
        firebaseRepository.getCollectionCanBeFriends(name).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní priatelov")
                potentionalFriends.value = null

                return@EventListener
            }

            var savedFriendList : MutableList<Friend> = mutableListOf()
            for (doc in value!!) {
                var friend = doc.toObject(Friend::class.java)
                friend.uid = doc.id
                savedFriendList.add(friend)
            }
            potentionalFriends.value = savedFriendList
        })
        return potentionalFriends
    }
}