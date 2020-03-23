package com.example.trashhunter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher

import android.view.Menu

import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters
import com.example.trashhunter.ui.addEvent.AddEventFragment
import kotlinx.android.synthetic.main.activity_map.*

class FindLocationActivity : AppCompatActivity() {

    private lateinit var mLocatorTask : LocatorTask
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

        var text_address = findViewById<TextView>(R.id.text_view_address)

        textLocation = findViewById<AutoCompleteTextView>(R.id.inputEventLocation)
        textLocation.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
                if (mLocatorTask.loadStatus == LoadStatus.LOADED && p0.toString() !=""){
                    if (mLocatorTask.locatorInfo.isSupportsSuggestions){
                        //ziskaj aktualne rozsirenie mapy

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

                setResult(AddEventFragment.RESULT_CODE_LOCATION,intent)
                finish()

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu;
        menuInflater.inflate(R.menu.main, menu)
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
