package com.example.application.ui.addEvent

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.example.application.MainActivity
import com.example.application.MapActivity
import com.example.application.R

class AddEventFragment : Fragment() {

    private lateinit var addEventViewModel: AddEventViewModel
    private lateinit var mapView: MapView
    private lateinit var textLocation: EditText
    private lateinit var map: ArcGISMap
    private var mLocationDisplay: LocationDisplay? = null

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


        addresses = ArrayList()
        // create a LocatorTask from an online service
        mLocatorTask = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")
        mLocatorTask.loadAsync()
        textLocation = root.findViewById<EditText>(R.id.inputLocation)
        textLocation.setOnClickListener(View.OnClickListener{
            var intent = Intent(activity, MapActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE)
        })

        return root
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
}