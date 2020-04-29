package com.holubek.trashhunter

/**
 * Trieda uchováva informácie o užívateľovi
 */
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