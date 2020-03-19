package com.example.trashhunter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mFirebaseAuth = FirebaseAuth.getInstance()

        var buttonLogin = findViewById<Button>(R.id.LoginButton)
        buttonLogin.setOnClickListener {
            signIn()
        }
        var buttonCreateUser = findViewById<Button>(R.id.LoginCreateActivityButton)
        buttonCreateUser.setOnClickListener {
            createUser()
        }
    }

    private fun createUser(){
        val startIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(startIntent)
    }

    private fun signIn(){
        val email : String = emailUserLogin.text.toString()
        val pass : String = passwordUserLogin.text.toString()
        mFirebaseAuth?.signInWithEmailAndPassword(email, pass)
            ?.addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                        Log.d(FragmentActivity.ACCOUNT_SERVICE, "signInWithEmail:success")
                        authenticationSuccess()
                    } else { // If sign in fails, display a message to the user.
                        Log.w(
                            FragmentActivity.ACCOUNT_SERVICE,
                            "signInWithEmail:failure",
                            task.exception
                        )
                        authenticationFailed()
                    }
                })
    }

    private fun authenticationSuccess(){
        Toast.makeText(
            this, "Authentication success.",
            Toast.LENGTH_SHORT
        ).show()
        val startIntent = Intent(this, MainActivity::class.java)

        startActivity(startIntent)
    }

    private fun authenticationFailed(){
        Toast.makeText(
            this, "Authentication failed.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
