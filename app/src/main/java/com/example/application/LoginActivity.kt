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
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun LoginButtonClicked(view : View){

     }

    fun loginCreateUserButtonOnClicked(view : View){
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun singIn(){
        mFirebaseAuth!!.signInWithEmailAndPassword(emailUserLogin.text.toString(), passwordUserLogin.text.toString())
            .addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                        Log.d(FragmentActivity.ACCESSIBILITY_SERVICE, "signInWithEmail:success")
                        mFirebaseUser = mFirebaseAuth?.currentUser
                        updateUI()
                        Toast.makeText(
                            this, "Authentication success.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val startIntent = Intent(this, MainActivity::class.java)
                        startActivity(startIntent)
                    } else { // If sign in fails, display a message to the user.
                        Log.w(
                            FragmentActivity.ACCESSIBILITY_SERVICE,
                            "signInWithEmail:failure",
                            task.exception
                        )
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI()
                    }
                    // ...
                })

    }

    private fun updateUI() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
