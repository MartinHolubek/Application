package com.example.trashhunter.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.mapping.view.LocationDisplay.DataSourceStatusChangedEvent
import com.esri.arcgisruntime.mapping.view.LocationDisplay.DataSourceStatusChangedListener
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.example.trashhunter.Place
import com.example.trashhunter.R
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_map.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MapFragment : Fragment() {

    private lateinit var mapViewModel: MapViewModel
    private lateinit var mapView: MapView
    private lateinit var map: ArcGISMap
    private lateinit var imageBeforePhoto: ImageView
    private var mLocationDisplay: LocationDisplay? = null

    private var byteArray_photo_before: ByteArray? = null
    private var byteArray_photo_after: ByteArray? = null

    private var uriPictureBefore:Uri?=null
    private var uriPictureAfter:Uri?=null

    lateinit var listUsersPlaces : List<Place>
    var currentPhotoBeforePath:String?=null
    var currentPhotoPath: String? = null
    val REQUEST_TAKE_PHOTO = 3
    val REQUEST_IMAGE_CAPTURE_BEFORE = 1
    val REQUEST_IMAGE_CAPTURE_AFTER = 2

    //objekt na zistenie adresy
    private lateinit var mLocatorTask : LocatorTask
    private  val REQUEST_CODE_PERMISSIONS = 10
    // This is an array of all the permission specified in the manifest.
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container?.removeAllViews()
        mapViewModel =
            ViewModelProviders.of(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        val editText: EditText = root.findViewById(R.id.text_gallery)

        var inputMethodManager: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText,InputMethodManager.SHOW_IMPLICIT)

        // create a LocatorTask from an online service

        mLocatorTask = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")

        mapView = root.findViewById<MapView>(R.id.map1)
        mapViewModel.text.observe(this, Observer {
            editText.setText(it)
            //update UI
        })
        mapViewModel.map.observe(this, Observer {
            mapView!!.map = it
        })

        mapViewModel.savedPlaces.observe(this, Observer {
            thread {
                listUsersPlaces = it
                if (mapView!!.map != null){
                    val myGraphicsOverlay = GraphicsOverlay()
                    for (item in listUsersPlaces){
                        val pt = Point(item.coordinates?.latitude!!,item.coordinates?.longitude!!, SpatialReference.create(4326))
                        val mySymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 18.0F)
                        val textSymbol = TextSymbol(30.0F,"Ahoj",Color.BLUE,TextSymbol.HorizontalAlignment.CENTER,TextSymbol.VerticalAlignment.BOTTOM)
                        val myGraphic = Graphic(pt, mySymbol)
                        myGraphic.attributes.set("username",item.userName)
                        myGraphic.attributes.set("placename",item.placeName)
                        myGraphic.attributes.set("date",item.date.toString())
                        myGraphic.attributes.set("clearText",item.ClearText)
                        myGraphic.attributes.set("rating",item.rating.toString())
                        myGraphicsOverlay.graphics.add(myGraphic)

                    }
                    mapView.graphicsOverlays.add(myGraphicsOverlay)
                }
            }
        })

        setOnTouchListener(root)

        val buttonClean : Button = root.findViewById(R.id.button_clean)
        buttonClean.setOnClickListener(View.OnClickListener {
            /*mLocationDisplay = mapView.locationDisplay

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
            val point2 = Place()
            point2.ClearText = "text miesta"
            point2.date = Calendar.getInstance().time
            point2.placeName = "Dumbier"
            point2.coordinates = GeoPoint(x!!,y!!)
            point2.rating = 0F
            point2.countOfRating = 0

            var bitmap = (imageView_before.drawable as BitmapDrawable).bitmap
            val baos =ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()

            if (uriPictureAfter == null){

            }
            var bitmap2 = (imageView_after.drawable as BitmapDrawable).bitmap
            val baos2 =ByteArrayOutputStream()
            bitmap2.compress(Bitmap.CompressFormat.JPEG, 50, baos2)
            val data2 = baos2.toByteArray()

            var addressInfo = mLocatorTask.reverseGeocodeAsync(Point(x,y))
            addressInfo.addDoneListener(Runnable {
                kotlin.run {
                    if (addressInfo.get().size> 0){
                        var address =addressInfo.get().get(0).attributes.getValue("Address").toString() + "," +
                                addressInfo.get().get(0).attributes.getValue("City")
                        point2.placeName = address

                        Toast.makeText(root.context,mapViewModel.addPoint2(point2,data, data2), Toast.LENGTH_LONG).show()

                    }
                }
            })*/
            savePoint(root)
        })

        /*val buttonGPS : Button = root.findViewById(R.id.button_gps)
        buttonGPS.setOnClickListener(View.OnClickListener {
            mapViewModel.changeText("GPS spustena")
            setupLocationDisplay(root, mapView)

        })*/
        imageBeforePhoto = root.findViewById(R.id.imageView_before)
        val textViewHintBefore = root.findViewById<TextView>(R.id.textViewHintBeforePhoto)
        imageBeforePhoto.setOnClickListener(View.OnClickListener {

            dispatchTakePictureIntent(root,REQUEST_IMAGE_CAPTURE_BEFORE)
            textViewHintBefore.visibility = View.GONE
            /*if (uriPictureBefore != null){
                showDialog(root,"Fotka je už nahratá, chcete zmeniť fotku?",true)
            }else{
                //dispatchTakePictureIntentBefore(root)
                dispatchTakePictureIntent(root,REQUEST_IMAGE_CAPTURE_BEFORE)
                textViewHintBefore.visibility = View.GONE
            }*/

        })
        val imageAfterPhoto = root.findViewById<ImageView>(R.id.imageView_after)
        val textViewHintAfter = root.findViewById<TextView>(R.id.textViewHintAfterPhoto)

        imageAfterPhoto.setOnClickListener(View.OnClickListener {

            dispatchTakePictureIntent(root, REQUEST_IMAGE_CAPTURE_AFTER)
            textViewHintAfter.visibility = View.GONE
            /*if (uriPictureAfter != null){
                showDialog(root,"Fotka je už nahratá, chcete zmeniť fotku?",false)
            }else{
                //dispatchTakePictureIntentAfter(root)
                dispatchTakePictureIntent(root, REQUEST_IMAGE_CAPTURE_AFTER)
                textViewHintAfter.visibility = View.GONE
            }*/

            //mapViewModel.getURL()
        })
        val frameLayout = root.findViewById<FrameLayout>(R.id.framelayoutAfterPhoto)
        frameLayout.visibility = View.GONE
        val switch = root.findViewById<Switch>(R.id.switchAddAfterPhoto)
        switch.setOnClickListener(View.OnClickListener {
            if (switch.isChecked){
                frameLayout.visibility = View.VISIBLE
            }else{
                frameLayout.visibility = View.GONE
            }
        })

        setupLocationDisplay(root, mapView)

        return root
    }

    private fun savePoint(root: View){
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
        val point2 = Place()
        point2.ClearText = "text miesta"
        point2.date = Calendar.getInstance().time
        point2.placeName = "Dumbier"
        point2.coordinates = GeoPoint(x!!,y!!)
        point2.rating = 0F
        point2.countOfRating = 0


        var addressInfo = mLocatorTask.reverseGeocodeAsync(Point(x,y))
        addressInfo.addDoneListener(Runnable {
            kotlin.run {
                if (addressInfo.get().size> 0){
                    var address =addressInfo.get().get(0).attributes.getValue("Address").toString() + "," +
                            addressInfo.get().get(0).attributes.getValue("City")
                    point2.placeName = address

                    var bitmap = (imageView_before.drawable as BitmapDrawable).bitmap
                    val baos =ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    val data = baos.toByteArray()

                    if (uriPictureAfter == null){
                        Toast.makeText(root.context,mapViewModel.addPoint2(point2,data, null), Toast.LENGTH_LONG).show()
                    }else{
                        var bitmap2 = (imageView_after.drawable as BitmapDrawable).bitmap
                        val baos2 =ByteArrayOutputStream()
                        bitmap2.compress(Bitmap.CompressFormat.JPEG, 50, baos2)
                        val data2 = baos2.toByteArray()

                        Toast.makeText(root.context,mapViewModel.addPoint2(point2,data, data2), Toast.LENGTH_LONG).show()
                    }

                }
            }
        })
    }

    private fun setOnTouchListener(root:View) {
        mapView.onTouchListener = object:DefaultMapViewOnTouchListener(root.context, mapView) {

            override fun onSingleTapConfirmed(e:MotionEvent):Boolean {
                val screenPoint : android.graphics.Point = android.graphics.Point(e.x.toInt(),e.y.toInt())

                val  identifyGraphics : ListenableFuture<MutableList<IdentifyGraphicsOverlayResult>>? =
                    mapView.identifyGraphicsOverlaysAsync(screenPoint,10.0,false)


                identifyGraphics?.addDoneListener(Runnable {
                    kotlin.run {
                        Toast.makeText(root.context, identifyGraphics.get().size.toString() + ",,,", Toast.LENGTH_SHORT).show()

                        //ak list obsahuje nejaky bod
                        if (identifyGraphics.get().size > 0 &&
                            identifyGraphics.get().get(0).graphics.size > 0){
                            identifyGraphics.get().get(0).graphics.get(0).isSelected = true

                            var place = identifyGraphics.get().get(0).graphics.get(0)
                            var username =   place.attributes.get("username").toString()
                            var placename =   place.attributes.get("placename").toString()
                            var date =   place.attributes.get("date").toString()
                            var text =   place.attributes.get("clearText").toString()
                            var rating =   place.attributes.get("rating").toString()


                            // zobraz9 dialogove okno
                            showDialog( root,username,placename,date,text,rating)
                        }else{
                            //Toast.makeText(root.context, "Ziadny vybrany bod", Toast.LENGTH_SHORT).show()
                        }
                    }
                })

                return true
            }
        }
    }

    private fun showDialog(view: View, text:String, before:Boolean){
        //objekt dialog okno
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(view.context)

        builder.setTitle("Nahrat fotku")

        builder.setMessage(text)

        val dialogClickListener = DialogInterface.OnClickListener{_,which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    if (before){
                        dispatchTakePictureIntent(view,REQUEST_IMAGE_CAPTURE_BEFORE)
                    }else{
                        dispatchTakePictureIntent(view, REQUEST_IMAGE_CAPTURE_AFTER)
                    }
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

    private fun showDialog(view: View, username: String, placename: String, date: String, text: String, rating: String){
        //objekt dialog okno
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(view.context)

        val dialogClickListener = DialogInterface.OnClickListener{_,which ->
            when(which){
                DialogInterface.BUTTON_NEUTRAL -> {

                }
            }
        }

        builder.setTitle("Dialog")

        builder.setMessage("Uživatel:${username}\nNázov miesta:${placename}\nDátum označenia:${date}\nPopis:${text}\nHodnotenie:${rating}")


        builder.setNeutralButton("CANCEL",dialogClickListener)

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
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
        val textSymbol = TextSymbol(30.0F,"Ahoj",Color.BLUE,TextSymbol.HorizontalAlignment.CENTER,TextSymbol.VerticalAlignment.BOTTOM)
        val myGraphic = Graphic(pt, mySymbol)
        myGraphic.attributes.set("Address","Varin")
        myGraphic.attributes.set("user","Martin")

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

        mLocationDisplay?.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
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
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this.context!!, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Metoda vráti jedinečný názov súboru pre novú fotografiu s použitím časovej pečiatky
     */
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(v: View, requestCode: Int): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = v.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            if (requestCode == REQUEST_IMAGE_CAPTURE_BEFORE){
                currentPhotoBeforePath = absolutePath
            }else if(requestCode == REQUEST_IMAGE_CAPTURE_AFTER){
                currentPhotoPath = absolutePath
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

    /**
     * Metoda, ktorá sa volá po ukončení aktivity
     * @param requestCode - kod vložený ako parameter pri starte aktivity
     * @param resultCode - kod vysledku
     * @param data - vrati bitmap v atribute extra
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_IMAGE_CAPTURE_BEFORE){
                setPic(imageView_before,REQUEST_IMAGE_CAPTURE_BEFORE)
            }else if(requestCode == REQUEST_IMAGE_CAPTURE_AFTER){
                setPic(imageView_after,REQUEST_IMAGE_CAPTURE_AFTER)
            }
        }
    }

    private fun dispatchTakePictureIntent(root: View, requestCode: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(root,requestCode)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                if (requestCode == REQUEST_IMAGE_CAPTURE_BEFORE){
                    uriPictureBefore = Uri.fromFile(photoFile)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPictureBefore)
                    startActivityForResult(takePictureIntent, requestCode)
                }else if(requestCode == REQUEST_IMAGE_CAPTURE_AFTER){
                    uriPictureAfter = Uri.fromFile(photoFile)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPictureAfter)
                    startActivityForResult(takePictureIntent, requestCode)
                }
            }
        }
    }

    private fun setPic(imageView: ImageView, requestCode: Int) {
        // Get the dimensions of the View
        val targetW: Int = imageView.width
        val targetH: Int = imageView.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE_BEFORE){

            BitmapFactory.decodeFile(currentPhotoBeforePath, bmOptions)?.also { bitmap ->
                //imageView.setImageBitmap(bitmap)
                imageView.post(object : Thread() {
                    override fun run() {
                        super.run()
                        imageView.setImageURI(null)
                        imageView.setImageURI(uriPictureBefore)
                        //imageView.setImageBitmap(bitmap)
                    }
                })
            }
        }else if(requestCode == REQUEST_IMAGE_CAPTURE_AFTER){
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
                //imageView.setImageBitmap(bitmap)
                imageView.post(object : Thread() {
                    override fun run() {
                        super.run()
                        imageView.setImageURI(null)
                        imageView.setImageURI(uriPictureAfter)
                        //imageView.setImageBitmap(bitmap)
                    }
                })
            }
        }


    }
}