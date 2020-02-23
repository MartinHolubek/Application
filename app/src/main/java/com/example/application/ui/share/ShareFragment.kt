package com.example.application.ui.share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.application.Friend
import com.example.application.R
import kotlinx.android.synthetic.main.ticket_friend.*
import kotlinx.android.synthetic.main.ticket_friend.view.*

class ShareFragment : Fragment() {

    private lateinit var shareViewModel: ShareViewModel

    lateinit var  listViewFriends : ListView
    lateinit var listFriends : ArrayList<Friend>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shareViewModel =
            ViewModelProviders.of(this).get(ShareViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_share, container, false)
        val textView: TextView = root.findViewById(R.id.text_share)
        shareViewModel.text.observe(this, Observer {
            textView.text = it
        })

        listFriends = arrayListOf()
        shareViewModel.getFriends().observe(this, Observer {
            listFriends = ArrayList(it)

            updateList(root)

        })

        return root
    }

    fun updateList(view : View){

        listViewFriends = view.findViewById<ListView>(R.id.lvFriends)
        var myfoodAdapter= foodAdapter(view.context,listFriends)
        listViewFriends.adapter=myfoodAdapter
    }

    inner class foodAdapter: BaseAdapter {
        var listFriendAdapter : ArrayList<Friend>
        var context: Context?=null
        constructor(context:Context, listPlaceAdapter: ArrayList<Friend>):super(){
            this.listFriendAdapter=listPlaceAdapter
            this.context=context
        }

        /**
         * Vytvorí
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var friendView=layoutInflater.inflate(R.layout.ticket_friend,null)
            var currentfriend=listFriendAdapter[position]
            friendView.textViewName.text = currentfriend.displayName.toString()
            friendView.addFriend.text = currentfriend.uid.toString()
            friendView.buttonAddFriend.text = getText(R.string.RemoveFriend)
            friendView.buttonAddFriend.setOnClickListener(View.OnClickListener {
                //shareViewModel.saveFriendsToFirebase(listFriendAdapter[position])
                shareViewModel.deleteFriendItem(listFriendAdapter[position])
            })

            return friendView
        }

        /**
         * @param pozicia prvku v liste
         */
        override fun getItem(position: Int): Any {
            return listFriendAdapter[position]
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
            return listFriendAdapter.size
        }
    }
}