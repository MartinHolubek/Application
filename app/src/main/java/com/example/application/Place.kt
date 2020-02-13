package com.example.application

import java.net.URL
import java.util.*

class Place{

    var pointID:Int?=null
    var userName:String?=null
    var date:Date?=null
    var placeName:String?=null
    var ClearText:String?=null
    var photo:String?=null
    constructor(pointID:Int, userName:String, date:Date, placeName:String, clearText:String, photo:String){
        this.pointID=pointID
        this.userName=userName
        this.date=date
        this.placeName=placeName
        this.ClearText=clearText
        this.photo=photo
    }
}