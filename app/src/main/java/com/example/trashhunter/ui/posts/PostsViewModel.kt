package com.example.trashhunter.ui.posts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trashhunter.firebase.FirebaseRepository
import com.example.trashhunter.Friend
import com.example.trashhunter.Place
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot


class PostsViewModel : ViewModel() {

    val TAG = "HOME_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()
    var savedFriends: MutableLiveData<List<String>> = MutableLiveData()
    var savedPlaces : MutableLiveData<List<Place>> = MutableLiveData()

    /**
     * Metóda, ktorá naplní z databázi pole miest
     * @return vráti pole s miestami
     */
    fun getPlaces() : LiveData<List<Place>>{
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
                savedPlaces.value = listOf<Place>()
            }else{
                firebaseRepository.getPlaces(savedFriendList).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
                    if (e != null) {
                        Log.w(TAG, "Chyba pri načitaní priatelov")
                        savedFriends.value = null

                        return@EventListener
                    }
                    var savedPlacesList : MutableList<Place> = mutableListOf()
                    for (doc in value!!){
                        var placeItem = doc.toObject(Place::class.java)
                        savedPlacesList.add(placeItem)
                    }

                    savedPlaces.value = savedPlacesList
                })
            }
            savedFriends.value = savedFriendList
        })
        return savedPlaces

    }


}