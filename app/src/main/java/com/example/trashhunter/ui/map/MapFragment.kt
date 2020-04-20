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
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.text.bold
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.mapping.view.LocationDisplay.DataSourceStatusChangedEvent
import com.esri.arcgisruntime.mapping.view.LocationDisplay.DataSourceStatusChangedListener
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.example.trashhunter.DateFormat
import com.example.trashhunter.Map
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
    private lateinit var imageBeforePhoto: ImageView
    private lateinit var imageAfterPhoto: ImageView
    private var mLocationDisplay: LocationDisplay? = null

    private var uriPictureBefore:Uri?=null
    private var uriPictureAfter:Uri?=null

    lateinit var listUsersPlaces : List<Place>
    var currentPhotoPathBefore:String?=null
    var currentPhotoPathAfter: String? = null

    val REQUEST_IMAGE_CAPTURE_BEFORE = 1
    val REQUEST_IMAGE_CAPTURE_AFTER = 2

    //objekt na zistenie adresy
    private lateinit var mLocatorTask : LocatorTask
    private  val REQUEST_CODE_PERMISSIONS = 10
    // This is an array of all the permission specified in the manifest.
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private val PERMISSION_CODE_CAMERA = 1000
    private val PERMISSION_CODE_LOCATION = 2

    lateinit var pathToFile: String


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
            ViewModelProviders.of(this).get(MapViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_map, container, false)
        val editText: EditText = root.findViewById(R.id.placeInfo)

        var inputMethodManager: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText,InputMethodManager.SHOW_IMPLICIT)

        // create a LocatorTask from an online service

        mLocatorTask = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")

        mapView = root.findViewById<MapView>(R.id.map1)

        mapViewModel.map.observe(this, Observer {
            mapView!!.map = it
            mapView.invalidate()
        })


        mapViewModel.getPlacesOfFriends().observe(this, Observer {
            thread {
                listUsersPlaces = it
                if (mapView.map != null){
                    val myGraphicsOverlay = GraphicsOverlay()
                    for (item in listUsersPlaces){
                        val pt = Point(item.coordinates?.latitude!!,item.coordinates?.longitude!!, SpatialReference.create(4326))
                        val mySymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 18.0F)

                        val myGraphic = Graphic(pt, mySymbol)
                        myGraphic.attributes["username"] = item.userName
                        myGraphic.attributes["placename"] = item.placeName
                        myGraphic.attributes["date"] = item.date?.time
                        myGraphic.attributes["clearText"] = item.ClearText
                        myGraphic.attributes["cleared"] = item.cleared
                        myGraphic.attributes["clearUsername"] = item.clearedBy
                        if (item.countOfRating == null || item.countOfRating == 0){
                            myGraphic.attributes["rating"] = "žiadne hodnotenie"
                        }else{
                            var rating = item.rating?.div(item.countOfRating!!)
                            myGraphic.attributes["rating"] = rating
                        }

                        myGraphicsOverlay.graphics.add(myGraphic)
                    }
                    mapView.graphicsOverlays.add(myGraphicsOverlay)
                }
            }
        })
        val scrollView = root.findViewById<NestedScrollView>(R.id.mapScrollView)
        Map.setMove(root,mapView,scrollView)
        setOnTouchListener(root)

        val buttonClean : Button = root.findViewById(R.id.button_clean)
        buttonClean.setOnClickListener(View.OnClickListener {
            savePoint(root)
        })

        imageBeforePhoto = root.findViewById<ImageView>(R.id.imageView_before)
        val textViewHintBefore = root.findViewById<TextView>(R.id.textViewHintBeforePhoto)
        imageBeforePhoto.setOnClickListener(View.OnClickListener {

            if (uriPictureBefore != null){
                showDialog(root,"Fotka je už nahratá, chcete zmeniť fotku?",true)
            }else{
                //dispatchTakePictureIntentBefore(root)
                dispatchTakePictureIntent(root,REQUEST_IMAGE_CAPTURE_BEFORE)
                textViewHintBefore.visibility = View.GONE
            }
        })
        imageAfterPhoto = root.findViewById<ImageView>(R.id.imageView_after)
        val textViewHintAfter = root.findViewById<TextView>(R.id.textViewHintAfterPhoto)

        imageAfterPhoto.setOnClickListener(View.OnClickListener {

            if (uriPictureAfter != null){
                showDialog(root,"Fotka je už nahratá, chcete zmeniť fotku?",false)
            }else{
                //dispatchTakePictureIntentAfter(root)
                dispatchTakePictureIntent(root, REQUEST_IMAGE_CAPTURE_AFTER)
                textViewHintAfter.visibility = View.GONE
            }
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
        var placeInfo = view?.findViewById<EditText>(R.id.placeInfo)

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
        point2.ClearText = placeInfo?.text.toString()
        point2.date = Calendar.getInstance().time
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

                        //ak  mapView obsahuje bod na súradniciach MotionEventu,
                        // tak zobraz dialogove okno
                        if (identifyGraphics.get().size > 0 &&
                            identifyGraphics.get().get(0).graphics.size > 0){
                            identifyGraphics.get().get(0).graphics.get(0).isSelected = true

                            var place = identifyGraphics.get().get(0).graphics.get(0)
                            var username =   place.attributes.get("username").toString()
                            var placename =   place.attributes.get("placename").toString()
                            var date =  Date(place.attributes.get("date") as Long)
                            var text =   place.attributes.get("clearText").toString()
                            var rating =   place.attributes.get("rating").toString()
                            var cleared = place.attributes.get("cleared")
                            var clearedUsername = place.attributes.get("clearedUsername")


                            var r = rating.toFloatOrNull()
                            if (r == null){
                                r = -1F
                            }
                            if (cleared as Boolean && clearedUsername == null){
                                clearedUsername = username
                            }

                            showDialog( root,username,placename,date,text, r,cleared as Boolean,clearedUsername)
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

    private fun showDialog(view: View, username: String, placename: String, date: Date, text: String, rating: Float,cleared: Boolean,clearedUser: Any?){
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
        var ratingValue : String
        if (rating == -1F){
            ratingValue = "žiadne hodnotenie"
        }else{
            ratingValue = rating.toString()
        }
        var helpInfo = "Uživatel: ${username}\n\nNázov miesta: ${placename}\n\nDátum označenia: ${date}\n\nPopis: ${text}\n\nHodnotenie: ${ratingValue}"
        var stringBuilder = SpannableStringBuilder()
        if (cleared){
            stringBuilder.bold { append("Miesto vyčistil:") }
                .append(clearedUser.toString() + "\n\n")
        }else{
            stringBuilder.bold { append("Miesto nie je vyčististené") }
                .append("\n\n")
        }
        stringBuilder.bold {append("Názov miesta: ")  }
            .append(placename + "\n\n")
            .bold { append("Dátum označenia: ") }
            .append(DateFormat.getDateFormat(date) + "\n\n")
            .bold { append("Popis: ") }
            .append(text + "\n\n")
            .bold {  append("Hodnotenie: ") }
            .append(ratingValue + "\n\n")
        var info = "<b>Uživatel:</b> ${username}\n\n" +
                "<b>Názov miesta:</b> ${placename}\n\n" +
                "<b>Dátum označenia:</b> ${date}\n\n" +
                "<b>Popis:</b> ${text}\n\n" +
                "<b>Hodnotenie:</b> ${ratingValue}"
        builder.setMessage(stringBuilder)

        builder.setTitle("Príspevok uživateľa ${username}")
        builder.setNeutralButton("Zavrieť",dialogClickListener)

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
                requestPermissions(requestPermissions,PERMISSION_CODE_LOCATION)
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
        when(requestCode){
            REQUEST_IMAGE_CAPTURE_BEFORE, REQUEST_IMAGE_CAPTURE_AFTER ->{
                if (grantResults.size > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    openCamera(requestCode)
                }
            }
            PERMISSION_CODE_LOCATION ->{
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    mLocationDisplay?.startAsync()
                }else
                {
                    Toast.makeText(this.context, resources.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Metoda vráti jedinečný názov súboru pre novú fotografiu s použitím časovej pečiatky
     */
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(v: View, requestCode: Int): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            if (requestCode == REQUEST_IMAGE_CAPTURE_BEFORE){
                currentPhotoPathBefore = absolutePath
            }else if(requestCode == REQUEST_IMAGE_CAPTURE_AFTER){
                currentPhotoPathAfter = absolutePath
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
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_IMAGE_CAPTURE_BEFORE){
                var bitmap = BitmapFactory.decodeFile(currentPhotoPathBefore)

                setPic(imageView_before,REQUEST_IMAGE_CAPTURE_BEFORE)
            }else if(requestCode == REQUEST_IMAGE_CAPTURE_AFTER){
                var bitmap = BitmapFactory.decodeFile(currentPhotoPathAfter)

                setPic(imageView_after,REQUEST_IMAGE_CAPTURE_AFTER)
            }
        }
    }

    private fun dispatchTakePictureIntent(root: View, requestCode: Int) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //musime skontrolovat ci ma pridelene prava
            if (checkSelfPermission(context!!, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(context!!,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                // povolenie je zakazane, po6iada5 o povolenine
                var permission: Array<String> = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission,requestCode)
            }else{
                openCamera(requestCode)
            }
        }else{
            openCamera(requestCode)
        }
    }

    private fun openCamera(requestCode: Int){
        thread {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile(view!!,requestCode)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    if (requestCode == REQUEST_IMAGE_CAPTURE_BEFORE){
                        uriPictureBefore = Uri.fromFile(photoFile)
                        //uriPictureBefore = FileProvider.getUriForFile(context!!,"com.example.android.fileprovider",photoFile!!)
                        //takePictureIntent.putExtra( android.provider.MediaStore.EXTRA_SIZE_LIMIT, "720000");
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPictureBefore)
                        startActivityForResult(takePictureIntent, requestCode)
                    }else if(requestCode == REQUEST_IMAGE_CAPTURE_AFTER){

                        uriPictureAfter = Uri.fromFile(photoFile)
                        //takePictureIntent.putExtra( android.provider.MediaStore.EXTRA_SIZE_LIMIT, "720000")
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPictureAfter)
                        startActivityForResult(takePictureIntent, requestCode)
                    }
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
            BitmapFactory.decodeFile(currentPhotoPathBefore, bmOptions)?.also { bitmap ->
                imageBeforePhoto.setImageBitmap(bitmap)
            }
        }else if(requestCode == REQUEST_IMAGE_CAPTURE_AFTER){
            BitmapFactory.decodeFile(currentPhotoPathAfter, bmOptions)?.also { bitmap ->
                imageAfterPhoto.setImageBitmap(bitmap)
            }
        }
    }
}