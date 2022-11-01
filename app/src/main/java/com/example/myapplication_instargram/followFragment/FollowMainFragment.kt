package com.example.myapplication_instargram.followFragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.unit.User
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FollowMainFragment(
    followType : Int,
    username:String
) : Fragment() {
    private  var type = followType
    private lateinit var userActivity: UserActivity
    private lateinit var followTab:TabLayout
    private lateinit var followView: ViewPager2
    private lateinit var pagerAdapter: FollowFragmentAdapter
    private val targetUsername= username
    //생명 주기 관리 (뒤로가기 버튼의 )
    private lateinit var callback: OnBackPressedCallback

    // 유저 프로필 이름
    private val tintColor= ColorStateList( //클릭 논 클릭
        arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(-android.R.attr.state_selected)),
        //위의 결과를 아래로 파서~ 클릭, 노클릭 - 아이콘 뷰의 이미지 색 변경 용
        intArrayOf(Color.parseColor("#FF000000"), Color.parseColor("#FF6B99B3"))
    )

    private val textView = arrayOf(
        "팔로워", "팔로잉"
    )
    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userActivity.onFragmentGoBack(this@FollowMainFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_follow, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.startAnimation(userActivity.viewChangeAnimateLeft)
        followTab = view.findViewById<TabLayout>(R.id.tab_follow)
        followView = view.findViewById<ViewPager2>(R.id.view_follow_view_pager)
        val backButton = view.findViewById<ImageView>(R.id.follow_back_button)
        val topName = view.findViewById<TextView>(R.id.follow_user_name)
        topName.text = targetUsername

        backButton.setOnClickListener {
            (activity as UserActivity).onFragmentGoBack(this@FollowMainFragment)
        }

        //프래그먼트에 프래그먼트 연결 시 매니저 + 라이프 사이클을 보내줌
        pagerAdapter = FollowFragmentAdapter(childFragmentManager,lifecycle)
        pagerAdapter.addFragment(ListFollowerFragment(targetUsername))
        pagerAdapter.addFragment(ListFollowingFragment(targetUsername))
        followView.adapter = pagerAdapter
        followView.currentItem = type
        //페이지 선택 시 포지션 이동
        followView.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
        //탭 설정
        followTab.tabTextColors = tintColor
        TabLayoutMediator(followTab,followView){ tab,position->
            tab.text = textView[position]

        }.attach()
    }

    override fun onResume() {
        super.onResume()
        //해당 뷰 동작 - > 외부 뷰 스와이프 작동 중지
        userActivity.viewPager.isUserInputEnabled = false
    }
}
