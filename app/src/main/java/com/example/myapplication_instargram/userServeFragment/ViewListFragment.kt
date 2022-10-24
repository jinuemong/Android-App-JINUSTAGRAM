package com.example.myapplication_instargram.userServeFragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.transition.Transition
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.manager.LikerAdapter
import com.example.myapplication_instargram.manager.StoryViewerAdapter
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.Like
import com.example.myapplication_instargram.unit.Story
import com.example.myapplication_instargram.unit.StoryViewer

class ViewListFragment (
    likerList:ArrayList<Like>?,viewerList:ArrayList<StoryViewer>?,
): Fragment() {
    private val likerListPost=likerList
    private val viewerListPost=viewerList
    private lateinit var userActivity : UserActivity
    private lateinit var masterApp : MasterApplication
    //생명 주기 관리 (뒤로가기 버튼의 )
    private lateinit var callback: OnBackPressedCallback
    private lateinit var fm: FragmentManager
    private lateinit var goBackButton: ImageView
    private lateinit var viewRecycler: RecyclerView
    private lateinit var viewCount: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        masterApp = userActivity.application as MasterApplication
        fm = userActivity.supportFragmentManager //프래그먼트 매니저
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userActivity.onFragmentGoBack(
                    this@ViewListFragment
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }
    @SuppressLint("SetTextI18n")
    private fun initView(view:View){
        goBackButton = view.findViewById(R.id.viewer_back_button)
        viewRecycler = view.findViewById(R.id.viewer_recycler)
        viewCount = view.findViewById(R.id.viewer_count)
        if (likerListPost!=null){ // 좋아요 리스트
            viewRecycler.adapter = LikerAdapter(userActivity,likerListPost, LayoutInflater.from(userActivity))
            viewCount.text = likerListPost.size.toString()+ " viewer"
        }else if(viewerListPost!=null){ //뷰어 리스트
            viewRecycler.adapter = StoryViewerAdapter(userActivity, viewerListPost, LayoutInflater.from(userActivity))
            viewCount.text = viewerListPost.size.toString()+ " viewer"
        }
        goBackButton.setOnClickListener {
            userActivity.onFragmentGoBack(this@ViewListFragment)
        }
    }
}