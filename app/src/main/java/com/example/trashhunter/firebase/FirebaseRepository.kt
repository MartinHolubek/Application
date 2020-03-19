package com.example.trashhunter.firebase

import com.example.trashhunter.Comment
import com.example.trashhunter.Event
import com.example.trashhunter.Friend
import com.example.trashhunter.Place
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*

class FirebaseRepository{

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

    fun getPlaceItems(): CollectionReference {
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

    fun getFriendItems(): CollectionReference {
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
        event.organizerID = user!!.uid
        var documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("events")
            .document()
        event.id = documentReference.id
        return documentReference.set(event)
    }

    fun getMyEventItems(): CollectionReference {
        var collectionReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("events")

        return collectionReference
    }

    fun getAllEventItems():Query{
        return firestoreDB.collectionGroup("events")
    }

    fun getAttendEvents():CollectionReference{
        var collectionReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("attendEvents")

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
            var commentToSave = Comment(
                uid,
                dn,
                newRating,
                comment,
                Calendar.getInstance().time
            )
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

    fun getUserPlaces(): CollectionReference{
        var collectionReference = firestoreDB.collection("users")

        return collectionReference
    }

    fun getPlaces(): Query{

        return firestoreDB.collectionGroup("places")
    }

    fun getPlaces(listUsers:List<String>): Query {
        //return firestoreDB.collection("users").whereIn("uid",listUsers)
        return firestoreDB.collectionGroup("places").whereIn("creatorID",listUsers)
    }

    fun saveAttendEvent(event: Event): Task<Void> {
        var documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("attendEvents")
            .document(event.id.toString())
        return documentReference.set(hashMapOf(
            "event" to firestoreDB.collection("users").document(event.organizerID.toString())
                .collection("events")
                .document(event.id.toString())))
    }

    fun getEvents(savedFriendList: MutableList<String>): Query {
        return firestoreDB.collectionGroup("events").whereIn("organizerID",savedFriendList)
    }

    fun deleteAttendEvent(event: Event):Task<Void>{
        var documentReference = firestoreDB.collection("users/${user!!.uid}/attendEvents").document(event.id.toString())

        return documentReference.delete()
    }

    fun saveAccountInfo(name: String?):Task<Void> {
        val account = hashMapOf(
            "displayName" to name,
            "uid" to user!!.uid
        )

        return firestoreDB.collection("users")
            .document(user!!.uid)
            .set(account)
    }


}