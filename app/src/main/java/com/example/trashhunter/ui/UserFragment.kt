package com.example.trashhunter.ui

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
import com.example.trashhunter.LoginActivity
import com.example.trashhunter.R
import com.example.trashhunter.firebase.FirebaseRepository
import com.example.trashhunter.firebase.FirebaseStorage
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {
    // TODO: Rename and change types of parameters
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
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseRepository = FirebaseRepository()
        firebaseStorage = FirebaseStorage()
    }

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

            firebaseStorage.getImageUser(value!!["image"].toString()).addOnSuccessListener {
                var bitMap = BitmapFactory.decodeByteArray(it, 0, it.size)
                image.setImageBitmap(bitMap)
            }
        })

        buttonDelete.setOnClickListener {
            showDialogDeleteAccount()
        }

        buttonSavePass.setOnClickListener {
            var newPassword = newPass.text.toString()
            var oldPassword = oldPass.text.toString()
            if (!newPassword.equals("") && !oldPassword.equals("")){
                val user = FirebaseAuth.getInstance().currentUser

                // Get auth credentials from the user for re-authentication. The example below shows
                // email and password credentials but there are multiple possible providers,
                // such as GoogleAuthProvider or FacebookAuthProvider.
                val credential = EmailAuthProvider
                    .getCredential(user!!.email.toString(), oldPassword)

                user?.reauthenticate(credential)
                    ?.addOnCompleteListener {
                        if (it.isSuccessful){
                            user?.updatePassword(newPassword)
                                ?.addOnCompleteListener { task ->
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

    fun changePassword(){
        val user = FirebaseAuth.getInstance().currentUser
        val newPassword = "SOME-SECURE-PASSWORD"

        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        view!!.context, "Heslo bolo zmenené",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun onClickChangeImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var uri = data?.data
        if (uri != null){
            changePicture(uri)
            Picasso.get().load(uri).into(image)
        }
    }

    private fun showDialogChangeName(newName: String){
        //objekt dialog okno
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(view!!.context)

        builder.setTitle("Zmeniť meno užívateľa")

        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    changeName(newName)
                }
            }
        }

        // Set the alert dialog positive/yes button
        builder.setPositiveButton("YES",dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO",dialogClickListener)

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }

    private fun showDialogChangeEmail(newEmail: String){
        //objekt dialog okno
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(view!!.context)

        builder.setTitle("Zmeniť email užívateľa")

        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    changeEmail(newEmail)
                }
            }
        }

        // Set the alert dialog positive/yes button
        builder.setPositiveButton("YES",dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO",dialogClickListener)

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }

    private fun showDialogDeleteAccount(){
        //objekt dialog okno
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(view!!.context)

        builder.setTitle("Zrušiť účet")
        builder.setMessage("Naozaj chcete zrušiť účet?")

        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    deleteAccount()
                }
            }
        }

        // Set the alert dialog positive/yes button
        builder.setPositiveButton("YES",dialogClickListener)

        // Set the alert dialog negative/no button
        builder.setNegativeButton("NO",dialogClickListener)

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }

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
