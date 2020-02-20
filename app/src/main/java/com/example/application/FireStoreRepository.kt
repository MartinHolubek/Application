package com.example.application

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class FireStoreRepository{

    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser



    //ulozit miesto do firebase
    fun savePlaceItem(place: Place): Task<Void>{
        var documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("places")
            .document(place.pointID.toString())
        return documentReference.set(place)
    }

    fun getPlaceItem(): CollectionReference {
        var collectionReference = firestoreDB.collection("users/${user!!.uid}/places")

        return collectionReference
    }


    fun deletePlaceItem(place: Place): Task<Void>{
        var documentReference = firestoreDB.collection("users/${user!!.uid}/places").document(place.pointID.toString())

        return documentReference.delete()
    }

}