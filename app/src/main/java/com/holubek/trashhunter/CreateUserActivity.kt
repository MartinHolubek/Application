package com.holubek.trashhunter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.firebase.FirebaseStorage
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.holubek.trashhunter.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_user.*
import java.io.ByteArrayOutputStream

/**
 * Trieda na vytvorenie nového užívateľa
 */
class CreateUserActivity : AppCompatActivity() {

    private var mFirebaseAuth: FirebaseAuth? = null
    val SELECT_IMAGE = 1000
    val TAG = "CreateUserActivity"
    private var imageUri: Uri? = null
    private var firebaseRepository : FirebaseRepository?=null
    private var firebaseStorage : FirebaseStorage?=null

    /**
     * Inicializácia atribútov
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseRepository =
            FirebaseRepository()
        firebaseStorage = FirebaseStorage()

        var button = findViewById<Button>(R.id.button_create_user)
        button.setOnClickListener {
            if(!sing_up_email.text.toString().equals("") && !singUpPass.text.toString().equals("")){
                createAccount()
            }else{
                Toast.makeText(
                    this, "Vložte správne údaje",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    /**
     * Zobrazí výber obrázkov z úložiska telefónu
     */
    fun onClickAddImage(view : View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED){
                //permission not granted
                val permissions = Array(1){android.Manifest.permission.READ_EXTERNAL_STORAGE}
                //show popup for runtime perrmision
                requestPermissions(permissions, Context.CONTEXT_INCLUDE_CODE)

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)

            }else{
                //permision allready granted
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)
            }
        }else{
            //system os is less as marshmallow
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)
        }
    }

    /**
     * Po ukončení výberu profilovej fotky nastaví novú profilovú fotku
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var uri = data?.data
        if (uri != null){
            Picasso.get().load(uri).into(create_user_image)
        }
    }

    /**
     * Uloží účet do Firebase Authentication
     */
    private fun createAccount(){

        mFirebaseAuth?.createUserWithEmailAndPassword(sing_up_email.text.toString(), singUpPass.text.toString())
            ?.addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                        Log.d(FragmentActivity.ACCOUNT_SERVICE, "createUserWithEmail:success")

                        //nastavenie fotky
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(imageUri)
                            .setDisplayName(sign_up_name.text.toString())
                            .build()
                        mFirebaseAuth?.currentUser?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("-newimage-", "User profile updated.")
                                }else{
                                    Log.d("-newimage-", "User profile crashed.")
                                }
                            }

                        Toast.makeText(
                                this, "Authentication success.",
                        Toast.LENGTH_SHORT
                        ).show()

                        saveInfoAccount(mFirebaseAuth?.currentUser?.uid!!)

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
                    }
                })
    }

    /**
     * Uloží údaje o uživateľovi do databázy firebase
     * @param uid jedinečný identifikátor užívateľa
     */
    private fun saveInfoAccount(uid: String){
        var name = findViewById<TextView>(R.id.sign_up_name)
        var image = findViewById<ImageView>(R.id.create_user_image)

        var bitmap = (image.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val data = baos.toByteArray()
        firebaseStorage?.saveImageUser(data)?.addOnSuccessListener {
            firebaseRepository?.saveAccountInfo(uid,name.text.toString(), it.metadata?.path.toString())?.addOnFailureListener {
                Log.e(TAG, "Chyba pri ukladaní udajov noveho uživateľa")
            }?.addOnSuccessListener {
                val startIntent = Intent(this, MainActivity::class.java)
                startIntent.putExtra("image",data)
                startActivity(startIntent)
            }
        }
    }
}
