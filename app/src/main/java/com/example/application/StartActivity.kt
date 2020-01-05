package com.example.application


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View


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
}
