package com.example.trashhunter

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.model.value.ReferenceValue
import java.util.*

class Place(){
    var creatorID:String?=null
    var pointID:String?=null
    var userName:String?=null
    var date:Date?=null
    var placeName:String?=null
    var ClearText:String?=null
    var photoBefore:String?=null
    var photoAfter:String?=null
    var coordinates:GeoPoint?=null
    var rating:Float?=null
    var countOfRating:Int?=null
    var cleared:Boolean?=null
    var clearedBy:String?= null
    constructor(pointID:String, userName:String, date:Date, placeName:String, clearText:String,
                photoBefore:String,photoAfter:String, coords:GeoPoint) : this() {
        this.pointID=pointID
        this.userName=userName
        this.date=date
        this.placeName=placeName
        this.ClearText=clearText
        this.photoBefore=photoBefore
        this.photoAfter=photoAfter
        this.coordinates=coords
    }
}