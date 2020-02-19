package com.example.application.ui.share

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.application.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ShareViewModel : ViewModel() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null

    private val _text = MutableLiveData<String>().apply {
        value = "This is share Fragment"
    }
    val text: LiveData<String> = _text

    private val _list = MutableLiveData<ArrayList<Friend>>().apply {
        value = ArrayList<Friend>()
        addFriends()

    }
    val list: LiveData<ArrayList<Friend>> = _list


    fun addFriends(){
        val db = FirebaseFirestore.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser

        db.collection("users").get().addOnSuccessListener { result ->
            var list = ArrayList<Friend>()
            android.os.Handler().postDelayed({
                for (document in result) {
                    if (document.data["displayName"]!!.equals(mFirebaseUser!!.displayName.toString())){
                        list!!.add(Friend(document.data["displayName"] as String))
                        _list.value = list
                    }

                }
            },3000)
        }
            .addOnFailureListener { exception ->
                Log.d("error db", "Error getting documents: ", exception)
            }

    }
}