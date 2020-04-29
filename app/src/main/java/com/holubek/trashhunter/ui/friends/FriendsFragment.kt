package com.holubek.trashhunter.ui.friends

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.holubek.trashhunter.Friend
import com.holubek.trashhunter.R
import com.holubek.trashhunter.firebase.FirebaseStorage
import kotlinx.android.synthetic.main.ticket_user.view.*

/**
 * Trieda na zobrzovanie priateľov
 */
class FriendsFragment : Fragment() {

    private lateinit var friendsViewModel: FriendsViewModel

    lateinit var  listViewFriends : ListView
    lateinit var listFriends : ArrayList<Friend>

    lateinit var firebaseStorage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        friendsViewModel =
            ViewModelProviders.of(this).get(FriendsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_friends, container, false)

        firebaseStorage = FirebaseStorage()
        listFriends = arrayListOf()
        friendsViewModel.getFriends().observe(this, Observer {
            listFriends = ArrayList(it)
            updateList(root)
        })

        return root
    }

    fun updateList(view : View){

        listViewFriends = view.findViewById<ListView>(R.id.lvFriends)
        var myFriendsAdapter= friendsAdapter(view.context,listFriends)
        listViewFriends.adapter=myFriendsAdapter
    }

    /**
     * trieda na naplnenie ListView objektmi priateľov
     */
    inner class friendsAdapter: BaseAdapter {
        var listFriendAdapter : ArrayList<Friend>
        var context: Context?=null
        constructor(context:Context, listPlaceAdapter: ArrayList<Friend>):super(){
            this.listFriendAdapter=listPlaceAdapter
            this.context=context
        }

        /**
         * Vytvorí View, v ktorom naplní údaje o priateľovi
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var friendView=layoutInflater.inflate(R.layout.ticket_user,null)
            var currentfriend=listFriendAdapter[position]
            friendView.textViewName.text = currentfriend.displayName.toString()

            firebaseStorage.getImage(currentfriend.image.toString()).addOnSuccessListener {
                var bitMap = BitmapFactory.decodeByteArray(it, 0, it.size)
                friendView.imageTicket.setImageBitmap(bitMap)
            }
            friendView.buttonAddUser.text = getText(R.string.RemoveFriend)
            friendView.buttonAddUser.setOnClickListener(View.OnClickListener {
                friendsViewModel.deleteFriendItem(listFriendAdapter[position])
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