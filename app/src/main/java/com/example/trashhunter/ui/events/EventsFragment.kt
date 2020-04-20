package com.example.trashhunter.ui.events

import android.content.Context
import android.graphics.Bitmap
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
import com.example.trashhunter.Event
import com.example.trashhunter.R
import com.example.trashhunter.ui.attendEvents.AttendEventsViewModel
import com.example.trashhunter.ui.attendEvents.attendEventsFragment
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ticket_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class EventsFragment : Fragment() {

    private lateinit var eventsViewModel: EventsViewModel
    private lateinit var attendEventsViewModel: AttendEventsViewModel

    lateinit var  listViewEvents : ListView
    lateinit var listEvents : ArrayList<Event>
    lateinit var listAttendEvents : ArrayList<Event>
    private var attend = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eventsViewModel =
            ViewModelProviders.of(this).get(EventsViewModel::class.java)
        attendEventsViewModel = ViewModelProviders.of(this).get(AttendEventsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_events, container, false)

        listEvents = arrayListOf()
        listAttendEvents = arrayListOf()
        var button = root.findViewById<Button>(R.id.button_to_my_events)
        button.setOnClickListener {
            findNavController().navigate(R.id.action_nav_events_to_myEventsFragment)
        }

        attendEventsViewModel.getSavedEvents().observe(this, Observer {
            listAttendEvents = ArrayList(it)
            updateList(root)
        })

        var buttonAttend = root.findViewById<Button>(R.id.button_to_attend_events)
        buttonAttend.setOnClickListener {
            findNavController().navigate(R.id.action_nav_events_to_attendEventsFragment)
        }

        eventsViewModel.getSavedEvents2().observe(this, Observer {

            listEvents = ArrayList(it)

            updateList(root)

        })
        return root
    }

    fun updateList(view : View){
        listViewEvents = view.findViewById<ListView>(R.id.lvEvents)
        var myFriendsAdapter= friendsAdapter(view.context,listEvents)
        listViewEvents.adapter=myFriendsAdapter
    }

    inner class friendsAdapter: BaseAdapter {
        var listEventAdapter : ArrayList<Event>
        var context: Context?=null
        constructor(context: Context, listEventAdapter: ArrayList<Event>):super(){
            this.listEventAdapter=listEventAdapter
            this.context=context
        }

        /**
         * Vytvorí
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var eventView=layoutInflater.inflate(R.layout.ticket_event,null)
            var currentEvent=listEventAdapter[position]
            eventView.textViewEventStartDate.text = DateFormat.getDateTimeFormat(currentEvent.startDate!!)
            eventView.textViewEventTitle.text = currentEvent.title.toString()
            eventView.textViewEventLocation.text = currentEvent.placeName.toString()
            eventView.textViewEventOrganizer.text = currentEvent.organizer.toString()
            val image = eventView.findViewById<ImageView>(R.id.imageTicketEvent)

            //Referencia na obrázok v úložisku Firebase
            var photoRef = FirebaseStorage.getInstance()
                .reference
                .child(currentEvent.picture.toString())
            photoRef.downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(image)
            }

            eventView.buttonDeleteEventInterest.visibility = View.GONE
            eventView.buttonAddEventInterest.visibility = View.VISIBLE
            eventView.joinEvent.text = ""
            eventView.buttonAddEventInterest.setOnClickListener {
                eventsViewModel.saveAttendEvent(listEventAdapter[position])
            }

            for (e in listAttendEvents){
                if (e.id == currentEvent.id){
                    eventView.joinEvent.text = "Zúčastnim sa"
                    eventView.buttonDeleteEventInterest.visibility = View.VISIBLE
                    eventView.buttonAddEventInterest.visibility = View.GONE
                    eventView.buttonDeleteEventInterest.setOnClickListener {
                        listAttendEvents.remove(e)
                        attendEventsViewModel.deleteEvent(e)
                        eventView.joinEvent.text = ""
                        eventView.buttonAddEventInterest.visibility = View.VISIBLE
                        eventView.buttonDeleteEventInterest.visibility = View.GONE
                    }
                }
            }

            eventView.setOnClickListener(View.OnClickListener {
                val bundle = Bundle()
                bundle.putString("EVENT_ID",currentEvent.id)
                bundle.putString("ORGANIZER_ID",currentEvent.organizerID)
                findNavController().navigate(R.id.action_nav_events_to_eventFragment,bundle)
            })

            return eventView
        }

        /**
         * @param pozicia prvku v liste
         */
        override fun getItem(position: Int): Any {
            return listEventAdapter[position]
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
            return listEventAdapter.size
        }
    }
}