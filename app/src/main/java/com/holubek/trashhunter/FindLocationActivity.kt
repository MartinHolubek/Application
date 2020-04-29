package com.holubek.trashhunter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters
import com.holubek.trashhunter.ui.addEvent.AddEventFragment
import kotlinx.android.synthetic.main.activity_find_location.*

/**
 * Trieda na vyhľadanie lokality podľa textového reťazca
 */
class FindLocationActivity : AppCompatActivity() {

    private lateinit var mLocatorTask : LocatorTask
    private lateinit var textLocation: AutoCompleteTextView
    private lateinit var textViewAddress: ListView
    private var isSet: Boolean? = null

    //zoznam adries
    var addresses : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_location)
        addresses = ArrayList()

        mLocatorTask = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")
        mLocatorTask.loadAsync()

        textLocation = findViewById<AutoCompleteTextView>(R.id.inputEventLocation)
        textLocation.setOnClickListener {
            if (isSet == true){
                isSet = false
            }
        }

        setSuggestions(textLocation)

        var buttonBack = findViewById<Button>(R.id.buttonBackEvent)
        buttonBack.setOnClickListener (View.OnClickListener{
            var intent = Intent()
            intent.putExtra(AddEventFragment.EXTRA_KEY,inputEventLocation.text.toString())

            setResult(AddEventFragment.RESULT_CODE_LOCATION,intent)
            finish()
        })
    }

    /**
     * Nastavenie zhodných lokalít podľa textu
     * @param textLocation objekt so vstupným textom na vyhľadanie lokalít
     */
    private fun setSuggestions(textLocation: AutoCompleteTextView){
        textLocation.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
                if(isSet != true){
                    if (mLocatorTask.loadStatus == LoadStatus.LOADED && p0.toString() !=""){
                        if (mLocatorTask.locatorInfo.isSupportsSuggestions){
                            //ziskaj aktualne rozsirenie mapy

                            //Obmedzte vyhľadávanie na tento rozsah máp a nie viac ako 10 návrhov
                            var suggestParams = SuggestParameters()
                            suggestParams.countryCode = "SVK"

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

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                /*if (mLocatorTask.loadStatus == LoadStatus.LOADED && p0.toString() !=""){
                    if (mLocatorTask.locatorInfo.isSupportsSuggestions){
                        //ziskaj aktualne rozsirenie mapy

                        //Obmedzte vyhľadávanie na tento rozsah máp a nie viac ako 10 návrhov
                        var suggestParams = SuggestParameters()
                        suggestParams.countryCode = "SVK"

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
                }*/
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                /*if (mLocatorTask.loadStatus == LoadStatus.LOADED && p0.toString() !=""){
                    if (mLocatorTask.locatorInfo.isSupportsSuggestions){
                        //ziskaj aktualne rozsirenie mapy

                        //Obmedzte vyhľadávanie na tento rozsah máp a nie viac ako 10 návrhov
                        var suggestParams = SuggestParameters()
                        suggestParams.countryCode = "SVK"

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
                                //showSuggestions()

                            }catch (e: InterruptedException){

                            }
                        } })
                    }
                }*/
            }
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
        textViewAddress = findViewById<ListView>(R.id.text_view_address)
        var adapter = TextAdapter(this, addresses!!)
        textLocation.setThreshold(2)
        textViewAddress.setAdapter(adapter)
        textViewAddress.dividerHeight = 10
    }

    /**
     * trieda na naplnenie ListView lokalitami
     */
    inner class TextAdapter: BaseAdapter {
        var listAdapter : ArrayList<String>
        var context: Context?=null
        constructor(context: Context, listAdapter: ArrayList<String>):super(){
            this.listAdapter= listAdapter
            this.context= context
        }

        /**
         * Vytvorí View, v ktorom naplní údaje o udalosti
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var textView = TextView(context)
            textView.text = listAdapter[position]
            textView.textSize = 18F
            textView.setTextColor(getColor(R.color.textColor))
            textView.setOnClickListener {
                textLocation.setText(textView.text)
                textViewAddress.adapter = null
                isSet = true
            }

            return textView
        }

        /**
         * @param pozicia prvku v liste
         */
        override fun getItem(position: Int): Any {
            return listAdapter[position]
        }
        /**
         * @param pozicia prvku v liste
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
        /**
         * vrati velkost listu
         */
        override fun getCount(): Int {
            return listAdapter.size
        }
    }


}
