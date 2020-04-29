package com.holubek.trashhunter.ui.posts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.holubek.trashhunter.DateFormat
import com.holubek.trashhunter.R
import com.holubek.trashhunter.Place
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ticket.view.*

/**
 * Trieda zobrazuje príspevky od priateľov
 */
class PostsFragment : Fragment() {

    private lateinit var postsViewModel: PostsViewModel

    lateinit var listPlaces2 : List<Place>
    lateinit var listFriends : List<String>
    lateinit var  listPoints : ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postsViewModel =
            ViewModelProviders.of(this).get(PostsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        listFriends = listOf()

        postsViewModel.getPlaces().observe(this, Observer {
            listPlaces2 = it
            updateList(root)
        })

        val buttonMyPlaces = root.findViewById<Button>(R.id.button_to_my_places)
        buttonMyPlaces.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_myPlacesFragment)
        }
        return root
    }

    /**
     * Nastavenie zoznamu príspevkov
     * @param view objekt View, ktorý reprezentuje obrazovku na zobrazovanie príspevkov od priateľov
     */
    fun updateList(view : View){
        kotlin.run {
            listPoints = view.findViewById<ListView>(R.id.lvPoints)
            var myPlaceAdapter= placeAdapter(view.context,listPlaces2)
            listPoints.adapter=myPlaceAdapter
        }

    }

    /**
     * trieda na naplnenie ListView príspevkov
     */
    inner class placeAdapter:BaseAdapter{
        var listFoodAdapter : List<Place>
        var context:Context?=null
        constructor(context:Context, listPlaceAdapter: List<Place>):super(){
            this.listFoodAdapter=listPlaceAdapter
            this.context=context
        }

        /**
         * Vytvorí View, v ktorom naplní údaje o príspevku
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var placeView=layoutInflater.inflate(R.layout.ticket,null)
            placeView.button_delete_place.visibility = View.GONE
            var currentPlace=listFoodAdapter[position]
            placeView.valueUser.text=currentPlace.userName

            placeView.textViewDescription.text=currentPlace.ClearText.toString()
            placeView.valuePlaceName.text=currentPlace.placeName.toString()
            placeView.valueDate.text = DateFormat.getDateFormat(currentPlace.date!!)

            val imageBeforeView = placeView.findViewById<ImageView>(R.id.imageTicketBefore)

            val ratingBar =placeView.findViewById<RatingBar>(R.id.rating_bar)

            if (currentPlace.rating != 0F){
                ratingBar.rating = currentPlace.rating!!.div(currentPlace.countOfRating!!)
                var rating = ""
                when(currentPlace.countOfRating){
                    1 -> rating = "Hodnotenie"
                    2,3,4 -> rating = "Hodnotenia"
                    else -> "Hodnotení"
                }
                placeView.textViewRating.text = currentPlace.countOfRating.toString() + " " + rating
            }

            placeView.setOnClickListener(View.OnClickListener {
                val bundle = Bundle()

                bundle.putString("POINT_ID",currentPlace.pointID.toString())
                findNavController().navigate(R.id.action_nav_home_to_place,bundle)
            })

            //Referencia na obrázok v úložisku Firebase
            var photoBeforeRef = FirebaseStorage.getInstance()
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