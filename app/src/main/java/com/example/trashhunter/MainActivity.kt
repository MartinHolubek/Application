package com.example.trashhunter

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // license with a license key
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud3079624525,none,E9PJD4SZ8P6PH6JRP224")

        //vrchna lišta
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //tlačidlo dole v rohu
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_map, R.id.nav_addevent,
                R.id.nav_events, R.id.nav_friends, R.id.nav_addfriend
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth?.currentUser

        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_logout -> {
                mFirebaseAuth?.signOut()
                val startIntent = Intent(this, LoginActivity::class.java)
                startActivity(startIntent)
            }
            else -> onSupportNavigateUp()
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==2){
            var address = data?.getStringExtra("MESSAGE")
            var textLocation = findViewById<EditText>(R.id.inputLocation)
            textLocation.setText(address)
        }
    }

    private fun updateUI() {
        var uemail = findViewById(R.id.nav_view) as NavigationView
        var navHead = uemail.getHeaderView(0)
        var tvEmail = navHead.findViewById(R.id.nav_header_email) as TextView
        var tvDisplayName = navHead.findViewById(R.id.nav_header_name) as TextView
        var tvImage = navHead.findViewById(R.id.nav_header_image) as ImageView
        tvEmail.text = mFirebaseUser?.email
        tvDisplayName.text = mFirebaseUser?.displayName




        /*val myUrlStr = "xyz"
        val url: URL
        var uri: Uri = Uri.EMPTY
        try {
            url = URL(mFirebaseUser?.photoUrl.toString())
            uri = Uri.parse(url.toURI().toString())
        } catch (e1: MalformedURLException) {
            e1.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        tvImage.setImageURI(null)
        tvImage.setImageURI(uri)*/

    }
}
