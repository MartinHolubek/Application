package com.example.application

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.util.*

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

    fun getPlaceItem(placeID:String): DocumentReference {
        var user = placeID.dropLast(15)
        var documentReference = firestoreDB.collection("users")
            .document(user)
            .collection("places")
            .document(placeID)

        return documentReference
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

        /*var collectionReference = firestoreDB.collection("users")
            .whereEqualTo("displayName", name.toString())*/

        var collectionReference = firestoreDB.collection("users")
            .orderBy("displayName").startAt(name).endAt(name+"\uf8ff")

        return collectionReference

    }

    fun saveEventItem(event: Event): Task<Void> {
        var documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("events")
            .document()
        return documentReference.set(event)
    }

    fun getEventItems(): CollectionReference {
        var collectionReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("events")

        return collectionReference
    }

    fun saveRatingByUser(placeID:String, oldCount: Int,oldRating:Float,newRating:Float,comment:String): Task<Void>{
        var userID = placeID.dropLast(15)
        var documentReference = firestoreDB.collection("users")
            .document(userID)
            .collection("places")
            .document(placeID)
        var newCount = oldCount + 1
        var actualRating = (oldRating + newRating)/2

        var ratingReference = firestoreDB.collection("users")
            .document(userID)
            .collection("places")
            .document(placeID)
            .collection("rating")
            .document()
        return documentReference.also{
            var uid =user!!.uid
            var dn = user!!.displayName
            var commentToSave = Comment(uid, dn, newRating, comment, Calendar.getInstance().time)
            ratingReference.set(commentToSave)
        }.update("countOfRating", newCount,"rating", actualRating)
    }

    fun getComments(placeID:String): CollectionReference{
        var userID = placeID.dropLast(15)
        var collectionReference = firestoreDB.collection("users")
            .document(userID)
            .collection("places")
            .document(placeID)
            .collection("rating")

        return collectionReference
    }

    fun getUser(){

    }






}