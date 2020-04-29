package com.holubek.trashhunter.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.holubek.trashhunter.LoginActivity
import com.holubek.trashhunter.R
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.firebase.FirebaseStorage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * Trieda zobrazuje základné informácie o užívateľovi
 */
class UserFragment : Fragment() {
    val SELECT_IMAGE = 1000
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var image:ImageView
    private lateinit var email:TextView
    private lateinit var name:TextView
    private lateinit var buttonName:Button
    private lateinit var buttonEmail:Button
    private lateinit var buttonNameSave:Button
    private lateinit var buttonEmailSave:Button
    private lateinit var buttonSavePass:Button
    private lateinit var oldPass: EditText
    private lateinit var newPass:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseRepository = FirebaseRepository()
        firebaseStorage = FirebaseStorage()
    }

    /**
     * Inicializácia informácií o užívateľovi
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root = inflater.inflate(R.layout.fragment_user, container, false)

        var buttonImage = root.findViewById<Button>(R.id.userButtonImage)
        var buttonDelete = root.findViewById<Button>(R.id.userButtonDelete)
        buttonName = root.findViewById<Button>(R.id.userButtonName)
        buttonEmail = root.findViewById<Button>(R.id.userButtonEmail)
        buttonNameSave = root.findViewById<Button>(R.id.userButtonNameSave)
        buttonEmailSave = root.findViewById<Button>(R.id.userButtonEmailSave)
        buttonSavePass = root.findViewById(R.id.userButtonPasswordSave)

        oldPass = root.findViewById(R.id.userOldPassword)
        newPass = root.findViewById(R.id.userNewPassword)

        name = root.findViewById(R.id.userName)
        name.text = firebaseAuth.currentUser!!.displayName
        name.isEnabled = false

        email = root.findViewById(R.id.userEmail)
        email.text = firebaseAuth.currentUser!!.email
        email.isEnabled = false

        image = root.findViewById<CircleImageView>(R.id.userImage)

        firebaseRepository.getUserInfo().addSnapshotListener(EventListener<DocumentSnapshot>{value, e ->
            if (e != null) {
                Log.w("Event", "Chyba pri načitaní údajov o uživateľovi")

                return@EventListener
            }

            firebaseStorage.getImage(value!!["image"].toString()).addOnSuccessListener {
                var bitMap = BitmapFactory.decodeByteArray(it, 0, it.size)
                image.setImageBitmap(bitMap)
            }
        })

        buttonDelete.setOnClickListener {
            showDialogDeleteAccount()
        }

        buttonSavePass.setOnClickListener {
            changePassword()
        }

        buttonImage.setOnClickListener {
            onClickChangeImage()
        }

        buttonName.setOnClickListener {
            buttonName.visibility = View.GONE
            buttonNameSave.visibility = View.VISIBLE
            name.isEnabled = true

            buttonNameSave.setOnClickListener {
                var newName = name.text.toString()
                if (!newName.equals("")){
                    showDialogChangeName(newName)
                }else{
                    Toast.makeText(
                        view!!.context, "Vložte nové meno užívateľa",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }

        buttonEmail.setOnClickListener {
            buttonEmail.visibility = View.GONE
            buttonEmailSave.visibility = View.VISIBLE
            email.isEnabled = true

            buttonEmailSave.setOnClickListener {
                var newEmail = email.text.toString()
                if(!newEmail.equals("")){
                    showDialogChangeEmail(newEmail)
                } else{
                    Toast.makeText(
                        view!!.context, "Vložte nový email",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }

        return root
    }

    /**
     * Zmení heslo užívateľa
     */
    fun changePassword(){
        var newPassword = newPass.text.toString()
        var oldPassword = oldPass.text.toString()
        if (!newPassword.equals("") && !oldPassword.equals("")){
            val user = FirebaseAuth.getInstance().currentUser

            // Get auth credentials from the user for re-authentication. The example below shows
            // email and password credentials but there are multiple possible providers,
            // such as GoogleAuthProvider or FacebookAuthProvider.
            val credential = EmailAuthProvider
                .getCredential(user!!.email.toString(), oldPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        view!!.context, "heslo bolo zmenené",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }else{
                        Toast.makeText(
                            view!!.context, "Vložili ste zlé heslo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
        }
    }

    /**
     * Zmení meno užívateľa
     * @param newName nové meno užívateľa
     */
    fun changeName(newName:String){
        val user = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        view!!.context, "Meno užívatela zmenené ",
                        Toast.LENGTH_SHORT
                    ).show()
                    buttonName.visibility = View.VISIBLE
                    buttonNameSave.visibility = View.GONE
                    name.isEnabled = false
                }else{
                    Toast.makeText(
                        view!!.context, "Meno užívatela už existuje ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Zmení email užívateľa
     * @param newEmail nový email užívateľa
     */
    fun changeEmail(newEmail:String){
        val user = FirebaseAuth.getInstance().currentUser

            user?.updateEmail(newEmail)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            view!!.context, "Email zmenený na " + user.email,
                            Toast.LENGTH_SHORT
                        ).show()
                        buttonEmail.visibility = View.VISIBLE
                        buttonEmailSave.visibility = View.GONE
                        email.isEnabled = false
                    }else{
                        Toast.makeText(
                            view!!.context, "Takýto email už existuje ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }

    /**
     * Zmení profilovú fotku užívateľa
     * @param uri uri adresa profilovej fotky užívateľa
     */
    fun changePicture(uri:Uri){
        val user = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        view!!.context, "Profilová fotka zmenená ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Zobrazí výber obrázkov z úložiska telefónu
     */
    fun onClickChangeImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)
    }

    /**
     * Po ukončení výberu novej profilovej fotky nastaví novú profilovú fotku
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var uri = data?.data
        if (uri != null){
            changePicture(uri)
            Picasso.get().load(uri).into(image)
        }
    }

    /**
     * Zobrazí dialógové okno s upozornením na zmenu mena užívateľa
     * @param newName nové meno užívateľa
     */
    private fun showDialogChangeName(newName: String){
        //objekt dialog okno
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(view!!.context,R.style.Dialog)

        builder.setTitle("Zmeniť meno užívateľa")
        builder.setMessage("Chcete zmeniť meno užívateľa?")

        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    changeName(newName)
                }
            }
        }

        builder.setPositiveButton("Áno",dialogClickListener)

        builder.setNegativeButton("Nie",dialogClickListener)

        dialog = builder.create()

        dialog.show()
    }

    /**
     * Zobrazí dialógové okno s upozornením na zmenu emailu užívateľa
     * @param newEmail nový email užívateľa
     */
    private fun showDialogChangeEmail(newEmail: String){
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(view!!.context,R.style.Dialog)

        builder.setTitle("Zmeniť email užívateľa")

        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    changeEmail(newEmail)
                }
            }
        }

        builder.setPositiveButton("Áno",dialogClickListener)
        builder.setNegativeButton("ie",dialogClickListener)
        dialog = builder.create()
        dialog.show()
    }

    /**
     * Zobrazí dialógové okno s upozornením na zrušenie účtu
     */
    private fun showDialogDeleteAccount(){
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(view!!.context,R.style.Dialog)

        builder.setTitle("Zrušiť účet")
        builder.setMessage("Naozaj chcete zrušiť účet?")

        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    deleteAccount()
                }
            }
        }

        builder.setPositiveButton("Áno",dialogClickListener)
        builder.setNegativeButton("Nie",dialogClickListener)
        dialog = builder.create()
        dialog.show()
    }

    /**
     * Odstráni účet uživateľa
     */
    fun deleteAccount(){
        val user = FirebaseAuth.getInstance().currentUser

        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        view!!.context, "Účet bol zrušený",
                        Toast.LENGTH_SHORT
                    ).show()
                    val startIntent = Intent(view?.context, LoginActivity::class.java)

                    startActivity(startIntent)
                }
            }
    }

}
