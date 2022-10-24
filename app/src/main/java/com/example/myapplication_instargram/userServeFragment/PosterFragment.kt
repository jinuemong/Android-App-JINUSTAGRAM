package com.example.myapplication_instargram.userServeFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.manager.PosterAdapter
import com.example.myapplication_instargram.manager.RandomPosterAdapter
import com.example.myapplication_instargram.manager.PosterManager
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.MiniProfiles
import com.example.myapplication_instargram.unit.Poster
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PosterFragment(
    viewUsername:String,
    posterList : ArrayList<Poster>,itemNum:Int) : Fragment() {
    private var viewuser = viewUsername
    private var userPosterList = posterList
    private var startNum  = itemNum
    private var isReset = true
    private lateinit var username : String
    private lateinit var userActivity: UserActivity
    private lateinit var masterApp : MasterApplication
    private lateinit var posterManager: PosterManager

    private lateinit var goBackButton:ImageView
    private lateinit var rePoster:RecyclerView
    //생명 주기 관리 (뒤로가기 버튼의 )
    private lateinit var callback: OnBackPressedCallback
    private lateinit var fm: FragmentManager
    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        masterApp = userActivity.application as MasterApplication
        posterManager = PosterManager(masterApp)
        username = userActivity.username

        fm = userActivity.supportFragmentManager //프래그먼트 매니저
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userActivity.onFragmentGoBack(
                    this@PosterFragment
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_poster, container, false)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initRePoster()
        setUpListener()
    }

    private fun initView(view:View){
        goBackButton = view.findViewById(R.id.poster_back_button)
        rePoster =view.findViewById(R.id.poster_recycler)
    }
    private fun setUpListener(){
        goBackButton.setOnClickListener {
            userActivity.onFragmentGoBack(this@PosterFragment)
        }
        //포스터 삭제 위한 리싸이클러 뷰 리스너
        rePoster.addOnItemTouchListener(object :
            RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    val position = rv.getChildAdapterPosition(child)
                    val view = rv.layoutManager?.findViewByPosition(position)
                    val id =
                        view?.findViewById<TextView>(R.id.posting_id)?.text.toString()
                    val moreInfoButton = view?.findViewById<ImageView>(R.id.poster_setting)
                    moreInfoButton?.setOnClickListener { infoView->
                        val popupMenu = PopupMenu(userActivity.applicationContext, infoView)
                        userActivity.menuInflater.inflate(R.menu.pop_up_poster,popupMenu.menu)
                        popupMenu.show()
                        popupMenu.setOnMenuItemClickListener { menu->
                            when(menu.itemId){
                                R.id.del_poster->{ //게시물 삭제
                                    posterManager.deletePoster(id.toInt(), paramFunc = {
                                        if(it){
                                            userPosterList.removeAt(position)
                                            rePoster.adapter?.notifyItemRemoved(position)
                                            rePoster.adapter?.notifyItemRangeChanged(
                                                position,
                                                rv.adapter!!.itemCount - position
                                            )
                                        }
                                    })
                                    return@setOnMenuItemClickListener true
                                }
                                else->{ return@setOnMenuItemClickListener true }
                            }
                        }
                    }
                }
                return false
            }
            //To change body of created functions use File | Settings | File Templates.
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            //To change body of created functions use File | Settings | File Templates.
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }
    private fun initRePoster(){
        masterApp.service.getUserProfileMini(viewuser)
            .enqueue(object : Callback<ArrayList<MiniProfiles>> {
                override fun onResponse(
                    call: Call<ArrayList<MiniProfiles>>,
                    response: Response<ArrayList<MiniProfiles>>
                ) {
                    if (response.isSuccessful){
                        rePoster.adapter = PosterAdapter(
                            response.body()!![0].username,
                            response.body()!![0].userImage,
                            response.body()!![0].customName,
                            userActivity,userPosterList, LayoutInflater.from(userActivity),posterManager)
                        if(isReset) {
                            rePoster.scrollToPosition(startNum)
                            isReset =false
                        }
                    }
                }
                override fun onFailure(call: Call<ArrayList<MiniProfiles>>, t: Throwable) {}
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}