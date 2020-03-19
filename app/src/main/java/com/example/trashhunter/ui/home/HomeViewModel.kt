package com.example.trashhunter.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.trashhunter.firebase.FirebaseRepository
import com.example.trashhunter.Friend
import com.example.trashhunter.Place
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot


class HomeViewModel : ViewModel() {

    val TAG = "HOME_VIEW_MODEL"
    var firebaseRepository =
        FirebaseRepository()
    var savedFriends: MutableLiveData<List<String>> = MutableLiveData()
    var savedPlaces : MutableLiveData<List<Place>> = MutableLiveData()

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    var text: LiveData<String> = _text


    /**
     * Metóda, ktorá naplní z databázi pole miest
     * @return vráti pole s miestami
     */
    fun getPlaces(list:List<String>) : LiveData<List<Place>> {
        firebaseRepository.getPlaces(list).addSnapshotListener(EventListener<QuerySnapshot>{value,e ->
            if (e != null){
                Log.w(TAG,"LISTEN FAILED", e)
                savedPlaces.value = null
                return@EventListener
            }

            var savedPlacesList : MutableList<Place> = mutableListOf()
            for (doc in value!!){
                var placeItem = doc.toObject(Place::class.java)
                savedPlacesList.add(placeItem)
            }

            savedPlaces.value = savedPlacesList
        })
        return savedPlaces
    }
    fun getFriends() : LiveData<List<Place>>{
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