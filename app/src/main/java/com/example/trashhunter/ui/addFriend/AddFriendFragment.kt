package com.example.trashhunter.ui.addFriend

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.trashhunter.Friend
import com.example.trashhunter.R
import com.example.trashhunter.firebase.FirebaseStorage
import kotlinx.android.synthetic.main.ticket_user.view.*

class AddFriendFragment : Fragment() {

    private lateinit var addFriendViewModel: AddFriendViewModel

    lateinit var  listViewFriends : ListView

    //zoznam uzivatelov
    lateinit var listPotentionalFriends : ArrayList<Friend>

    //zoznam priatelov
    lateinit var listFriends : ArrayList<Friend>

    lateinit var firebaseStorage : FirebaseStorage

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

        firebaseStorage = FirebaseStorage()

        var buttonSearch = root.findViewById<Button>(R.id.buttonSearchFriend)
        buttonSearch.setOnClickListener(View.OnClickListener {

            if (!textView.text.toString().equals("")){
                addFriendViewModel.getPotentionalFriends(textView.text.toString())
            }else{
                Toast.makeText(root.context,"Vlože vyhladavaci text",Toast.LENGTH_SHORT).show()
            }
        })
        return root
    }

    fun updateList(view : View){

        listViewFriends = view.findViewById<ListView>(R.id.lvPotentionalFriends)
        var myfoodAdapter= friendAdapter(view.context,listPotentionalFriends)
        listViewFriends.adapter=myfoodAdapter
    }

    /**
     *
     */
    fun isFriend(friend: Friend): Boolean{
        var equal = false
        for (obj in listFriends){
            if (obj.uid == friend.uid){
                equal = true
            }
        }
        return equal
    }

    inner class friendAdapter: BaseAdapter {
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

            var friendView=layoutInflater.inflate(R.layout.ticket_user,null)
            var currentfriend=listFriendAdapter[position]
            friendView.textViewName.text = currentfriend.displayName.toString()

            firebaseStorage.getImageUser(currentfriend.image.toString()).addOnSuccessListener {
                var bitMap = BitmapFactory.decodeByteArray(it, 0, it.size)
                friendView.imageTicket.setImageBitmap(bitMap)
            }

            if (isFriend(listFriendAdapter[position])){
                //buttonAddFriend.text = getText(R.string.RemoveFriend)
                friendView.buttonAddUser.text = "Odober"
            }else{
                //buttonAddFriend.text = getText(R.string.AddFriend)
                friendView.buttonAddUser.text = "Pridaj"
            }
            friendView.buttonAddUser.setOnClickListener(View.OnClickListener {

                if (!isFriend(listFriendAdapter[position])){
                    addFriendViewModel.saveFriendsToFirebase(listFriendAdapter[position])
                    friendView.buttonAddUser.text = getText(R.string.RemoveFriend)
                }else{
                    addFriendViewModel.deleteFriendItem(listFriendAdapter[position])
                    friendView.buttonAddUser.text = getText(R.string.AddFriend)
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