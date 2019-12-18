package com.example.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.MapView

import android.view.Menu

import android.view.MenuItem
import android.view.View
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast


class MapActivity : AppCompatActivity() {

    var myFlag: Boolean = false
    //deklaracia premennej map view, ktora zobrazuje mapu
    private var mapView: MapView? = null

    private var map: ArcGISMap? = null

    private var clearPlaceList: ListView? = null
    private var myAdapter: ArrayAdapter<*>? = null
    var cities = arrayOf("New York", "Chicago", "Denver", "Detroit", "Las Vegas", "Paris")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById<MapView>(R.id.map1)

        map = ArcGISMap(Basemap.Type.STREETS_VECTOR, 49.201476197, 18.870735168, 11)

        mapView!!.map = map

        clearPlaceList = findViewById<ListView>(R.id.listview)
        clearPlaceList?.setVisibility(View.GONE)

        //pridanie listu do myAdaptera
        myAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities)
        clearPlaceList?.adapter = myAdapter

        clearPlaceList?.setOnItemClickListener { _, _, i, _ -> Toast.makeText(applicationContext, cities[i], Toast.LENGTH_SHORT).show() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu;
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    //metoda na spustenie gps
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
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
        }
        return true
    }

}
