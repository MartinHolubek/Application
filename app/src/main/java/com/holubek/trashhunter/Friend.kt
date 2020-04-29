package com.holubek.trashhunter

/**
 * Trieda na uchovanie informácií o priateľovi
 */
class Friend() {
    var displayName: String?=null
    var uid : String?=null
    var image: String?=null

    constructor(name:String, uid:String) : this() {
        this.displayName = name
        this.uid = uid
    }

    constructor(name:String, uid:String, image:String):this(name,uid){
        this.image = image
    }

}