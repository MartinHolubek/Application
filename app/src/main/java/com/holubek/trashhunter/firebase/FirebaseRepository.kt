package com.holubek.trashhunter.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.holubek.trashhunter.*
import java.util.*

/**
 * Trieda na ukladanie a načítavanie dáť z databázy firestore
 */
class FirebaseRepository{

    val TAG = "FIREBASE_REPOSITORY"
    var firestoreDB = FirebaseFirestore.getInstance()
    var user = FirebaseAuth.getInstance().currentUser

    /**
     * Získanie informácií o uživateľovi     *
     */
    fun getUserInfo(): DocumentReference{
        var documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
        return documentReference
    }

    /**
     * Získanie uživateľov, ktorý sa zúčastnia udalosti
     * @param organizerID jedinečný identifikátor organizátora
     * @param eventID
     */
    fun getJoinedUsers(organizerID: String, eventID: String): CollectionReference{
        var collectionReference = firestoreDB.collection("users")
            .document(organizerID)
            .collection("events")
            .document(eventID)
            .collection("participant")

        return collectionReference
    }

    /**
     * Uložiť objekt miesta do databázy
     * @param place objekt miesta s uloženými údajmi
     */
    fun savePlaceItem(place: Place): Task<Void>{
        var documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("places")
            .document(place.pointID.toString())
        return documentReference.set(place)
    }

    /**
     * Získanie všetkých miest, ktoré prihlasený uživateľ vyčistil, alebo označil
     */
    fun getPlaceItems(): CollectionReference {
        var collectionReference = firestoreDB.collection("users/${user!!.uid}/places")

        return collectionReference
    }

    /**
     * Získanie miesta podla placeID
     * @param placeID jedinečný identifikátor miesta uloženého v databáze
     */
    fun getPlaceItem(placeID:String): DocumentReference {
        var user = placeID.dropLast(15)
        var documentReference = firestoreDB.collection("users")
            .document(user)
            .collection("places")
            .document(placeID)

        return documentReference
    }

    /**
     * Zmazanie dokumentu miesta v databáze
     * @param place objekt miesta, ktorý sa zmaže z databázy
     * @return objekt,ktorý informuje o stave zmazania
     */
    fun deletePlaceItem(place: Place): Task<Void>{
        var documentReference = firestoreDB.collection("users/${user!!.uid}/places").document(place.pointID.toString())

        return documentReference.delete()
    }

    /**
     * Zmazanie dokumentu udalosti v databáze
     * @param event objekt udalosti, ktorý sa zmaže z databázy
     * @return objekt,ktorý informuje o stave zmazania
     */
    fun deleteEventItem(event: Event): Task<Void>{
        var documentReference = firestoreDB.collection("users/${user!!.uid}/events").document(event.id.toString())

        return documentReference.delete()
    }

    /**
     * Uložiť priatela do databázy
     * @param friend objekt priateľa, ktorého uložíme do databázy
     * @return objekt,ktorý informuje o stave uloženia
     */
    fun saveFriendItem(friend: Friend): Task<Void>{
        var documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("friends")
            .document(friend.uid!!)
        return documentReference.set(friend)
    }

    /**
     * Získanie kolekcie priatelov prihláseného uživateľa
     * @return kolekcia priateľov
     */
    fun getFriendItems(): CollectionReference {
        var collectionReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("friends")

        return collectionReference
    }

    /**
     * Zmazanie dokumentu priateľa z kolekcie prihláseného užívateľa
     * @param friend objekt mazaného dokumentu
     */
    fun deleteFriendItem(friend: Friend): Task<Void>{
        var documentReference = firestoreDB.collection("users/${user!!.uid}/friends").document(friend.uid!!)

        return documentReference.delete()
    }

