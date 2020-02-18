package com.example.application.ui.home

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.application.R
import com.example.application.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.ticket.view.*
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {


    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null
    private lateinit var homeViewModel: HomeViewModel

    private var myDataset : ArrayList<String> = ArrayList()
    var listFood =  ArrayList<Place>()
    lateinit var  listPoints : ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })
        homeViewModel.list.observe(this, Observer {
            listFood = it
            updateList(root)
        })
        return root
    }

    fun updateList(view : View){

        listPoints = view.findViewById<ListView>(R.id.lvPoints)
        var myfoodAdapter= foodAdapter(view.context,listFood)
        listPoints.adapter=myfoodAdapter
    }

    inner class foodAdapter:BaseAdapter{
        var listFoodAdapter= java.util.ArrayList<Place>()
        var context:Context?=null
        constructor(context:Context, listPlaceAdapter: java.util.ArrayList<Place>):super(){
            this.listFoodAdapter=listPlaceAdapter
            this.context=context
        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var placeView=layoutInflater.inflate(R.layout.ticket,null)
            var currentPlace=listFoodAdapter[position]
            placeView.textViewName.text=currentPlace.userName
            //placeView.valueFoto.setImageURI()
            placeView.valueFoto.text=currentPlace.photo.toString()
            placeView.valueClearText.text=currentPlace.ClearText.toString()
            placeView.valuePlaceName.text=currentPlace.placeName.toString()
            placeView.valueDate.text=currentPlace.date.toString()

            // Reference to an image file in Cloud Storage
            val storageReference = FirebaseStorage.getInstance().reference

            // ImageView in your Activity
            val imageView = placeView.findViewById<ImageView>(R.id.imageTicket)

            /*// Download directly from StorageReference using Glide
            // (See MyAppGlideModule for Loader registration)
            Glide.with(context!!)
                .load(storageReference)
                .into(imageView)*/
            var islandRef = storageReference.child(currentPlace.photo.toString())

            val ONE_MEGABYTE: Long = 1024 * 1024
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                // Konvertujeme byteArray na bitmap
                var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, imageView.width,imageView.height,false))


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