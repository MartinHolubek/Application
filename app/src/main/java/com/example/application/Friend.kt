package com.example.application

class Friend() {
    var displayName: String?=null
    var uid : String?=null

    constructor(name:String, uid:String) : this() {
        this.displayName = name
        this.uid = uid
    }

}