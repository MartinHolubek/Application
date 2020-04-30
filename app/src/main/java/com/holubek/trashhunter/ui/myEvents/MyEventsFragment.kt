package com.holubek.trashhunter.ui.myEvents

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
import com.holubek.trashhunter.DateFormat
import com.holubek.trashhunter.Event

import com.holubek.trashhunter.R
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ticket_event.view.*
import kotlin.collections.ArrayList

/**
 * Trieda na zobrazenie udalosti, ktoré vytvoril užívateľ
 */
class MyEventsFragment : Fragment() {

    companion object {
        fun newInstance() =
            MyEventsFragment()
    }

    lateinit var  listViewEvents : ListView
    lateinit var listEvents : ArrayList<Event>

    private lateinit var viewModel: MyEventsViewModel

    /**
     * Inicializácia ListView udalosti, ktoré vytvoril užívateľ
     */
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

    /**
     * Nastavenie zoznamu udalosti, ktoré vytvoril užívateľ
     * @param view objekt View, ktorý reprezentuje obrazovku na pridávanie priateľov
     */
    fun updateList(view: View){
        var myFriendsAdapter= eventsAdapter(view.context,listEvents)
        listViewEvents.adapter=myFriendsAdapter
    }

    /**
     * trieda na naplnenie ListView udalosťami, ktoré vytvoril užívateľ
     */
    inner class eventsAdapter: BaseAdapter {
        var listEventAdapter : ArrayList<Event>
        var context: Context?=null
        constructor(context: Context, listEventAdapter: ArrayList<Event>):super(){
            this.listEventAdapter=listEventAdapter
            this.context=context
        }

        /**
         * Vytvorí View, v ktorom naplní údaje o udalosti, ktoré vytvoril užívateľ
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var eventView=layoutInflater.inflate(R.layout.ticket_event,null)
            var currentEvent=listEventAdapter[position]
            eventView.textViewEventStartDate.text = DateFormat.getDateTimeFormat(currentEvent.startDate!!)
            eventView.textViewEventTitle.text = currentEvent.title.toString()
            eventView.textViewEventLocation.text = currentEvent.placeName
            eventView.textViewEventOrganizer.text = currentEvent.organizer.toString()
            eventView.buttonAddEventInterest.visibility = View.GONE
            eventView.buttonDeleteEventInterest.visibility = View.GONE
            val image = eventView.findViewById<ImageView>(R.id.imageTicketEvent)

            eventView.button_delete_event.visibility = View.VISIBLE
            eventView.button_delete_event.setOnClickListener {
                viewModel.deleteEvent(listEventAdapter[position])
            }
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

            photoRef.downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(image)
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