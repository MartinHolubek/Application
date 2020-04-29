package com.holubek.trashhunter

import com.holubek.trashhunter.Friend
import com.google.firebase.firestore.GeoPoint
import java.util.*
import kotlin.collections.ArrayList

/**
 * Trieda na uchovanie informácií o udalosti
 */
class Event(){
    var id:String?=null
    var title :String?=null
    var organizer :String?=null
    var organizerID :String?=null
    var startDate :Date?=null
    var endDate :Date?=null
    var coordinates : GeoPoint?=null
    var placeName: String?=null
    var picture : String?=null
    var details : String?=null
    var participants : ArrayList<Friend>?=null
    var rating:Float?=0F
    var countOfRating:Int?=0

}