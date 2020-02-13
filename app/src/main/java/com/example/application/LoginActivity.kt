package com.example.application

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null

    var myDataset = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mFirebaseAuth = FirebaseAuth.getInstance()
    }

    fun LoginButtonClicked(view : View){
        signIn()
     }

    fun loginCreateUserButtonOnClicked(view : View){
        val startIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(startIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun signIn(){
        val email : String = emailUserLogin.text.toString()
        val pass : String = passwordUserLogin.text.toString()
        mFirebaseAuth?.signInWithEmailAndPassword(email, pass)
            ?.addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                        Log.d(FragmentActivity.ACCOUNT_SERVICE, "signInWithEmail:success")
                        mFirebaseUser = mFirebaseAuth?.currentUser
                        Toast.makeText(
                            this, "Authentication success.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val startIntent = Intent(this, MainActivity::class.java)

                        startActivity(startIntent)
                    } else { // If sign in fails, display a message to the user.
                        Log.w(
                            FragmentActivity.ACCOUNT_SERVICE,
                            "signInWithEmail:failure",
                            task.exception
                        )
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }

    private fun populateTable() {
        object : Thread() {
            override fun run() {
                signIn()
                try { // code runs in a thread
                } catch (ex: Exception) {
                    Log.i("---", "Exception in thread")
                }
            }
        }.start()
    }
}
