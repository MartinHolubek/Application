package com.holubek.trashhunter

import java.util.*

/**
 * Trieda , ktorá uchováva informácie o komentári
 */
class Comment() {
    var userID:String?=null
    var userName:String?=null
    var rating:Float?=null
    var comment:String?=null
    var date:Date?=null

    constructor(
        userID: String?,
        userName: String?,
        rating: Float?,
        comment: String?,
        date: Date?
    ) : this() {
        this.userID = userID
        this.userName = userName
        this.rating = rating
        this.comment = comment
        this.date = date
    }
}