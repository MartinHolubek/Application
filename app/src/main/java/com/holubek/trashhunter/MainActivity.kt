package com.holubek.trashhunter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.firebase.FirebaseStorage
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

/**
 * Trieda, v ktoréj sa zobrazujú všetky fragmenty v aplikácií
 */
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null
    private lateinit var mFirebaseStorage: FirebaseStorage
    private lateinit var mFirebaseRepository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var image : ByteArray?=null
        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras == null) {

            } else {
                image = extras.getByteArray("image")!!
            }
        }

        setContentView(R.layout.activity_main)
        // Licenčný klúč
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud3079624525,none,E9PJD4SZ8P6PH6JRP224")

        //vrchný toolbar aplikácie
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_posts, R.id.nav_map, R.id.nav_addevent,
                R.id.nav_events, R.id.nav_friends, R.id.nav_addfriend
            ), drawerLayout
        )

        //Nastavenie menu v horom ľavom rohu toolbaru aplikácie
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser
        mFirebaseStorage = FirebaseStorage()
        mFirebaseRepository = FirebaseRepository()

        if (image == null){
            updateUI()
        }else{
            updateUI(image)
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            //Požiadanie užívateľa o povolenie
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        } else {
            // Povolenie bolo udelené
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    /**
     * nastavenie možnosti v menu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_logout -> {
                mFirebaseAuth?.signOut()
                val startIntent = Intent(this, LoginActivity::class.java)
                startActivity(startIntent)
            }
            R.id.action_user_settings -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.userFragment)
            }
            else -> onSupportNavigateUp()
        }
        return true
    }

    /**
     * Zobrazenie hlavného menu
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Nastavenie informácií o užívateľovi v hlavičke hlavného menu
     * @param imageByteArray bajtové pole reprezentujúce profilovú fotku užívateľa
     */
    private fun updateUI(imageByteArray: ByteArray? = null) {
        var uemail = findViewById(R.id.nav_view) as NavigationView
        var navHead = uemail.getHeaderView(0)
        var tvEmail = navHead.findViewById(R.id.nav_header_email) as TextView
        var tvDisplayName = navHead.findViewById(R.id.nav_header_name) as TextView
        var tvImage = navHead.findViewById(R.id.nav_header_image) as ImageView

        if(imageByteArray == null){
            mFirebaseRepository.getUserInfo().addSnapshotListener(EventListener<DocumentSnapshot>{ value, e ->
                if (e != null) {
                    return@EventListener
                }
                var imagePath = value?.data?.get("image").toString()
                mFirebaseStorage.getImage(imagePath).addOnSuccessListener {
                    var bitMap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    tvImage.setImageBitmap(bitMap)
                }
            })
        }else{
            var bitMap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
            tvImage.setImageBitmap(bitMap)
        }
        tvEmail.text = mFirebaseUser?.email
        tvDisplayName.text = mFirebaseUser?.displayName

    }
}
