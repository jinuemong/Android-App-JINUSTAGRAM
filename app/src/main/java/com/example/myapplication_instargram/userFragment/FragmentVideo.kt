package com.example.myapplication_instargram.userFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.manager.RandomPosterAdapter
import com.example.myapplication_instargram.manager.PosterManager
import com.example.myapplication_instargram.server.MasterApplication

private lateinit var username: String
private lateinit var userActivity: UserActivity
private lateinit var viRecycler: RecyclerView
private lateinit var masterApp: MasterApplication
private lateinit var posterManager: PosterManager
class FragmentVideo : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        username = userActivity.username
        //서버와 통신
        masterApp = ((activity as UserActivity).application as MasterApplication)
        posterManager = PosterManager(masterApp)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viRecycler = view.findViewById<RecyclerView>(R.id.video_recycler)
        initVideoRecycler()
    }

}

private fun initVideoRecycler(){
    posterManager.getRandPosterList("2", username, paramFunc ={ randList->
        if (randList!=null){
            viRecycler.adapter = RandomPosterAdapter(userActivity,randList,
                LayoutInflater.from(userActivity),posterManager)
            viRecycler.layoutManager = LinearLayoutManager(userActivity)
        }
    })
}