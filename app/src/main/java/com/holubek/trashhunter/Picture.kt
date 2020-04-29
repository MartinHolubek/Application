package com.holubek.trashhunter

import android.graphics.Bitmap

/**
 * Trieda uchováva informácie a obrázku
 */
class Picture() {

    var image: Bitmap?=null
    var title:String?=null

    constructor(image: Bitmap?, title: String?) : this() {
        this.image = image
        this.title = title
    }
}