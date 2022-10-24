package com.example.myapplication_instargram.userFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Callback
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_instargram.manager.ItemViewRecyclerAdapter
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.unit.MiniProfiles
import retrofit2.Call
import retrofit2.Response

private lateinit var username: String
private lateinit var userActivity: UserActivity
private lateinit var exRecycler: RecyclerView
private lateinit var masterApp: MasterApplication
class Explore : Fragment() {

    //activity의 context 사용을 위함
    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        username = userActivity.username
        //서버와 통신
        masterApp = ((activity as UserActivity).application as MasterApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exRecycler = view.findViewById<RecyclerView>(R.id.explore_recycler)
        initExploreRecycler()
    }
}


private fun initExploreRecycler(){
    masterApp.service.getExploreList(username)
        .enqueue(object : Callback<ArrayList<MiniProfiles>>{
            override fun onResponse(
                call: Call<ArrayList<MiniProfiles>>,
                response: Response<ArrayList<MiniProfiles>>
            ) {
                if (response.isSuccessful){
                    //어뎁터 생성
                    val adapter = response.body()?.let {
                            exploreList->
                        ItemViewRecyclerAdapter(exploreList,LayoutInflater.from(userActivity),
                            masterApp, username,userActivity,2)
                    }
                    exRecycler.adapter = adapter
                    exRecycler.layoutManager = LinearLayoutManager(userActivity)
                }
            }
            override fun onFailure(call: Call<ArrayList<MiniProfiles>>, t: Throwable) {}
        })
}