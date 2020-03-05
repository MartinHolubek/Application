package com.example.application.ui.place

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.application.Picture
import com.example.application.Place

import com.example.application.R
import com.google.firebase.storage.FirebaseStorage

class PlaceFragment : Fragment() {

    companion object {
        fun newInstance() = PlaceFragment()
    }
    private lateinit var viewPager:ViewPager
    private lateinit var argbEvaluator: ArgbEvaluator
    private lateinit var models : ArrayList<Picture>
    private lateinit var place: Place
    private lateinit var placeViewModel: PlaceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container?.removeAllViews()
        placeViewModel =
            ViewModelProviders.of(this).get(PlaceViewModel::class.java)
        val root = inflater.inflate(R.layout.place_fragment, container, false)
        viewPager = root.findViewById<ViewPager>(R.id.viewPager)



        var placeID = arguments?.getString("POINT_ID")

        placeViewModel.getPlace(placeID.toString()).observe(this, Observer { it ->
            place = it
        })
        argbEvaluator = ArgbEvaluator()
        models = ArrayList<Picture>()
        //Referencia na obrázok v úložisku Firebase
        var photoBeforeRef = FirebaseStorage.getInstance()
            .reference
            .child("/Pictures/Qs3QLZEy78SM3DPRFHEYf54TSZx2/pictureBefore_20200305_185143")

        val ONE_MEGABYTE: Long = 1024 * 1024
        photoBeforeRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            // Konvertujeme byteArray na bitmap
            var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            models.add(Picture(bmp,"Fotka pred"))
        }.addOnFailureListener {
            // Handle any errors
        }
        var photoAfterRef = FirebaseStorage.getInstance()
            .reference
            .child("/Pictures/Qs3QLZEy78SM3DPRFHEYf54TSZx2/pictureAfter_20200305_185143")
        photoAfterRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            // Konvertujeme byteArray na bitmap
            var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            models.add(Picture(bmp,"Fotka po"))

            var adapter = Adapter(models,root.context)

            viewPager.adapter = adapter
            viewPager.setPadding(130,0,130,0)
        }.addOnFailureListener {
            // Handle any errors
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        placeViewModel = ViewModelProviders.of(this).get(PlaceViewModel::class.java)
        // TODO: Use the ViewModel
    }

    inner class Adapter : PagerAdapter{

        private lateinit var models:List<Picture>
        private lateinit var context: Context

        constructor(models: List<Picture>, context: Context) : super() {
            this.models = models
            this.context = context
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view.equals(`object`)
        }

        override fun getItemPosition(`object`: Any): Int {
            var i = 0
            while (i < count){
                if (`object` as Picture == models[i]){
                    return i
                }
                i++
            }
            return POSITION_NONE
        }

        override fun getCount(): Int {
            return models.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var view = layoutInflater.inflate(R.layout.ticket_picture,null)
            var currentPicture = models[position]
            var imageView:ImageView
            var title:TextView

            imageView = view.findViewById(R.id.imageTicketPlace)
            title = view.findViewById<TextView>(R.id.pictureTitle)
            imageView.setImageBitmap(currentPicture.image)
            title.setText(currentPicture.title)


            container.addView(view,0)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }

}
