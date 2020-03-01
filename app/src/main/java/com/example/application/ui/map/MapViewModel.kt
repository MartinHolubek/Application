package com.example.application.ui.map

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.example.application.FireStoreRepository
import com.example.application.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat

class MapViewModel : ViewModel() {

    val TAG = "MAP_VIEW_MODEL"
    var fireStoreRepository = FireStoreRepository()


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

    fun startGPS(){

        /*map.addDoneLoadingListener(Runnable {
            //zatvorí menu s miestami
            //clearPlaceList?.setVisibility(View.GONE)
            //myFlag = false
            //vytvoreny lokator, ktorý zobrazuje na mape polohu zariadenia
            var myLocation = map!!.locationDisplay as LocationDisplay
            if (myLocation.isStarted){
                myLocation.stop()
            }else{
                myLocation.autoPanMode = LocationDisplay.AutoPanMode.COMPASS_NAVIGATION
                myLocation.startAsync()
                myLocation.initialZoomScale = 2000.0
                println( " old: " + mapView!!.locationDisplay.initialZoomScale.toString())
                println("new: " + myLocation.initialZoomScale)
            }
        })*/
    }

    fun changeText(s: String){
        _text.value = s
    }

    @SuppressLint("SimpleDateFormat")
    fun addPoint(point : HashMap<String,Any?>, ba1: ByteArray?, ba2: ByteArray?) : String {
        val storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser


        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = storage.getReference("Pictures/" + mFirebaseUser!!.uid)
        val pictureRef = reference.child("pictures_$timeStamp")
        var uploadTask = pictureRef.putBytes(ba1!!)


        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.

        }
        //point.put("pict",pictureRef.path)
        point.put("pict",pictureRef.path)

        //Prvy sposob ukladania
        /*db.collection("users")
            .document(mFirebaseUser!!.uid)
            .collection("places")
            .add(point).addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }*/

        return   pictureRef.path
    }

    @SuppressLint("SimpleDateFormat")
    fun addPoint2(point : Place, ba1: ByteArray?, ba2: ByteArray?) : String {
        val storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser


        var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val reference = storage.getReference("Pictures/" + mFirebaseUser!!.uid)
        var pictureRef = reference.child("pictureBefore_$timeStamp")
        var uploadTask = pictureRef.putBytes(ba1!!)


        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.

        }
        //point.put("pict",pictureRef.path)
        point.photoBefore = pictureRef.path

        timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        pictureRef = reference.child("pictureAfter_$timeStamp")
        uploadTask =pictureRef.putBytes(ba2!!)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.

        }
        point.photoAfter = pictureRef.path

        point.pointID = mFirebaseUser!!.uid + timeStamp
        point.userName = mFirebaseUser!!.displayName

        //Druhy sposob ukladania
        fireStoreRepository.savePlaceItem(point).addOnFailureListener{
            Log.e(TAG,"Chyba pri ukladani miesta")
        }

        return   pictureRef.path
    }

    /**
     * vráti hodnotu prveho zaznamu v kolekcii
     */
    fun getURL(){
        val storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser
        var path : String? = null

        db.collection("users")
            .document(mFirebaseUser!!.uid)
            .collection("places")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    var storageRef = storage.reference
                    _after_photo_path.value = document.data.get("pict") as String

                    break
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }
}

