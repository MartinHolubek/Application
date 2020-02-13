package com.example.application

import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class RetrieveData{
    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null


    fun Retrieve(Array : ArrayList<Place>){

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
                Array!!.apply { Array.add(Place(2, "Pato", Calendar.getInstance().time, "Slovakia", "vycistene", "%"))}


            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
}