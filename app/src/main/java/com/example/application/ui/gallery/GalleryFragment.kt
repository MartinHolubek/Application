package com.example.application.ui.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.LocationDisplay.DataSourceStatusChangedEvent
import com.esri.arcgisruntime.mapping.view.LocationDisplay.DataSourceStatusChangedListener
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.example.application.R
import kotlinx.android.synthetic.main.fragment_gallery.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var mapView: MapView
    private lateinit var map: ArcGISMap
    private var mLocationDisplay: LocationDisplay? = null

    private var byteArray_photo_before: ByteArray? = null
    private var byteArray_photo_after: ByteArray? = null

    var currentPhotoPath: String? = null
    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_IMAGE_CAPTURE_BEFORE = 1
    val REQUEST_IMAGE_CAPTURE_AFTER = 2

    private val requestCode = 2
    var reqPermissions = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProviders.of(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        val mapView = root.findViewById<MapView>(R.id.map1)
        galleryViewModel.text.observe(this, Observer {
            textView.text = it
            //update UI
        })
        galleryViewModel.map.observe(this, Observer {
            mapView!!.map = it
        })
        galleryViewModel.before_photo_path.observe(this, Observer {
            //imageView_before.setImageURI(Uri.parse(it))
        })
        galleryViewModel.after_photo_path.observe(this, Observer {
            //imageView_after.setImageURI(Uri.parse(it))
        })

        val buttonClean : Button = root.findViewById(R.id.button_clean)
        buttonClean.setOnClickListener(View.OnClickListener {
            mLocationDisplay = mapView.locationDisplay

            var x = mLocationDisplay?.location?.position?.x
            var y = mLocationDisplay?.location?.position?.y
            if(x != null && y != null){
                addPoint(mapView,x,y)
            }else{
                Toast.makeText(root.context,resources.getString(R.string.undefined_location),Toast.LENGTH_SHORT).show()
            }
            var currentTime = Calendar.getInstance().time


            //urobit if ak sa byteaaray rovna null
            val point = hashMapOf(
                "x" to x,
                "y" to y,
                "date" to currentTime
            )

            if (byteArray_photo_after != null && byteArray_photo_before != null){
                Toast.makeText(root.context,galleryViewModel.addPoint(point,byteArray_photo_before, byteArray_photo_after), Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(root.context,R.string.without_pictures, Toast.LENGTH_LONG).show()
            }


        })

        val buttonGPS : Button = root.findViewById(R.id.button_gps)
        buttonGPS.setOnClickListener(View.OnClickListener {
            galleryViewModel.changeText("GPS spustena")
            setupLocationDisplay(root, mapView)

        })

        val buttonBeforePhoto : Button = root.findViewById(R.id.button_photo_before)
        buttonBeforePhoto.setOnClickListener(View.OnClickListener {
            dispatchTakePictureIntentBefore(root)
        })

        val buttonAfterPhoto : Button = root.findViewById(R.id.button_photo_after)
        buttonAfterPhoto.setOnClickListener(View.OnClickListener {
            //dispatchTakePictureIntentAfter(root)
            galleryViewModel.getURL()
        })

        return root
    }


    /**
     * Zobrazí sa bod na mapovej vrstve
     * @param [mv] mapova vrstva
     * @param [x] x-ová súradnica na mapovej vrstve
     * @param [y] y-ová súradnica na mapovej vrstve
     */
    fun addPoint(mv: MapView, x : Double, y : Double){
        val pt = Point(x,y, SpatialReference.create(4326))
        val mySymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 18.0F)
        val myGraphic = Graphic(pt, mySymbol)
        val myGraphicsOverlay = GraphicsOverlay()
        myGraphicsOverlay.graphics.add(myGraphic)
        mv.graphicsOverlays.add(myGraphicsOverlay)
    }

    /**
     * Spusti gps lokalizáciu, ak aplikácia nemá prístup k polohe zariadenia,
     * tak vyžiada si povolenie na získanie polohy zariadenia
     * @param [v] aktualný view kde sa nachádzajú a zobrazujú komponenty používatelského rozhrania
     * @param [mapView] mapová vrstva
     */
    fun setupLocationDisplay(v: View, mapView: MapView){
        mLocationDisplay = mapView.locationDisplay

        mLocationDisplay?.addDataSourceStatusChangedListener(DataSourceStatusChangedListener { dataSourceStatusChangedEvent: DataSourceStatusChangedEvent ->
            if (dataSourceStatusChangedEvent.isStarted || dataSourceStatusChangedEvent.error == null) {
                return@DataSourceStatusChangedListener
            }
            val requestPermissionsCode = 2
            val requestPermissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (!(ContextCompat.checkSelfPermission(
                    v.context,
                    requestPermissions[0]
                ) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(
                    v.context,
                    requestPermissions[1]
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                requestPermissions(requestPermissions,requestPermissionsCode)
            } else {
                val message = String.format(
                    "Error in DataSourceStatusChangedListener: %s",
                    dataSourceStatusChangedEvent.source.locationDataSource.error.message
                )
                Toast.makeText(v.context, message, Toast.LENGTH_LONG).show()
            }
        })

        mLocationDisplay?.autoPanMode = LocationDisplay.AutoPanMode.COMPASS_NAVIGATION
        mLocationDisplay?.startAsync()
    }

    /**
     * Prepísaná metóda systému, ktorá skontroluje či ma aplikácia udelené povelenia,
     * ak áno, spustí sa zobrazenie polohy
     */
    override fun onRequestPermissionsResult(requestCode:Int, @NonNull permissions:Array<String>, @NonNull grantResults:IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            mLocationDisplay?.startAsync()
        }
        else
        {
            Toast.makeText(this.context, resources.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Metoda vráti jedinečný názov súboru pre novú fotografiu s použitím časovej pečiatky
     */
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(v: View): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = v.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    /**
     *
     */
    private fun dispatchTakePictureIntent(v: View) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(v.context.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(v)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    var photoURI: Uri = FileProvider.getUriForFile(
                        v.context,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    private fun dispatchTakePictureIntentBefore(v: View) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(v.context.packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_BEFORE)
            }
        }
    }

    private fun dispatchTakePictureIntentAfter(v: View) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(v.context.packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_AFTER)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_BEFORE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView_before.setImageBitmap(imageBitmap)

            // Get the data from an ImageView as bytes
            imageView_before.isDrawingCacheEnabled = true
            imageView_before.buildDrawingCache()
            val bitmap = (imageView_before.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val dataByteArray = baos.toByteArray()

            byteArray_photo_before = dataByteArray

        }
        if (requestCode == REQUEST_IMAGE_CAPTURE_AFTER && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView_after.setImageBitmap(imageBitmap)

            // Get the data from an ImageView as bytes
            imageView_after.isDrawingCacheEnabled = true
            imageView_after.buildDrawingCache()
            val bitmap = (imageView_after.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val dataByteArray = baos.toByteArray()

            byteArray_photo_after = dataByteArray
        }
    }
}