package com.example.trashhunter.ui.myPlaces

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.RatingBar
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.trashhunter.DateFormat
import com.example.trashhunter.Place

import com.example.trashhunter.R
import com.example.trashhunter.firebase.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ticket.view.textViewDescription
import kotlinx.android.synthetic.main.ticket.view.textViewRating
import kotlinx.android.synthetic.main.ticket.view.valueDate
import kotlinx.android.synthetic.main.ticket.view.valuePlaceName
import kotlinx.android.synthetic.main.ticket.view.valueUser
import kotlinx.android.synthetic.main.ticket_my_place.view.*

class MyPlacesFragment : Fragment() {

    companion object {
        fun newInstance() =
            MyPlacesFragment()
    }
    private lateinit var lvPlaces:ListView
    private lateinit var listPlaces:List<Place>


    private lateinit var viewModel: MyPlacesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root = inflater.inflate(R.layout.my_places_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(MyPlacesViewModel::class.java)
        // TODO: Use the ViewModel

        lvPlaces = root.findViewById<ListView>(R.id.lvMyPlaces)
        viewModel.getPlaces().observe(this, Observer {
            listPlaces = it

            updateList(root)
        })

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }

    fun updateList(view : View){
        kotlin.run {
            var myPlaceAdapter= placeAdapter(view.context,listPlaces)
            lvPlaces.adapter=myPlaceAdapter
        }
    }

    inner class placeAdapter: BaseAdapter {
        var listPlaceAdapter : List<Place>
        var context: Context?=null
        constructor(context: Context, listPlaceAdapter: List<Place>):super(){
            this.listPlaceAdapter=listPlaceAdapter
            this.context=context
        }

        /**
         * Vytvorí
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var placeView=layoutInflater.inflate(R.layout.ticket,null)
            var currentPlace=listPlaceAdapter[position]
            placeView.valueUser.text=currentPlace.userName
            //placeView.valueFoto.setImageURI()
            placeView.textViewDescription.text=currentPlace.ClearText.toString()
            placeView.valuePlaceName.text=currentPlace.placeName.toString()
            placeView.valueDate.text = DateFormat.getDateFormat(currentPlace.date!!)

            val imageBeforeView = placeView.findViewById<ImageView>(R.id.imageTicketBefore)
            var ratingValue:Float
            val ratingBar =placeView.findViewById<RatingBar>(R.id.rating_bar)

            if (currentPlace.rating != 0F){
                ratingBar.rating = currentPlace.rating!!.div(currentPlace.countOfRating!!)
                placeView.textViewRating.text = currentPlace.countOfRating.toString() + " Hodnotení"
            }
            placeView.textViewRating.text = currentPlace.countOfRating.toString() + " Hodnotení"

            placeView.setOnClickListener(View.OnClickListener {
                val bundle = Bundle()
                bundle.putString("POINT_ID",currentPlace.pointID.toString())
                findNavController().navigate(R.id.action_myPlacesFragment_to_nav_place,bundle)
            })

            placeView.button_delete_place.setOnClickListener {
                viewModel.deletePlace(listPlaceAdapter[position])
            }
            //Referencia na obrázok v úložisku Firebase
            var photoBeforeRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                .reference
                .child(currentPlace.photoBefore.toString())
            photoBeforeRef.downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(imageBeforeView)
            }

            return placeView
        }

        /**
         * @param pozicia prvku v liste
         */
        override fun getItem(position: Int): Any {
            return listPlaceAdapter[position]
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
            return listPlaceAdapter.size
        }
    }

}
