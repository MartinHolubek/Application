package com.holubek.trashhunter.ui.event

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.holubek.trashhunter.Comment
import com.holubek.trashhunter.DateFormat
import com.holubek.trashhunter.Event
import com.holubek.trashhunter.Map
import com.holubek.trashhunter.R
import com.holubek.trashhunter.firebase.FirebaseRepository
import com.holubek.trashhunter.firebase.FirebaseStorage
import java.util.*


private const val ARG_PARAM1 = "EVENT_ID"
private const val ARG_PARAM2 = "ORGANIZER_ID"



/**
 * Trieda na zobrazenie detailov o udalosti
 */
class EventFragment : Fragment() {
    private var eventID: String? = null
    private var organizerID: String? = null
    private lateinit var eventViewModel: EventViewModel
    private lateinit var firebaseRepository: FirebaseRepository
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var event : Event
    private lateinit var mapView:MapView
    private lateinit var comments: List<Comment>
    private lateinit var viewPagerComments: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventID = it.getString(ARG_PARAM1)
            organizerID = it.getString(ARG_PARAM2)
        }
        firebaseStorage = FirebaseStorage()
        firebaseRepository = FirebaseRepository()
    }

    /**
     * Nastavenie údajov na zobrazenie
     */
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
            var commentText = ""
            if (!comment.text.toString().equals(getString(R.string.Comment))){
                commentText = comment.text.toString()
            }
            eventViewModel.saveRating(event.id.toString(),event.organizerID.toString(),
                event.countOfRating!!, event.rating!!,ratingbarPlace.rating,commentText)
        })

        buttonParticipants.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("EVENT_ID",eventID)
            bundle.putString("ORGANIZER_ID",organizerID)
            findNavController().navigate(R.id.action_eventFragment_to_joinedUsersFragment, bundle)
        }

        return root
    }

    /**
     * Nastavenie údajov na zobrazenie
     * @param root objekt View v ktorý reprezentuje detail udalosti
     */
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

        var ratingBar = root?.findViewById<AppCompatRatingBar>(R.id.ratingPlaceEvent)
        var ratingCount = root?.findViewById<TextView>(R.id.countRatingTextEvent)
        if (event.rating != 0F){
            ratingBar?.rating = event.rating!!.div(event.countOfRating!!)
            ratingCount?.text = event.countOfRating.toString() + " Hodnotení"
        }

        details?.text = event.details
        location?.text = event.placeName.toString()
        coordination?.text = "${event.coordinates!!.latitude.toString()}, ${event.coordinates!!.longitude.toString()}"
    }

    /**
     * Nastavenie zoznamu komentárov
     * @param root objekt View, ktorý reprezentuje obrazovku detail udalosti
     */
    private fun updateComments(root: View) {
        var adapter = AdapterComments(comments,root.context)
        viewPagerComments.adapter = adapter
        viewPagerComments.setPadding(130,0,130,0)
    }

    /**
     * Zobrazenie lokality na mape, kde sa uskutoční udalosť
     *
     */
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    /**
     * Trieda Adapter na zobrazovanie komentárov
     */
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

        /**
         * Nastaví View komentára a naplní ho hodnotami
         */
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
