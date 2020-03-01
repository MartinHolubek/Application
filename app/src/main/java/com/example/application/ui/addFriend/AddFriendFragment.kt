package com.example.application.ui.addFriend

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.application.Friend
import com.example.application.R
import kotlinx.android.synthetic.main.ticket_friend.view.*

class AddFriendFragment : Fragment() {

    private lateinit var addFriendViewModel: AddFriendViewModel

    lateinit var  listViewFriends : ListView
    lateinit var listPotentionalFriends : ArrayList<Friend>
    lateinit var listFriends : ArrayList<Friend>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addFriendViewModel =
            ViewModelProviders.of(this).get(AddFriendViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_addfriend, container, false)
        val textView: TextView = root.findViewById(R.id.text_send)
        listFriends = arrayListOf()
        listPotentionalFriends = arrayListOf()

        addFriendViewModel.getPotentionalFriends(textView.text.toString()).observe(this, Observer {
            listPotentionalFriends = ArrayList(it)
            updateList(root)
        })

        addFriendViewModel.getFriends().observe(this, Observer {
            listFriends = ArrayList(it)
        })

        var buttonSearch = root.findViewById<Button>(R.id.buttonSearchFriend)
        buttonSearch.setOnClickListener(View.OnClickListener {
            addFriendViewModel.getPotentionalFriends(textView.text.toString())
        })
        return root
    }

    fun updateList(view : View){

        listViewFriends = view.findViewById<ListView>(R.id.lvPotentionalFriends)
        var myfoodAdapter= foodAdapter(view.context,listPotentionalFriends)
        listViewFriends.adapter=myfoodAdapter
    }

    fun isFriend(friend: Friend): Boolean{
        var equal = false
        for (obj in listFriends){
            if (obj.uid == friend.uid){
                equal = true
            }
        }
        return equal
    }

    inner class foodAdapter: BaseAdapter {
        var listFriendAdapter : ArrayList<Friend>
        var context: Context?=null
        constructor(context: Context, listPlaceAdapter: ArrayList<Friend>):super(){
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

            if (isFriend(listFriendAdapter[position])){
                //buttonAddFriend.text = getText(R.string.RemoveFriend)
                friendView.buttonAddFriend.text = "Odober"
            }else{
                //buttonAddFriend.text = getText(R.string.AddFriend)
                friendView.buttonAddFriend.text = "Pridaj"
            }
            friendView.buttonAddFriend.setOnClickListener(View.OnClickListener {

                if (!isFriend(listFriendAdapter[position])){
                    addFriendViewModel.saveFriendsToFirebase(listFriendAdapter[position])
                    friendView.buttonAddFriend.text = getText(R.string.RemoveFriend)
                }else{
                    addFriendViewModel.deleteFriendItem(listFriendAdapter[position])
                    friendView.buttonAddFriend.text = getText(R.string.AddFriend)
                }

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