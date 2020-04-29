package com.holubek.trashhunter

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

/**
 * Trieda na prihlásenie používateľa do systému
 */
class LoginActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null

    /**
     * Inicializácia Firebase Authentication a listenenrov pre tlačídla
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mFirebaseAuth = FirebaseAuth.getInstance()

        var buttonLogin = findViewById<Button>(R.id.LoginButton)
        buttonLogin.setOnClickListener {
            if( !emailUserLogin.text.toString().equals("") &&
                !passwordUserLogin.text.toString().equals("")){
                signIn()
            }else{
                Toast.makeText(
                    this, "Vložte údaje na prihlásenie",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        var buttonCreateUser = findViewById<Button>(R.id.LoginCreateActivityButton)
        buttonCreateUser.setOnClickListener {
            createUser()
        }
    }

    /**
     * Spustí aktivitu na registráciu užívateľa
     */
    private fun createUser(){
        val startIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(startIntent)
    }

    /**
     * Metóda prihlási používateľa do systému
     */
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

    /**
     * Spustí hlavnú aktivitu aplikácie
     */
    private fun authenticationSuccess(){
        Toast.makeText(
            this, "Authentication success.",
            Toast.LENGTH_SHORT
        ).show()
        val startIntent = Intent(this, MainActivity::class.java)

        startActivity(startIntent)
    }

    /**
     *
     */
    private fun authenticationFailed(){
        Toast.makeText(
            this, "Prihlásenie bolo neúspešné.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
