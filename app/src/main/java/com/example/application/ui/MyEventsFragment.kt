package com.example.application.ui

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
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.application.Event

import com.example.application.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.my_events_fragment.*
import kotlinx.android.synthetic.main.ticket_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class MyEventsFragment : Fragment() {

    companion object {
        fun newInstance() = MyEventsFragment()
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
            var currentevent=listEventAdapter[position]
            eventView.textViewEventDate.text = Date(currentevent.startDate!!.seconds).toString()
            eventView.textViewEventTitle.text = currentevent.title.toString()
            eventView.textViewOrganizerValue.text = currentevent.organizer

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
