package com.example.trashhunter

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.*
import kotlin.collections.ArrayList

class Event(){
    var id:String?=null
    var title :String?=null
    var organizer :String?=null
    var organizerID :String?=null
    var startDate :Timestamp?=null
    var endDate :Date?=null
    var coordinates : GeoPoint?=null
    var picture : String?=null
    var details : String?=null
    var participants : ArrayList<Friend>?=null

}