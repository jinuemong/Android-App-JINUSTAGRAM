package com.example.myapplication_instargram.followFragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_instargram.manager.FollowManager
import com.example.myapplication_instargram.manager.ItemViewRecyclerAdapter
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.UserActivity

private lateinit var followerRecycler : RecyclerView
private lateinit var username: String //my name
private lateinit var followManager: FollowManager
private lateinit var userActivity : UserActivity
private lateinit var masterApp: MasterApplication
private lateinit var adapter : ItemViewRecyclerAdapter
class ListFollowerFragment(username : String): Fragment() {
    private val targetUserName= username
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        username = (activity as UserActivity).username
        masterApp = ((activity as UserActivity).application as MasterApplication)
        followManager = FollowManager(masterApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_follower_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        followerRecycler = view.findViewById<RecyclerView>(R.id.follower_list_recycler)
        initFollowerRecycler()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun initFollowerRecycler(){
        //target
        val type =  if (username== targetUserName){1}else{ 2 }
        followManager.getFollowerList(targetUserName, paramFunc = {followerList->
            adapter = ItemViewRecyclerAdapter(followerList,LayoutInflater.from(userActivity),
                masterApp, username,userActivity,type)
            followerRecycler.adapter =adapter
            followerRecycler.layoutManager = LinearLayoutManager(userActivity)
            parentFragment?.onResume() //초기화
        })
    }
}
