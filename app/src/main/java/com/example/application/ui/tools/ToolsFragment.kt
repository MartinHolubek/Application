package com.example.application.ui.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.application.Event
import com.example.application.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.ticket_event.view.*
import java.util.*
import kotlin.collections.ArrayList

class ToolsFragment : Fragment() {

    private lateinit var toolsViewModel: ToolsViewModel

    lateinit var  listViewEvents : ListView
    lateinit var listEvents : ArrayList<Event>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolsViewModel =
            ViewModelProviders.of(this).get(ToolsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tools, container, false)
        val textView: TextView = root.findViewById(R.id.text_tools)
        toolsViewModel.text.observe(this, Observer {
            textView.text = it
        })

        listEvents = arrayListOf()
        toolsViewModel.getSavedEvents().observe(this, Observer {
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