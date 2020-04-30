package com.holubek.trashhunter.ui.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.holubek.trashhunter.*
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.holubek.trashhunter.R
import java.text.SimpleDateFormat

/**
 * View Model fragmentu na zobrazovanie miest od užívateľov na mape a na vytvorenie príspevku
 */
class MapViewModel : ViewModel() {

    val TAG = "MAP_VIEW_MODEL"
    var fireStoreRepository =
        FirebaseRepository()

    var mFirebaseStorage = com.holubek.trashhunter.firebase.FirebaseStorage()
    var savedFriendsPlaces: MutableLiveData<List<Place>> = MutableLiveData()

    var savedPlaces : LiveData<List<Place>> = savedFriendsPlaces
    var savedFriends: MutableLiveData<List<String>> = MutableLiveData()

    /**
     * Vráti zoznam miest od priateľov
     */
    fun getPlacesOfFriends() : LiveData<List<Place>>{
        fireStoreRepository.getFriendItems().addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
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
                savedFriendsPlaces.value = listOf<Place>()
            }else{
                fireStoreRepository.getPlaces(savedFriendList).addSnapshotListener(EventListener<QuerySnapshot>{ value, e ->
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
                    savedFriendsPlaces.value = savedPlacesList
                })
            }
            savedFriends.value = savedFriendList
        })
        return savedPlaces
    }

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null
    private var _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text
    private var _before_photo_path = MutableLiveData<String>().apply {
        value = null
    }

    private var _map = MutableLiveData<ArcGISMap>().apply {
        value = ArcGISMap(Basemap.Type.STREETS_VECTOR, 49.201476197, 18.870735168, 11)

    }
    val map: LiveData<ArcGISMap> = _map

    /**
     * Uloží miesto do databázy
     * @param place miesto, ktoré sa ma uložiť
     * @param ba1 bajtove pole reprezentujúce fotku pre vyčistením
     * @param ba2 bajtove pole reprezentujúce fotku po vyčistením
     */
    @SuppressLint("SimpleDateFormat")
    fun savePlace(place : Place, ba1: ByteArray?, ba2: ByteArray?) : Boolean {
        val storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser
        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        place.creatorID = mFirebaseUser!!.uid
        place.pointID = mFirebaseUser!!.uid + timeStamp
        place.userName = mFirebaseUser!!.displayName

        mFirebaseStorage.saveImagePlace(ba1,timeStamp, false).addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener { taskSnapshot ->
            place.photoBefore = taskSnapshot.metadata?.path
            place.cleared = false

            if( ba2 == null){
                fireStoreRepository.savePlaceItem(place).addOnFailureListener{
                    Log.e(TAG,"Chyba pri ukladani miesta")
                }
            }else{
                mFirebaseStorage.saveImagePlace(ba2, timeStamp, true)
                    .addOnSuccessListener {taskSnapshot ->
                    place.photoAfter = taskSnapshot.metadata?.path
                    place.cleared = true

                    //Druhy sposob ukladania
                    fireStoreRepository.savePlaceItem(place).addOnFailureListener{
                        Log.e(TAG,"Chyba pri ukladani miesta")
                    }
                }
            }
        }
        return true
    }
}

