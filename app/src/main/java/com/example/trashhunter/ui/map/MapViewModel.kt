package com.example.trashhunter.ui.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.example.trashhunter.*
import com.example.trashhunter.firebase.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class MapViewModel : ViewModel() {

    val TAG = "MAP_VIEW_MODEL"
    var fireStoreRepository =
        FirebaseRepository()

    var mFirebaseStorage = com.example.trashhunter.firebase.FirebaseStorage()
    var savedUsersPlaces: MutableLiveData<List<Place>> = MutableLiveData()
    /*var savedUsersPlaces: MutableLiveData<List<Place>> = MutableLiveData<List<Place>>().apply {

        thread {
            fireStoreRepository.getPlaces()
                .addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Chyba pri načitaní priatelov")

                        return@EventListener
                    }

                    var savedPlacesList: MutableList<Place> = mutableListOf()
                    for (doc in value!!) {
                        var place = doc.toObject(Place::class.java)
                        savedPlacesList.add(place)
                    }
                    this.postValue(savedPlacesList)

                })
        }

    }*/
    var savedPlaces : LiveData<List<Place>> = savedUsersPlaces
    var savedFriends: MutableLiveData<List<String>> = MutableLiveData()

    fun getFriendsPlace() : LiveData<List<Place>>{
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
                savedUsersPlaces.value = listOf<Place>()
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

                    savedUsersPlaces.value = savedPlacesList
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
    val before_photo_path : LiveData<String> = _before_photo_path

    private var _after_photo_path = MutableLiveData<String>().apply {
        value = null
    }
    val after_photo_path : LiveData<String> = _after_photo_path

    var myFlag: Boolean = false

    //private var map: ArcGISMap? = null

    private var _map = MutableLiveData<ArcGISMap>().apply {
        value = ArcGISMap(Basemap.Type.STREETS_VECTOR, 49.201476197, 18.870735168, 11)

    }
    val map: LiveData<ArcGISMap> = _map

    fun changeText(s: String){
        _text.value = s
    }

    @SuppressLint("SimpleDateFormat")
    fun addPoint2(point : Place, ba1: ByteArray?, ba2: ByteArray?) : String {
        val storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser
        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        point.creatorID = mFirebaseUser!!.uid
        point.pointID = mFirebaseUser!!.uid + timeStamp
        point.userName = mFirebaseUser!!.displayName
        /*var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = storage.getReference("Pictures/" + mFirebaseUser!!.uid)
        var pictureRef = reference.child("pictureBefore_$timeStamp")
        var uploadTask = pictureRef.putBytes(ba1!!)

        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.

        }*/
        mFirebaseStorage.saveImagePlace(ba1,timeStamp, false).addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener { taskSnapshot ->
            point.photoBefore = taskSnapshot.metadata?.path
            point.cleared = false

            if( ba2 == null){
                fireStoreRepository.savePlaceItem(point).addOnFailureListener{
                    Log.e(TAG,"Chyba pri ukladani miesta")
                }
            }else{
                mFirebaseStorage.saveImagePlace(ba2, timeStamp, true).addOnFailureListener {
                    // Handle unsuccessful uploads

                }.addOnSuccessListener {taskSnapshot ->
                    point.photoAfter = taskSnapshot.metadata?.path
                    point.cleared = true

                    //Druhy sposob ukladania
                    fireStoreRepository.savePlaceItem(point).addOnFailureListener{
                        Log.e(TAG,"Chyba pri ukladani miesta")
                    }
                }
            }
        }

        //point.put("pict",pictureRef.path)
        //point.photoBefore = pictureRef.path

        /*timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        pictureRef = reference.child("pictureAfter_$timeStamp")
        uploadTask =pictureRef.putBytes(ba2!!)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.

        }*/
        //point.photoAfter = pictureRef.path

        /*point.creatorID = mFirebaseUser!!.uid
        point.pointID = mFirebaseUser!!.uid + timeStamp
        point.userName = mFirebaseUser!!.displayName

        //Druhy sposob ukladania
        fireStoreRepository.savePlaceItem(point).addOnFailureListener{
            Log.e(TAG,"Chyba pri ukladani miesta")
        }*/

        return "OK"
    }
}

