package com.holubek.trashhunter


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    fun onClickSingIn(view: View){
        val startIntent = Intent(this, LoginActivity::class.java)
        startActivity(startIntent)
        finish()
    }

    fun onClickSingUp(view: View){
        val startIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(startIntent)
        finish()
    }

    fun getFirebaseUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}
