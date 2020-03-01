package com.example.application

import com.google.firebase.firestore.GeoPoint
import java.util.*
import kotlin.collections.ArrayList

class Event(){
    var organizer :String?=null
    var startDate :Date?=null
    var endDate :Date?=null
    var coordinates : GeoPoint?=null
    var picture : String?=null
    var details : String?=null
    var participants : ArrayList<Friend>?=null

}