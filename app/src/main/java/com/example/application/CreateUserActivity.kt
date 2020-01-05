package com.example.application

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_create_user.*

class CreateUserActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        mFirebaseAuth = FirebaseAuth.getInstance()
    }

    fun SingUpOnClicked(view : View){
        createAccount()

    }

    fun createAccount(){
        mFirebaseAuth?.createUserWithEmailAndPassword(sing_up_email.text.toString(), singUpPass.text.toString())
            ?.addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                        Log.d(FragmentActivity.ACCOUNT_SERVICE, "createUserWithEmail:success")
                        mFirebaseUser = mFirebaseAuth?.currentUser
                        Toast.makeText(
                            this, "Authentication success.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val startIntent = Intent(this, MainActivity::class.java)
                        startActivity(startIntent)
                        updateUI()
                    } else { // If sign in fails, display a message to the user.
                        Log.w(
                            FragmentActivity.ACCOUNT_SERVICE,
                            "createUserWithEmail:failure",
                            task.exception
                        )
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI()
                    }

                })
    }

    fun updateUI(){

    }
}
