package com.holubek.trashhunter.ui.JoinedUsers


import android.content.Context
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

import com.holubek.trashhunter.R
import com.holubek.trashhunter.User
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ticket_user.view.*

private const val ARG_PARAM1 = "EVENT_ID"
private const val ARG_PARAM2 = "ORGANIZER_ID"

/**
 * Trieda, ktorá zobrazí účastníkov na udalosti
 */
class JoinedUsersFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JoinedUsersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    lateinit var  listViewUsers : ListView
    lateinit var listUsers : ArrayList<User>
    var organizerID:String?=null
    var eventID:String?=null

    private lateinit var viewModel: JoinedUsersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventID = it.getString(ARG_PARAM1)
            organizerID = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.joined_users_fragment, container, false)
    }

    /**
     * Inicializácia ListView, ktorý zobrazuje účastníkov
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(JoinedUsersViewModel::class.java)
        listUsers = arrayListOf()
        listViewUsers = view!!.findViewById(R.id.lvJoinedUsers)
        viewModel.getUsers(organizerID!!,eventID!!).observe(this, Observer {
            listUsers = ArrayList(it)
            updateList(view!!)
        })
    }
    fun updateList(view: View){
        var myUsersAdapter= UsersAdapter(view.context,listUsers)
        listViewUsers.adapter=myUsersAdapter
    }

    /**
     * trieda na naplnenie ListView účastníkmi
     */
    inner class UsersAdapter(context: Context, listUserAdapter: ArrayList<User>) :
        BaseAdapter() {
        private var listUserAdapter : ArrayList<User> = listUserAdapter
        var context: Context?= context

        /**
         * Vytvorí View, v ktorom naplní údaje o účastníkovi
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val userView= layoutInflater.inflate(R.layout.ticket_user,null)
            val currentUser= listUserAdapter[position]
            userView.textViewName.text = currentUser.displayName
            userView.buttonAddUser.visibility = View.GONE

            val image = userView.findViewById<ImageView>(R.id.imageTicket)

            //Referencia na obrázok v úložisku Firebase
            var photoRef = FirebaseStorage.getInstance()
                .reference
                .child(currentUser.image.toString())
            photoRef.downloadUrl.addOnSuccessListener {uri ->
                // Konvertujeme byteArray na bitmap
                Picasso.get().load(uri).into(image)
            }
            return userView
        }

        /**
         * @param pozicia prvku v liste
         */
        override fun getItem(position: Int): Any {
            return listUserAdapter[position]
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
            return listUserAdapter.size
        }
    }


}
