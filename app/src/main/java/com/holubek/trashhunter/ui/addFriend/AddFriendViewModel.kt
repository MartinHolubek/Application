package com.holubek.trashhunter.ui.addFriend

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.Friend
import com.holubek.trashhunter.firebase.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

/**
 * View Model pre fragment na pridanie priateľov
 */
class AddFriendViewModel : ViewModel() {

    val TAG = "FIRESTORE_VIEW_MODEL"
    var firebaseRepository = FirebaseRepository()
    var firebaseStorage = FirebaseStorage()

    var potentionalFriends: MutableLiveData<List<Friend>> = MutableLiveData()
    var savedFriends: MutableLiveData<List<Friend>> = MutableLiveData()

    /**
     * Uloženie užívateľa do zoznamu priateľov
     * @param friend objekt pridávaného priateľa
     */
    fun saveFriendsToFirebase(friend: Friend) {
        firebaseRepository.saveFriendItem(friend).addOnFailureListener {
            Log.e(TAG, "Chyba pri ukladaní priatela")
        }
    }

    /**
     * Zmazanie priateľa zo zoznamu priateľov
     * @param friend objekt pridávaného priateľa
     */
    fun deleteFriendItem(friend: Friend){
        firebaseRepository.deleteFriendItem(friend).addOnFailureListener{
            Log.e(TAG,"Failed to delete Address")
        }
    }

    /**
     * Získa zoznam priateľov z databázy
     * @return Zoznam priateľov
     */
    fun getFriends() : LiveData<List<Friend>>{
        firebaseRepository.getFriendItems().addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
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

    /**
     * Získa zoznam priateľov z databázy podľa mena užívateľa
     * @param name retazec na vyhľadanie užívateľov
     * @return Zoznam priateľov
     */
    fun getPotentionalFriends(name: String) : LiveData<List<Friend>>{
        firebaseRepository.getCollectionCanBeFriends(name).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
            if (e != null) {
                Log.w(TAG, "Chyba pri načitaní priatelov")
                potentionalFriends.value = null
                return@EventListener
            }

            var savedFriendList : MutableList<Friend> = mutableListOf()

            for (doc in value!!) {
                if (FirebaseAuth.getInstance().currentUser!!.uid != doc["uid"]){
                    var friend = doc.toObject(Friend::class.java)
                    friend.uid = doc.id
                    savedFriendList.add(friend)
                }
            }
            potentionalFriends.value = savedFriendList
        })
        return potentionalFriends
    }
}