package com.example.trashhunter.ui

import android.content.Context
import android.graphics.Bitmap
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
import com.example.trashhunter.Event

import com.example.trashhunter.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.ticket_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class attendEventsFragment : Fragment() {

    companion object {
        fun newInstance() = attendEventsFragment()
    }
    lateinit var  listViewEvents : ListView
    lateinit var listEvents : ArrayList<Event>

    private lateinit var viewModel: AttendEventsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root = inflater.inflate(R.layout.attend_events_fragment, container, false)
        viewModel = ViewModelProviders.of(this).get(AttendEventsViewModel::class.java)
        listViewEvents = root.findViewById<ListView>(R.id.lvAttendEvents)
        super.onActivityCreated(savedInstanceState)

        listEvents = arrayListOf()
        viewModel.getSavedEvents().observe(this, Observer {
            listEvents = ArrayList(it)
            updateList(root)
        })

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AttendEventsViewModel::class.java)
        // TODO: Use the ViewModel
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
            var currentevent=listEventAdapter[position]
            eventView.textViewEventDate.text = Date(currentevent.startDate!!.seconds).toString()
            eventView.textViewEventTitle.text = currentevent.title.toString()
            eventView.textViewOrganizerValue.text = currentevent.organizer

            eventView.buttonAddEventInterest.setBackgroundResource(R.drawable.ic_delete_black_24dp)
            eventView.buttonAddEventInterest.setOnClickListener {
                viewModel.deleteEvent(listEventAdapter[position])
                listEvents.remove(listEventAdapter[position])
                updateList(view!!)

            }

            val image = eventView.findViewById<ImageView>(R.id.imageTicketEvent)

            //Referencia na obrázok v úložisku Firebase
            var photoRef = FirebaseStorage.getInstance()
                .reference
                .child(currentevent.picture.toString())

            /*photoRef.downloadUrl.addOnSuccessListener {
                image.setImageURI(it)
            }*/

            val ONE_MEGABYTE: Long = 1024 * 1024
            photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                // Konvertujeme byteArray na bitmap
                var bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.width,image.height,false))
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
