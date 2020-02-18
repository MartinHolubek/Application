package com.example.application.ui.home

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.application.Place
import com.example.application.RetrieveData
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class HomeViewModel : ViewModel() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null
    private var RetrieveData: RetrieveData = RetrieveData()
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    var text: LiveData<String> = _text

    private val _list = MutableLiveData<ArrayList<Place>>().apply {
        value = ArrayList<Place>()
        addPoints()

    }
    val list: LiveData<ArrayList<Place>> = _list

    fun addPoints(){
        val storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser
        var path : String? = null
        val storageRef = storage.reference


        db.collection("users")
            .document(mFirebaseUser!!.uid)
            .collection("places")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    android.os.Handler().postDelayed({
                        var list = ArrayList<Place>()
                        /*list!!.add(Place(2, "Pato", Calendar.getInstance().time, "Slovakia", "vycistene", "%"))
                        list!!.add(Place(2, "Pato", Calendar.getInstance().time, "Slovakia", "vycistene", "%"))
                        list!!.add(Place(2, "Pato", Calendar.getInstance().time, "Slovakia", "vycistene", "%"))*/
                        list!!.add(Place(2, "Pato", Calendar.getInstance().time, "Slovakia", "vycistene", document.data["pict"] as String))
                        _list.value = list

                    },3000)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    operator fun <T> MutableLiveData<ArrayList<T>>.plusAssign(values: List<T>) {
        val value = this.value ?: arrayListOf()
        value.addAll(values)
        this.value = value
    }

}