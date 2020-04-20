package com.example.trashhunter

class User {
    var displayName: String?=null
    var uid : String?=null
    var image : String?=null

    constructor(name:String, uid:String, image:String) {
        this.displayName = name
        this.uid = uid
        this.image = image
    }
}