    /**
     *Ziskanie kolekcie užívateľov ktorých meno začína na text z parametra
     * @param name hodnota podľa ktoréj hľadáme priateľov
     */
    fun getCollectionCanBeFriends(name :String): Query {
        var collectionReference = firestoreDB.collection("users")
            .orderBy("displayName").startAt(name).endAt(name+"\uf8ff")

        return collectionReference
    }

    /**
     * Uloženie udalosti do databázy
     * @param event objekt, ktorý sa uloží do databázy
     */
    fun saveEventItem(event: Event): Task<Void> {
        val documentReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("events")
            .document()
        event.id = documentReference.id
        return documentReference.set(event)
    }

    /**
     * Získanie udalosti ktoré vytvoril prihlásený užívateľ
     * @return kolekcia udalosti
     */
    fun getMyEventItems(): CollectionReference {
        var collectionReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("events")

        return collectionReference
    }

    /**
     *Získanie zoznamu udalosti na ktorých sa zúčastní prihlasený užívateľ
     * @return kolekcia udalosti
     */
    fun getAttendEvents():CollectionReference{
        var collectionReference = firestoreDB.collection("users")
            .document(user!!.uid)
            .collection("attendEvents")

        return collectionReference
    }

    /**
     * Uloží nový dokument hodnotenia a aktualizuje celkové hodnotenie miesta
     * @param ID jedinečný identifikátor miesta
     * @param oldCount počet hodnotení miesta
     * @param oldRating starý súčet hodnotení miesta
     * @param newRating nová hodnota hodnotenia miesta
     * @param comment objekt komentár k miestu
     */
    fun saveRatingByUser(ID:String, oldCount: Int,oldRating:Float,newRating:Float,comment:String): Task<Void>{
        var userID = ID.dropLast(15)
        var documentReference = firestoreDB.collection("users")
            .document(userID)
            .collection("places")
            .document(ID)
        var newCount = oldCount + 1
        var actualRating = oldRating + newRating

        var ratingReference = firestoreDB.collection("users")
            .document(userID)
            .collection("places")
            .document(ID)
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

    /**
     * Uloží nový dokument hodnotenia a aktualizuje celkové hodnotenie udalosti
     * @param eventID jedinečný identifikátor udalosti
     * @param userID jedinečný identifikátor užívateľa
     * @param oldCount počet hodnotení udalosti
     * @param oldRating starý súčet hodnotení udalosti
     * @param newRating nová hodnota hodnotena udalosti
     * @param comment objekt komentár k udalosti
     */
    fun saveEventRatingByUser(eventID:String, userID:String,
                              oldCount: Int, oldRating:Float, newRating:Float, comment:String): Task<Void>{
        var documentReference = firestoreDB.collection("users")
            .document(userID)
            .collection("events")
            .document(eventID)

        var ratingReference = firestoreDB.collection("users")
            .document(userID)
            .collection("events")
            .document(eventID)
            .collection("rating")
            .document()

        var newCount = oldCount + 1
        var actualRating = (oldRating + newRating)/2

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

    /**
     * Uloží referenciu fotky po vyčistení a informácie o užívateľovi,
     * ktorý miesto vyčistil ku danému miestu
     * @param placeID jedinečný identifikátor miesta
     * @param path referencia na fotku v uložisku firebase
     */
    fun updatePlace(placeID:String, path: String): Task<Void>{
        var userID = placeID.dropLast(15)
        return firestoreDB.collection("users")
            .document(userID)
            .collection("places")
            .document(placeID).update(
                "photoAfter",path,
                "cleared", true,
                "clearedBy", user!!.displayName,
                "clearedByID",user!!.uid
            )
    }

    /**
     * Získanie komentárov k miestu
     * @param placeID jedinečný identifikátor miesta
     */
    fun getPlaceComments(placeID:String): CollectionReference{
        var userID = placeID.dropLast(15)
        var collectionReference = firestoreDB.collection("users")
            .document(userID)
            .collection("places")
            .document(placeID)
            .collection("rating")

        return collectionReference
    }

    /**
     * Získanie komentárov k udalosti
     * @param eventID jedinečný identifikátor udalosti
     * @param userID jedinečný identifikátor užívateľa
     */
    fun getEventComments(eventID:String, userID: String): CollectionReference{

        var collectionReference = firestoreDB.collection("users")
            .document(userID)
            .collection("events")
            .document(eventID)
            .collection("rating")

        return collectionReference
    }

    /**
     * Získanie kolekcie miest ktoré vytvorili konkrétny užívateľia
     * @param listUsers zoznam jedinečných identifikátorov užívateľov
     */
    fun getPlaces(listUsers:List<String>): Query {
        return firestoreDB.collectionGroup("places").whereIn("creatorID",listUsers).orderBy("date")
    }

    /**
     * Uloženie dokumentu udalosti na ktorej sa užívateľ zúčastní
     * @param event obejkt udalosti
     */
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

    /**
     * Uloží referenciu na prihláseného užívateľa do kolekcie učastníkov v udalosti
     * @param event objekt udalosti
     */
    fun saveJoinedUser(event: Event): Task<Void>{
        var documentReference = firestoreDB.collection("users")
            .document(event.organizerID.toString())
            .collection("events")
            .document(event.id.toString())
            .collection("participant")
            .document(user!!.uid)
        return documentReference.set(hashMapOf(
            "user" to firestoreDB.collection("users").document(user!!.uid)
        ))
    }

    /**
     * Zmazanie referencie na prihláseného užívateľa z kolekcie učastníkov v udalosti
     *@param event objekt udalosti
     */
    fun deleteJoinedUser(event: Event): Task<Void>{
        var documentReference = firestoreDB.collection("users")
            .document(event.organizerID.toString())
            .collection("events")
            .document(event.id.toString())
            .collection("participant")
            .document(user!!.uid)
        return documentReference.delete()
    }

    /**
     * Získanie udalostí, ktoér vytvorili konkrétny užívatelia
     * @param savedFriendList zoznam jedinečných identifikátorov užívateľov
     */
    fun getEvents(savedFriendList: MutableList<String>): Query {
        return firestoreDB.collectionGroup("events").whereIn("organizerID",savedFriendList)
            .whereGreaterThan("endDate",Calendar.getInstance().time)
    }

    /**
     * Zmazanie udalosti z kolekcie udalosti na ktorých sa zúčastní prihlásený užívateľ
     * @param event objekt udalosti
     */
    fun deleteAttendEvent(event: Event):Task<Void>{
        var documentReference = firestoreDB.collection("users/${user!!.uid}/attendEvents").document(event.id.toString())

        return documentReference.delete()
    }

    /**
     * Uložiť informácie o novom užívateľovi
     * @param uid jedinečný identifikátor nového užívateľa
     * @param name meno nového užívateľa
     * @param image referencia na profilovú fotku nového užívateľa v úložisku firebase
     */
    fun saveAccountInfo(uid:String,name: String?, image: String):Task<Void> {
        val account = hashMapOf(
            "displayName" to name,
            "uid" to uid,
            "image" to image
        )

        return firestoreDB.collection("users")
            .document(uid)
            .set(account)
    }

    /**
     * Získanie kolekcie užívateľov podľa zoznamu jedinečných identifikátorov užívateľov
     * @param zoznam jedinečných identifikátorov užívateľov
     */
    fun getUsers(uids: ArrayList<String>): Query {
        return firestoreDB.collectionGroup("users").whereIn("uid",uids)
    }

    /**
     * Získanie dokumentu udalosti
     * @param eventID jedinečný identifikátor udalosti
     * @param organizerID jedinečný identifikátor organizátora udalosti
     * @return dokument udalosti
     */
    fun getEventItem(eventID: String, organizerID: String): DocumentReference{
        return firestoreDB.collection("users")
            .document(organizerID)
            .collection("events")
            .document(eventID)
    }


}