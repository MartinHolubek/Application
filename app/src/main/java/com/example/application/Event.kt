package com.example.application

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.sql.Time
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class Event(){
    var title :String?=null
    var organizer :String?=null
    var startDate :Timestamp?=null
    var endDate :Date?=null
    var startTime :Time?=null
    var endTime :Time?=null
    var coordinates : GeoPoint?=null
    var picture : String?=null
    var details : String?=null
    var participants : ArrayList<Friend>?=null

}