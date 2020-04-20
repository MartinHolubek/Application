package com.example.trashhunter.ui.addEvent

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.example.trashhunter.Event
import com.example.trashhunter.FindLocationActivity
import com.example.trashhunter.Map
import com.example.trashhunter.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class AddEventFragment : Fragment() {

    private lateinit var addEventViewModel: AddEventViewModel
    private lateinit var mapView: MapView
    private lateinit var textLocation: EditText
    private lateinit var map: ArcGISMap
    private var mLocationDisplay: LocationDisplay? = null

    val SELECT_IMAGE = 1001
    private var imageUri: Uri? = null

    private var locationName: String? = null
    private lateinit var startDate: EditText
    private lateinit var startTime: EditText
    private lateinit var endDate: EditText
    private lateinit var endTime: EditText
    private lateinit var imageViewPhotoEvent: ImageView

    private lateinit var point: GeoPoint

    val REQUEST_CODE_LOCATION = 11
    val REQUEST_CODE_PHOTO = 10

    companion object{
        val EXTRA_KEY = "address"
        val RESULT_CODE_LOCATION = 12
        val RESULT_CODE_PHOTO = 13
        lateinit var StartDateEvent : ArrayList<Int>
        lateinit var EndDateEvent : ArrayList<Int>
        var startDateCal =  Calendar.getInstance()
        var endDateCal =  Calendar.getInstance()

    }
    //objekt na zistenie adresy
    private lateinit var mLocatorTask : LocatorTask
    private lateinit var graphicOverlay : GraphicsOverlay

    //pole adries
    var addresses : ArrayList<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addEventViewModel =
            ViewModelProviders.of(this).get(AddEventViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_addevent, container, false)


        mapView = root.findViewById(R.id.addEventMapView)
        mapView.locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
        addEventViewModel.map.observe(this, Observer {
            mapView!!.map = it
        })

        startDate = root.findViewById(R.id.inputStartDateEvent)
        startTime = root.findViewById(R.id.inputStartTimeEvent)
        endDate = root.findViewById(R.id.inputEndDateEvent)
        endTime = root.findViewById(R.id.inputEndTimeEvent)
        imageViewPhotoEvent = root.findViewById(R.id.imageViewPhotoEvent)
        StartDateEvent = ArrayList(5)
        for (x in 0..5)StartDateEvent.add(0)
        EndDateEvent = ArrayList(5)
        for (x in 0..5)EndDateEvent.add(0)

        addresses = ArrayList()
        // create a LocatorTask from an online service
        mLocatorTask = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")
        mLocatorTask.loadAsync()
        textLocation = root.findViewById<EditText>(R.id.inputLocation)
        textLocation.setOnClickListener(View.OnClickListener{
            var intent = Intent(activity, FindLocationActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE_LOCATION)
        })

        var buttonAdd = root.findViewById<Button>(R.id.buttonAddEvent)
        buttonAdd.setOnClickListener(View.OnClickListener {
            addEvent(root)
        })
        var buttonAddPhoto = root.findViewById<Button>(R.id.buttonAddPictureEvent)
        buttonAddPhoto.setOnClickListener(View.OnClickListener {
            onClickAddImage(root)
        })

        startDate.setOnClickListener(View.OnClickListener {
            showDatePickerDialog("startDate")
        })
        startTime.setOnClickListener(View.OnClickListener {
            showTimePickerDialog("startTime")
        })

        endDate.setOnClickListener(View.OnClickListener {
            showDatePickerDialog("endDate")
        })
        endTime.setOnClickListener(View.OnClickListener {
            showTimePickerDialog("endTime")
        })

        var scrollView = root.findViewById<NestedScrollView>(R.id.addEventScrollView)
        Map.setMove(root,mapView,scrollView)

        return root
    }

    /**
     * Metoda. ktora uloží udalosť do databazy firebase
     */
    private fun showToast(text:String){
        Toast.makeText(view?.context,text,Toast.LENGTH_SHORT).show()
    }

    private fun addEvent(view:View):Boolean {
        var event = Event()
        val title = view.findViewById<EditText>(R.id.inputTitleEvent).text.toString()
        val details = view.findViewById<EditText>(R.id.inputDetailsEvent).text.toString()
        val startDate = view.findViewById<EditText>(R.id.inputStartDateEvent)
        val startTime = view.findViewById<EditText>(R.id.inputStartTimeEvent)
        val endDate = view.findViewById<EditText>(R.id.inputEndDateEvent)
        val endTime = view.findViewById<EditText>(R.id.inputEndTimeEvent)
        if(title==""){
            showToast("Vložte názov udalosti")
        }else{
            event.title = title
        }
        if (details==""){
            showToast("Vložte informácie o udalosti")
        }else{
            event.details = details
        }
        if (startDate.text.toString()=="" && startTime.text.toString()==""){
            showToast("Vložte dátum a čas začiatku udalosti")
        }else{
            val startTimestamp = startDateCal.time
            event.startDate = startTimestamp
        }
        if (endDate.text.toString()=="" && endTime.text.toString()==""){
            showToast("Vložte dátum a čas konca udalosti")
        }else{
            val startTimestamp = startDateCal.time
            event.startDate = startTimestamp
        }
        val endTimestamp = endDateCal.time
        event.endDate = endTimestamp

        event.coordinates = point
        event.picture = imageUri.toString()
        event.organizer = FirebaseAuth.getInstance().currentUser?.displayName
        event.organizerID = FirebaseAuth.getInstance().currentUser?.uid
        var uri = Uri.parse(event.picture)

        var bitmap = (imageViewPhotoEvent.drawable as BitmapDrawable).bitmap
        
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        val data = baos.toByteArray()
        event.placeName = locationName
        addEventViewModel.saveEventToFirebase(event, data)


        return true
    }

    //Metoda ktora ziskava spravy z inej aktivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==REQUEST_CODE_LOCATION && resultCode == RESULT_CODE_LOCATION){
            var address = data?.getStringExtra(EXTRA_KEY)
            textLocation.setText(address)
            locationName = address

            if(!address.equals("")){
                val geocodeFuture = mLocatorTask.geocodeAsync(address)
                geocodeFuture.addDoneListener(Runnable { kotlin.run {
                    try {
                        val geocodeResults = geocodeFuture.get()
                        if (geocodeResults.size>0){
                            var topResult = geocodeResults.get(0)
                            var loc = topResult.displayLocation
                            //inicializacia point
                            point = GeoPoint(loc.x,loc.y)

                            var att = topResult.attributes
                            var symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                                Color.rgb(255,0,0),20.0f)
                            var geocodeLocation = Graphic(loc,att,symbol)
                            var graphicsOverlay = GraphicsOverlay()
                            mapView.graphicsOverlays.add(graphicsOverlay)
                            mapView.graphicsOverlays.get(0).graphics.add(geocodeLocation)
                            var spatialReference = SpatialReference.create(2229)
                            mapView.run {
                                setViewpointCenterAsync(loc,7000.0)
                            }
                        }
                    }catch (e:Error){
                        Toast.makeText(this.context,e.toString(),Toast.LENGTH_SHORT).show()
                    }
                } })
            }

        }else if(resultCode== Activity.RESULT_OK && requestCode== SELECT_IMAGE){
            imageUri = data?.data
            if (imageUri != null){
                imageViewPhotoEvent.setImageURI(imageUri)
            }
        }
    }

    /**
     * Zobrazi okno s vyberom datumu
     */
    private fun showDatePickerDialog(tag: String) {
        val newFragment = DatePickerFragment()
        newFragment.show(activity!!.supportFragmentManager, tag)
    }

    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // Create a new instance of DatePickerDialog and return it
            return DatePickerDialog(this.context!!,R.style.Dialog, this, year, month, day)
        }

        /***
         *
         */
        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            // Do something with the date chosen by the user
            if (this.tag == "startDate"){
                var text = this.activity?.findViewById<EditText>(R.id.inputStartDateEvent)
                startDateCal.set(year,month,day)
                text?.setText(com.example.trashhunter.DateFormat.getDateFormat(startDateCal))
            }else{
                var text = this.activity?.findViewById<EditText>(R.id.inputEndDateEvent)
                endDateCal.set(year,month,day)
                text?.setText(com.example.trashhunter.DateFormat.getDateFormat(endDateCal))
            }
        }
    }

    private fun showTimePickerDialog(tag: String) {
        var timePicker = TimePickerFragment()

        TimePickerFragment().show(activity!!.supportFragmentManager, tag)
    }

    class TimePickerFragment() : DialogFragment(), TimePickerDialog.OnTimeSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Create a new instance of TimePickerDialog and return it
            return TimePickerDialog(activity,R.style.Dialog, this, hour, minute,
                DateFormat.is24HourFormat(activity))
        }

        @SuppressLint("SetTextI18n")
        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            // Do something with the time chosen by the user
            if (this.tag == "startTime"){
                var text = this.activity?.findViewById<EditText>(R.id.inputStartTimeEvent)
                if (minute < 10){
                    text?.setText("${hourOfDay}:0${minute}")
                }else{
                    text?.setText("${hourOfDay}:${minute}")
                }
                startDateCal.set(Calendar.HOUR_OF_DAY,hourOfDay)
                startDateCal.set(Calendar.MINUTE,minute)
            }else{
                var text = this.activity?.findViewById<EditText>(R.id.inputEndTimeEvent)
                if (minute < 10){
                    text?.setText("${hourOfDay}:0${minute}")
                }else{
                    text?.setText("${hourOfDay}:${minute}")
                }
                endDateCal.set(Calendar.HOUR_OF_DAY,hourOfDay)
                endDateCal.set(Calendar.MINUTE,minute)
            }
        }
    }

    fun onClickAddImage(view : View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(activity?.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
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


}