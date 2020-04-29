package com.holubek.trashhunter

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

/**
 * Trieda na obrazenie obrázku na celú obrazovku
 */
class FullScreenImageActivity : AppCompatActivity() {

    /**
     * Získa url adresu a nastaví obrázok na celú obrazovku
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        var imageView = findViewById<ImageView>(R.id.fullScreenImageView)

        var intent = intent
        if (intent != null){
            var imageUri = intent.data
            if (imageUri != null && imageView != null){
                Picasso.get().load(imageUri).into(imageView)
            }
            //nastavenie zobrazenia za celú obrazovku
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)

        }
    }
}
