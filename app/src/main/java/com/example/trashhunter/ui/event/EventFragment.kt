package com.example.trashhunter.ui.event

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.example.trashhunter.Comment
import com.example.trashhunter.DateFormat
import com.example.trashhunter.Event
import com.example.trashhunter.Map
import com.example.trashhunter.R
import com.example.trashhunter.firebase.FirebaseRepository
import com.example.trashhunter.firebase.FirebaseStorage
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "EVENT_ID"
private const val ARG_PARAM2 = "ORGANIZER_ID"

private lateinit var eventViewModel: EventViewModel
private lateinit var firebaseRepository: FirebaseRepository
private lateinit var firebaseStorage: FirebaseStorage
private lateinit var event : Event
private lateinit var mapView:MapView
private lateinit var comments: List<Comment>
private lateinit var viewPagerComments: ViewPager

/**
 * A simple [Fragment] subclass.
 * Use the [EventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var eventID: String? = null
    private var organizerID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventID = it.getString(ARG_PARAM1)
            organizerID = it.getString(ARG_PARAM2)
        }
        firebaseStorage = FirebaseStorage()
        firebaseRepository = FirebaseRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eventViewModel =
            ViewModelProviders.of(this).get(EventViewModel::class.java)
        // Inflate the layout for this fragment
        var root = inflater.inflate(R.layout.fragment_event, container, false)
        var buttonParticipants = root.findViewById<Button>(R.id.buttonParticiants)
        var scrollView = root.findViewById<NestedScrollView>(R.id.eventScrollView)
        var buttonRateEvent = root.findViewById<AppCompatButton>(R.id.buttonEventRate)

        viewPagerComments = root.findViewById(R.id.viewPagerComments)
        mapView = root.findViewById<MapView>(R.id.eventMap)
        mapView!!.map = ArcGISMap(Basemap.Type.STREETS_VECTOR, 49.201476197, 18.870735168, 11)

        Map.setMove(root, mapView,scrollView)
        eventViewModel.getPlace(eventID!!, organizerID!!).observe(this, Observer { it ->
            event = it
            updateEvent(root)
        })

        eventViewModel.getComments(eventID.toString(),organizerID.toString()).observe(this, Observer {
            comments = it
            updateComments(root)
        })


        buttonRateEvent.setOnClickListener(View.OnClickListener {

            var ratingbarPlace = root.findViewById<RatingBar>(R.id.eventRatingBar)
            var comment = root.findViewById<AppCompatEditText>(R.id.eventTextRating)

            eventViewModel.saveRating(event.id.toString(),event.organizerID.toString(),
                event.countOfRating!!, event.rating!!,ratingbarPlace.rating,comment.text.toString())
        })


        buttonParticipants.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("EVENT_ID",eventID)
            bundle.putString("ORGANIZER_ID",organizerID)
            findNavController().navigate(R.id.action_eventFragment_to_joinedUsersFragment, bundle)

        }

        return root
    }

    private fun updateEvent(root: View?) {
        firebaseStorage.getImage(event.picture!!).addOnSuccessListener {
            var image = root?.findViewById<ImageView>(R.id.event_image)
            var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            image?.setImageBitmap(bmp)
        }
        updateMap()
        var title = root?.findViewById<TextView>(R.id.eventTitle)
        var date = root?.findViewById<TextView>(R.id.eventDate)
        var endDate = root?.findViewById<TextView>(R.id.eventEndDate)
        var endDateLabel = root?.findViewById<TextView>(R.id.eventEndDateLabel)
        var details = root?.findViewById<TextView>(R.id.eventDetails)
        var location = root?.findViewById<TextView>(R.id.eventLocation)
        var coordination = root?.findViewById<TextView>(R.id.eventCoordination)
        var organizer = root?.findViewById<TextView>(R.id.eventOrganizer)


        title?.text = event.title
        organizer?.text = event.organizer
        date?.text = DateFormat.getDateTimeFormat(event.startDate!!)
        if (event.endDate != null){
            endDate?.text = DateFormat.getDateTimeFormat(event.endDate!!)
        }else{
            endDate?.visibility = View.GONE
            endDateLabel?.visibility = View.GONE
        }
        details?.text = event.details
        location?.text = event.placeName.toString()
        coordination?.text = "${event.coordinates!!.latitude.toString()}, ${event.coordinates!!.longitude.toString()}"
    }
    private fun updateComments(root: View) {
        var adapter = AdapterComments(comments,root.context)

        viewPagerComments.adapter = adapter
        viewPagerComments.setPadding(130,0,130,0)

    }

    private fun updateMap() {
        val pt = Point(event.coordinates!!.latitude,event.coordinates!!.longitude, SpatialReference.create(4326))
        val mySymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 18.0F)

        val myGraphic = Graphic(pt, mySymbol)

        val myGraphicsOverlay = GraphicsOverlay()

        myGraphicsOverlay.graphics.add(myGraphic)
        mapView.graphicsOverlays.add(myGraphicsOverlay)
        mapView.map.initialViewpoint = Viewpoint(pt, 40.0)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    inner class AdapterComments : PagerAdapter {

        private lateinit var models:List<Comment>
        private lateinit var context: Context

        constructor(models: List<Comment>, context: Context) : super() {
            this.models = models
            this.context = context
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view.equals(`object`)
        }

        override fun getItemPosition(`object`: Any): Int {
            var i = 0
            while (i < count){
                if (`object` as Comment == models[i]){
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
            var view = layoutInflater.inflate(R.layout.ticket_comment,null)
            var currentComment = models[position]
            var text= view.findViewById<TextView>(R.id.commentText)
            var textUserName=  view.findViewById<TextView>(R.id.commentUsername)
            var date = view.findViewById<TextView>(R.id.commentDate)
            var ratingBar= view.findViewById<RatingBar>(R.id.commentRating)

            text.setText(currentComment.comment)
            textUserName.setText(currentComment.userName)
            ratingBar.rating = currentComment.rating!!
            date.text = DateFormat.getDateFormat(Date(currentComment.date?.time!!))

            container.addView(view,0)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)

        }

    }
}
