package com.example.trashhunter.ui.myEvents

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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.trashhunter.DateFormat
import com.example.trashhunter.Event

import com.example.trashhunter.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.ticket_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class MyEventsFragment : Fragment() {

    companion object {
        fun newInstance() =
            MyEventsFragment()
    }

    lateinit var  listViewEvents : ListView
    lateinit var listEvents : ArrayList<Event>

    private lateinit var viewModel: MyEventsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(MyEventsViewModel::class.java)
        var root = inflater.inflate(R.layout.my_events_fragment, container, false)
        listViewEvents = root.findViewById<ListView>(R.id.lvMyEvents)
        super.onActivityCreated(savedInstanceState)

        listEvents = arrayListOf()
        viewModel.getSavedEvents().observe(this, Observer {
            listEvents = ArrayList(it)
            updateList(root)
        })

        return root
    }

    fun updateList(view: View){
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
            eventView.textViewEventLocation.text = currentEvent.placeName
            eventView.buttonAddEventInterest.visibility = View.GONE
            val image = eventView.findViewById<ImageView>(R.id.imageTicketEvent)


            eventView.setOnClickListener(View.OnClickListener {
                val bundle = Bundle()
                bundle.putString("EVENT_ID",currentEvent.id)
                bundle.putString("ORGANIZER_ID",currentEvent.organizerID)
                findNavController().navigate(R.id.action_myEventsFragment_to_eventFragment,bundle)
            })
            //Referencia na obrázok v úložisku Firebase
            var photoRef = FirebaseStorage.getInstance()
                .reference
                .child(currentEvent.picture.toString())

            /*photoRef.downloadUrl.addOnSuccessListener {
                image.setImageURI(it)
            }*/

            val ONE_MEGABYTE: Long = 1024 * 1024
            photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                // Konvertujeme byteArray na bitmap
                var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                image.setImageBitmap(bmp)
            }.addOnFailureListener {
                // Handle any errors
            }

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
