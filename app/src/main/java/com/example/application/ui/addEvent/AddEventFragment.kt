package com.example.application.ui.addEvent

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters
import com.esri.arcgisruntime.util.ListenableList
import com.example.application.Event
import com.example.application.MainActivity
import com.example.application.MapActivity
import com.example.application.R
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.fragment_addevent.*
import java.util.*
import kotlin.collections.ArrayList

class AddEventFragment : Fragment() {

    private lateinit var addEventViewModel: AddEventViewModel
    private lateinit var mapView: MapView
    private lateinit var textLocation: EditText
    private lateinit var map: ArcGISMap
    private var mLocationDisplay: LocationDisplay? = null

    private lateinit var startDate: EditText
    private lateinit var startTime: EditText

    val REQUEST_CODE = 11

    companion object{
        val EXTRA_KEY = "address"
        val RESULT_CODE = 12
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


        addresses = ArrayList()
        // create a LocatorTask from an online service
        mLocatorTask = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")
        mLocatorTask.loadAsync()
        textLocation = root.findViewById<EditText>(R.id.inputLocation)
        textLocation.setOnClickListener(View.OnClickListener{
            var intent = Intent(activity, MapActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE)
        })

        var buttonAdd = root.findViewById<Button>(R.id.buttonAddEvent)
        buttonAdd.setOnClickListener(View.OnClickListener {
            addEvent(root)
        })
        startDate.setOnClickListener(View.OnClickListener {
            showDatePickerDialog(startDate)
        })
        startTime.setOnClickListener(View.OnClickListener {
            showTimePickerDialog(root)
        })

        return root
    }


    private fun addEvent(view:View):Boolean {
        var event = Event()
        event.title = view.findViewById<EditText>(R.id.inputTitleEvent).toString()
        event.details = view.findViewById<EditText>(R.id.inputDetailsEvent).toString()


        addEventViewModel.saveEventToFirebase(event)
        return true
    }

    //Metoda ktora ziskava spravy z inej aktivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==REQUEST_CODE && resultCode == RESULT_CODE){
            var address = data?.getStringExtra(EXTRA_KEY)
            textLocation.setText(address)

            val geocodeFuture = mLocatorTask.geocodeAsync(address)
            geocodeFuture.addDoneListener(Runnable { kotlin.run {
                try {
                    val geocodeResults = geocodeFuture.get()
                    if (geocodeResults.size>0){
                        var topResult = geocodeResults.get(0)
                        var loc = topResult.displayLocation
                        var att = topResult.attributes
                        var symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE,
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
    }

    fun showDatePickerDialog(v: View) {
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
            text?.setText("${day.toString()}.${month.toString()}.${year.toString()}")
        }
    }

    fun showTimePickerDialog(v: View) {
        TimePickerFragment().show(activity!!.supportFragmentManager, "timePicker")
    }

    class TimePickerFragment() : DialogFragment(), TimePickerDialog.OnTimeSetListener {


        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Create a new instance of TimePickerDialog and return it
            return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            // Do something with the time chosen by the user
            var text = this.activity?.findViewById<EditText>(R.id.inputStartTimeEvent)
            text?.setText("${hourOfDay.toString()}:${minute.toString()}")


        }
    }
}