package com.example.trashhunter.ui.place

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.example.trashhunter.Comment
import com.example.trashhunter.Picture
import com.example.trashhunter.Place

import com.example.trashhunter.R
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class PlaceFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceFragment()
    }
    private var PICTURE_REQUEST_CODE = 1

    private lateinit var viewPager:ViewPager
    private lateinit var viewPagerComments:ViewPager
    private lateinit var argbEvaluator: ArgbEvaluator
    private lateinit var pictures : ArrayList<Picture>
    private lateinit var place: Place
    private var placeID:String?=null
    private lateinit var comments: List<Comment>
    private lateinit var placeViewModel: PlaceViewModel
    private lateinit var mapView:MapView
    private lateinit var uriPictureAfter:Uri
    private var pictureFile:File?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        placeViewModel =
            ViewModelProviders.of(this).get(PlaceViewModel::class.java)
        val root = inflater.inflate(R.layout.place_fragment, container, false)
        if (placeID == null){
            placeID = arguments?.getString("POINT_ID")
        }
        viewPager = root.findViewById<ViewPager>(R.id.viewPager)
        viewPagerComments = root.findViewById(R.id.viewPagerComments)
        mapView = root.findViewById<MapView>(R.id.placeMap)
        mapView!!.map = ArcGISMap(Basemap.Type.STREETS_VECTOR, 49.201476197, 18.870735168, 11)

        //Vyhladanie miesta podla ID
        placeViewModel.getPlace(placeID.toString()).observe(this, Observer { it ->
            place = it
            updatePlace(root)
        })

        //Observer na naplnenie komentárov do pageViewera
        placeViewModel.getComments(placeID.toString()).observe(this, Observer {
            comments = it
            updateComments(root)
        })

        var buttonRatePlace = root.findViewById<AppCompatButton>(R.id.buttonPlaceRate)
        buttonRatePlace.setOnClickListener(View.OnClickListener {

            var ratingbarPlace = root.findViewById<RatingBar>(R.id.placeRatingBar)
            var comment = root.findViewById<AppCompatEditText>(R.id.placeTextRating)

            placeViewModel.saveRating(place.pointID.toString(),
                place.countOfRating!!,place.rating!!,ratingbarPlace.rating,comment.text.toString())
        })

        var buttonTakePicture = root.findViewById<Button>(R.id.button_takeAftPicture)
        buttonTakePicture.setOnClickListener {
            dispatchTakePictureIntent(root)
            //dsPicture(root)
        }
        return root
    }
    private fun dsPicture(root: View){
        val photoFile: File? = try {
            createImageFile(root)
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }
        // Continue only if the File was successfully created

        uriPictureAfter = Uri.fromFile(photoFile)
        var intent = ShareCompat.IntentBuilder.from(activity)
            .setStream(uriPictureAfter)
            .intent
            .setAction(MediaStore.ACTION_IMAGE_CAPTURE)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriPictureAfter)
        startActivityForResult(intent, PICTURE_REQUEST_CODE)
    }

    private fun dispatchTakePictureIntent(root: View) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                // Create the File where the photo should go
                thread {
                    pictureFile = try {
                        createImageFile(root)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created

                    uriPictureAfter = Uri.fromFile(pictureFile)

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriPictureAfter)
                    startActivityForResult(takePictureIntent, PICTURE_REQUEST_CODE)
                }
            }
        }
    }

    private fun setPic(): Bitmap {

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inPurgeable = true
        }
        return BitmapFactory.decodeFile(pictureFile?.absolutePath, bmOptions)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //data su niekedy null niekedy nie, tak treba zistit cestu k suboru kde sa nachadza fotka
        if (resultCode== RESULT_OK){
            if(requestCode== PICTURE_REQUEST_CODE){

                if (pictures.size == 2){
                    pictures.removeAt(1)
                }
                var bm = setPic()
                pictures.add(Picture(bm,"Fotka po"))
                updatePictures(view!!)
            }
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
            uriPictureAfter = absolutePath.toUri()
        }
    }

    private fun updateComments(root: View) {
        var adapter = AdapterComments(comments,root.context)

        viewPagerComments.adapter = adapter
        viewPagerComments.setPadding(130,0,130,0)

    }

    private fun updatePlace(root: View) {
        pictures = ArrayList<Picture>()
        downloadPictures(root)
        updateInfo(root,place)
        updateMap(mapView,place.coordinates!!)

        var mapLocation = root.findViewById<TextView>(R.id.placeLocation)
        mapLocation.text = place.placeName
        var mapCoordination = root.findViewById<TextView>(R.id.placeCoordination)

        mapCoordination.text = "${place.coordinates!!.latitude.toString()}, ${place.coordinates!!.longitude.toString()}"
    }

    private fun updateMap(map: MapView,point: GeoPoint) {
        val pt = Point(point.latitude,point.longitude, SpatialReference.create(4326))
        val mySymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 18.0F)

        val myGraphic = Graphic(pt, mySymbol)

        val myGraphicsOverlay = GraphicsOverlay()

        myGraphicsOverlay.graphics.add(myGraphic)
        map.graphicsOverlays.add(myGraphicsOverlay)
    }

    private fun updateInfo(view: View, place: Place) {
        var cleared = view.findViewById<TextView>(R.id.placeCleared)
        var userName = view.findViewById<TextView>(R.id.placeUsername)
        var date = view.findViewById<TextView>(R.id.placeDate)
        var text = view.findViewById<TextView>(R.id.placeText)

        if (place.cleared!!){
            cleared.text = "Vyčistené"
        }else{
            cleared.text = "Nevyčistené"
        }

        userName.text = place.userName

        var df = android.text.format.DateFormat.getDateFormat(view.context)
        date.text = df.format(Date(place.date?.time!!))

        text.text = place.ClearText
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        placeViewModel = ViewModelProviders.of(this).get(PlaceViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun downloadPictures(view:View){
        //Referencia na obrázok v úložisku Firebase
        var photoBeforeRef = FirebaseStorage.getInstance()
            .reference
            .child(place.photoBefore.toString())

        val ONE_MEGABYTE: Long = 1024 * 1024
        photoBeforeRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            // Konvertujeme byteArray na bitmap
            var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            pictures.add(Picture(bmp,"Fotka pred"))

            updatePictures(view)
        }.addOnFailureListener {
            // Handle any errors
        }
        if (place.photoAfter != null){
            var photoAfterRef = FirebaseStorage.getInstance()
                .reference
                .child(place.photoAfter.toString())
            photoAfterRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                // Konvertujeme byteArray na bitmap
                var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                pictures.add(Picture(bmp,"Fotka po"))

                /*var adapterPictures = AdapterPictures(pictures,view.context)

                viewPager.adapter = adapterPictures
                viewPager.setPadding(130,0,130,0)*/
                updatePictures(view)
            }.addOnFailureListener {
                // Handle any errors
            }
        }

    }

    fun updatePictures(view: View){
        var adapterPictures = AdapterPictures(pictures,view.context)

        viewPager.adapter = adapterPictures
        viewPager.setPadding(130,0,130,0)
    }

    inner class AdapterPictures : PagerAdapter{

        private lateinit var models:List<Picture>
        private lateinit var context: Context

        constructor(models: List<Picture>, context: Context) : super() {
            this.models = models
            this.context = context
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view.equals(`object`)
        }

        override fun getItemPosition(`object`: Any): Int {
            var i = 0
            while (i < count){
                if (`object` as Picture == models[i]){
                    return i
                }
                i++
            }
            return POSITION_NONE
        }

        override fun getCount(): Int {
            return models.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var view = layoutInflater.inflate(R.layout.ticket_picture,null)
            var currentPicture = models[position]
            var imageView:ImageView
            var title:TextView

            imageView = view.findViewById(R.id.imageTicketPlace)
            title = view.findViewById<TextView>(R.id.pictureTitle)
            imageView.setImageBitmap(currentPicture.image)
            title.setText(currentPicture.title)

            container.addView(view,0)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }

    inner class AdapterComments : PagerAdapter{

        private lateinit var models:List<Comment>
        private lateinit var context: Context

        constructor(models: List<Comment>, context: Context) : super() {
            this.models = models
            this.context = context
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view.equals(`object`)
        }

        override fun getItemPosition(`object`: Any): Int {
            var i = 0
            while (i < count){
                if (`object` as Comment == models[i]){
                    return i
                }
                i++
            }
            return POSITION_NONE
        }

        override fun getCount(): Int {
            return models.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var view = layoutInflater.inflate(R.layout.ticket_comment,null)
            var currentComment = models[position]
            var text:TextView
            var textUserName:TextView
            var date: TextView
            var ratingBar:RatingBar

            text = view.findViewById<TextView>(R.id.commentText)
            textUserName =  view.findViewById<TextView>(R.id.commentUsername)
            ratingBar = view.findViewById<RatingBar>(R.id.commentRating)
            date = view.findViewById<TextView>(R.id.commentDate)

            text.setText(currentComment.comment)
            textUserName.setText(currentComment.userName)
            ratingBar.rating = currentComment.rating!!
            var df = android.text.format.DateFormat.getDateFormat(view.context)
            date.text = df.format(currentComment.date)

            container.addView(view,0)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)

        }

    }

}
