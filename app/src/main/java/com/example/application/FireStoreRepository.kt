package com.example.application

import android.os.Handler
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

    /**
     * Uložiť priatela
     */
    fun saveFriendItem(friend: Friend): Task<Void>{
        var documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("friends")
            .document(friend.uid!!)
        return documentReference.set(friend)
    }

    fun getFriendsItem(): CollectionReference {
        var collectionReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("friends")

        return collectionReference
    }

    fun deleteFriendItem(friend: Friend): Task<Void>{
        var documentReference = firestoreDB.collection("users/${user!!.uid}/friends").document(friend.uid!!)

        return documentReference.delete()
    }

    fun getCollectionCanBeFriends(name :String): Query {
        var friends : MutableList<String> = mutableListOf()

        firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("friends").get().addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot) {
                    val friend = doc.toObject(Friend::class.java)
                    friends.add(friend.uid!!)

                }
            }

        var collectionReference = firestoreDB.collection("users")
            .whereEqualTo("displayName", name.toString())

        return collectionReference

    }



}