package com.example.application

import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView

import android.view.Menu

import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters
import com.example.application.ui.addEvent.AddEventFragment
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.ticket.view.*

class MapActivity : AppCompatActivity() {

    private lateinit var mLocatorTask : LocatorTask
    private lateinit var graphicOverlay : GraphicsOverlay
    private lateinit var mapView: MapView
    private lateinit var textLocation: AutoCompleteTextView

    //pole adries
    var addresses : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        addresses = ArrayList()
        //mapView = findViewById(R.id.mapEvent)
        mLocatorTask = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")
        mLocatorTask.loadAsync()

        textLocation = findViewById<AutoCompleteTextView>(R.id.inputEventLocation)
        textLocation.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (mLocatorTask.loadStatus == LoadStatus.LOADED && p0.toString() !=""){
                    if (mLocatorTask.locatorInfo.isSupportsSuggestions){
                        //ziskaj aktualne rozsirenie mapy
                        //var currentExtent = mapView!!.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).targetGeometry

                        //Obmedzte vyhľadávanie na tento rozsah máp a nie viac ako 10 návrhov
                        var suggestParams = SuggestParameters()
                        //suggestParams.searchArea = currentExtent
                        suggestParams.maxResults = 10

                        //Ziskanie navrhov zo vstupu uzivatela
                        var suggestionsFuture = mLocatorTask.suggestAsync(p0.toString(),suggestParams)
                        suggestionsFuture.addDoneListener(Runnable { kotlin.run {
                            try {
                                var suggestResults = suggestionsFuture.get()
                                addresses!!.clear()
                                for (result in suggestResults){
                                    addresses!!.add(result.label)
                                }
                                showSuggestions()

                            }catch (e: InterruptedException){

                            }
                        } })
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        var buttonBack = findViewById<Button>(R.id.buttonBackEvent)
        buttonBack.setOnClickListener (View.OnClickListener{
                var intent = Intent()
                intent.putExtra(AddEventFragment.EXTRA_KEY,inputEventLocation.text.toString())

                setResult(AddEventFragment.RESULT_CODE,intent)
                finish()

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu;
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    //metoda na spustenie gps
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /*when (item.getItemId()) {
            R.id.gps1 ->
                map?.addDoneLoadingListener(Runnable {
                    //zatvorí menu s miestami
                    clearPlaceList?.setVisibility(View.GONE)
                    myFlag = false
                    //vytvoreny lokator, ktorý zobrazuje na mape polohu zariadenia
                    var myLocation = mapView!!.locationDisplay as LocationDisplay
                    if (myLocation.isStarted){
                        myLocation.stop()
                    }else{
                        myLocation.autoPanMode = LocationDisplay.AutoPanMode.COMPASS_NAVIGATION
                        myLocation.startAsync()
                        myLocation.initialZoomScale = 2000.0
                        println( " old: " + mapView!!.locationDisplay.initialZoomScale.toString())
                        println("new: " + myLocation.initialZoomScale)
                    }
                })
            R.id.menuItem ->
                if (myFlag) {
                    clearPlaceList?.setVisibility(View.GONE)
                    myFlag = false
                }else {
                    clearPlaceList?.setVisibility(View.VISIBLE)
                    myFlag = true
                }
        }*/
        return true
    }

    /**
     * Naplní adapter pre zobrazenie vyhladaných miest podla textu
     */
    fun showSuggestions(){
        var adapter : ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_list_item_1,
            addresses!!.toList())
        textLocation.setAdapter(adapter)
    }


}
