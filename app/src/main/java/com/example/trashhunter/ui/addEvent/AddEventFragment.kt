package com.example.trashhunter.ui.addEvent

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.example.trashhunter.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
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

    private lateinit var startDate: EditText
    private lateinit var startTime: EditText
    private lateinit var imageViewPhotoEvent: ImageView

    private lateinit var point: GeoPoint

    val REQUEST_CODE_LOCATION = 11
    val REQUEST_CODE_PHOTO = 10

    companion object{
        val EXTRA_KEY = "address"
        val RESULT_CODE_LOCATION = 12
        val RESULT_CODE_PHOTO = 13
        lateinit var localDate : ArrayList<Int>

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
        container?.removeAllViews()
        addEventViewModel =
            ViewModelProviders.of(this).get(AddEventViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_addevent, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)

        mapView = root.findViewById<MapView>(R.id.mapEvent)
        mapView.locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
        addEventViewModel.text.observe(this, Observer {
            textView.text = it
        })
        addEventViewModel.map.observe(this, Observer {
            mapView!!.map = it
        })

        startDate = root.findViewById(R.id.inputStartDateEvent)
        startTime = root.findViewById(R.id.inputStartTimeEvent)
        imageViewPhotoEvent = root.findViewById(R.id.imageViewPhotoEvent)
        localDate = ArrayList()

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
            showDatePickerDialog(root)
        })
        startTime.setOnClickListener(View.OnClickListener {
            showTimePickerDialog(root)
        })

        return root
    }

    /**
     * Metoda. ktora uloží udalosť do databazy firebase
     */

    private fun addEvent(view:View):Boolean {
        var event = Event()
        event.title = view.findViewById<EditText>(R.id.inputTitleEvent).text.toString()
        event.details = view.findViewById<EditText>(R.id.inputDetailsEvent).text.toString()
        var date = GregorianCalendar(localDate[0],localDate[1],localDate[2],localDate[3],localDate[4]).time
        event.startDate = Timestamp(date)
        event.coordinates = point
        event.picture = imageUri.toString()

        addEventViewModel.saveEventToFirebase(event)
        return true
    }

    //Metoda ktora ziskava spravy z inej aktivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==REQUEST_CODE_LOCATION && resultCode == RESULT_CODE_LOCATION){
            var address = data?.getStringExtra(EXTRA_KEY)
            textLocation.setText(address)

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
    private fun showDatePickerDialog(v: View) {
        val newFragment = DatePickerFragment()
        newFragment.show(activity!!.supportFragmentManager, "datePicker")
    }

    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {


        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // Create a new instance of DatePickerDialog and return it
            return DatePickerDialog(this.context!!, this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            // Do something with the date chosen by the user
            var text = this.activity?.findViewById<EditText>(R.id.inputStartDateEvent)
            text?.setText("${month+1} ${day}, ${year}")

            localDate.add(0,year)
            localDate.add(1,month)
            localDate.add(2,day)
        }
    }

    private fun showTimePickerDialog(v: View) {
        TimePickerFragment().show(activity!!.supportFragmentManager, "timePicker")
    }

    class TimePickerFragment() : DialogFragment(), TimePickerDialog.OnTimeSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Create a new instance of TimePickerDialog and return it
            return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            // Do something with the time chosen by the user
            var text = this.activity?.findViewById<EditText>(R.id.inputStartTimeEvent)
            text?.setText("${hourOfDay}:${minute}")
            localDate.add(3,hourOfDay)
            localDate.add(4,minute)

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