package com.example.application

import android.graphics.Bitmap

class Picture() {

    var image: Bitmap?=null
    var title:String?=null

    constructor(image: Bitmap?, title: String?) : this() {
        this.image = image
        this.title = title
    }
}