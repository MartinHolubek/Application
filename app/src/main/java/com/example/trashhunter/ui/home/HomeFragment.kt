package com.example.trashhunter.ui.home

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.trashhunter.DateFormat
import com.example.trashhunter.R
import com.example.trashhunter.Place
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.ticket.view.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    lateinit var listPlaces2 : List<Place>
    lateinit var listFriends : List<String>
    lateinit var  listPoints : ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        listFriends = listOf()

        homeViewModel.getFriends().observe(this, Observer {
            listPlaces2 = it
            updateList2(root)
        })

        val buttonMyPlaces = root.findViewById<Button>(R.id.button_to_my_places)
        buttonMyPlaces.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_myPlacesFragment)
        }
        return root
    }

    fun updateList2(view : View){
        kotlin.run {
            listPoints = view.findViewById<ListView>(R.id.lvPoints)
            var myPlaceAdapter= placeAdapter2(view.context,listPlaces2)
            listPoints.adapter=myPlaceAdapter
        }

    }

    inner class placeAdapter2:BaseAdapter{
        var listFoodAdapter : List<Place>
        var context:Context?=null
        constructor(context:Context, listPlaceAdapter: List<Place>):super(){
            this.listFoodAdapter=listPlaceAdapter
            this.context=context
        }

        /**
         * Vytvorí
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var placeView=layoutInflater.inflate(R.layout.ticket,null)
            var currentPlace=listFoodAdapter[position]
            placeView.valueUser.text=currentPlace.userName
            //placeView.valueFoto.setImageURI()
            placeView.textViewDescription.text=currentPlace.ClearText.toString()
            placeView.valuePlaceName.text=currentPlace.placeName.toString()
            placeView.valueDate.text = DateFormat.getDateFormat(currentPlace.date!!)

            val imageBeforeView = placeView.findViewById<ImageView>(R.id.imageTicketBefore)
            var ratingValue:Float
            val ratingbar =placeView.findViewById<RatingBar>(R.id.rating_bar)


            ratingbar.rating = 2F
            placeView.textViewRating.text = currentPlace.countOfRating.toString() + " Hodnotení"

            placeView.setOnClickListener(View.OnClickListener {
                val bundle = Bundle()

                bundle.putString("POINT_ID",currentPlace.pointID.toString())
                findNavController().navigate(R.id.action_nav_home_to_place,bundle)
            })

            //Referencia na obrázok v úložisku Firebase
            var photoBeforeRef = FirebaseStorage.getInstance()
                .reference
                .child(currentPlace.photoBefore.toString())


            val ONE_MEGABYTE: Long = 1024 * 1024
            photoBeforeRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                // Konvertujeme byteArray na bitmap
                var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                //imageBeforeView.setImageBitmap(Bitmap.createScaledBitmap(bmp, imageBeforeView.width,imageBeforeView.height,false))
                imageBeforeView.setImageBitmap(bmp)
            }.addOnFailureListener {
                // Handle any errors
            }

            return placeView
        }

        /**
         * @param pozicia prvku v liste
         */
        override fun getItem(position: Int): Any {
            return listFoodAdapter[position]
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
            return listFoodAdapter.size
        }
    }
}