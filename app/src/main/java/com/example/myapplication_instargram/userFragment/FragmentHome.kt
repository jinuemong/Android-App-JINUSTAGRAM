package com.example.myapplication_instargram.userFragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication_instargram.*
import com.example.myapplication_instargram.Message.MessageRoomListFragment
import com.example.myapplication_instargram.manager.FollowManager
import com.example.myapplication_instargram.manager.RandomPosterAdapter
import com.example.myapplication_instargram.manager.PosterManager
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.MiniProfiles
import com.example.myapplication_instargram.userServeFragment.StoryFragment
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList

private lateinit var username: String
private lateinit var userActivity: UserActivity
private lateinit var masterApp: MasterApplication
private lateinit var followManager : FollowManager
private lateinit var posterManager: PosterManager
private lateinit var storyRecycler : RecyclerView
private lateinit var posterRecycler : RecyclerView

class Home : Fragment() {
    private lateinit var fm:FragmentManager
    private lateinit var uploadButton : ImageView
    private lateinit var getHeartList : ImageView
    private lateinit var getMessageList : ImageView
    private lateinit var swipe: SwipeRefreshLayout
    override fun onStart() {
        super.onStart()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        username = userActivity.username
        fm = (activity as UserActivity).supportFragmentManager //프래그먼트 매니저
        masterApp = (userActivity.application as MasterApplication)

        followManager = FollowManager(masterApp)
        posterManager = PosterManager(masterApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUpListener()
        initStoryRecycler()
        initPostingRecycler()
        swipe.setOnRefreshListener {
            //새로고침 시 초기화 init()
            initStoryRecycler()
            initPostingRecycler()

            swipe.isRefreshing=false
        }
    }
    override fun onResume() {
        super.onResume()
    }

    private fun initView(view:View){
        uploadButton = view.findViewById(R.id.frag_home_upload_button)
        getHeartList = view.findViewById(R.id.go_to_heart)
        getMessageList = view.findViewById(R.id.go_to_message)
        storyRecycler = view.findViewById(R.id.main_horizontal_recyclerView)
        posterRecycler = view.findViewById(R.id.main_vertical_recyclerView)
        swipe = view.findViewById(R.id.swipe_home)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpListener(){
        //스토리 리싸이클러 스크롤 시 뷰페이저 이동 제어
        storyRecycler.setOnTouchListener{_,event->
            when(event.action){
                MotionEvent.ACTION_DOWN->{
                    userActivity.viewPager.isUserInputEnabled=false
                }
                MotionEvent.ACTION_MOVE->{
                    userActivity.viewPager.isUserInputEnabled=false
                }
                MotionEvent.ACTION_UP->{
                    userActivity.viewPager.isUserInputEnabled=true
                }
                else->{}
            }
            true
        }

        //업로드 버튼 클릭 리스너
        uploadButton.setOnClickListener { view->
            val popupMenu = PopupMenu(userActivity.applicationContext, view)
            userActivity.menuInflater.inflate(R.menu.pop_up_upload,popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { menu->
                when(menu.itemId){
                    R.id.upload_story->{ //스토리 추가
                        val storyIntent = Intent(userActivity, SelectPicActivity::class.java)
                        storyIntent.putExtra("username",username)
                        startActivity(storyIntent)
                        return@setOnMenuItemClickListener true
                    }
                    else->{ //게시물 추가
                        val posterIntent = Intent(userActivity, UploadPosterActivity::class.java)
                        posterIntent.putExtra("username",username)
                        startActivity(posterIntent)
                        return@setOnMenuItemClickListener true
                    }                }
            }
        }

        getHeartList.setOnClickListener {

        }

        getMessageList.setOnClickListener {
            userActivity.onFragmentChange(MessageRoomListFragment())
        }

    }
}


//ArrayList<Profiles> -> 내가 팔로우 하는 프로필 목록을 가져와서 클릭 시 스토리 연결
private class StoryAdapter(
    private val userActivity: UserActivity,
    private val itemList:ArrayList<MiniProfiles>,
    private val inflater: LayoutInflater,
):RecyclerView.Adapter<StoryAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val itemUsername : TextView
        val itemUserImage : CircleImageView
        init {
            itemUsername = itemView.findViewById(R.id.item_circle_profile_name)
            itemUserImage= itemView.findViewById(R.id.item_circle_profile_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryAdapter.ViewHolder {
        val view = inflater.inflate(R.layout.item_circle_mini_profile,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryAdapter.ViewHolder, position: Int) {

        itemList[position].userImage.let { userActivity.setCircleImage(it, holder.itemUserImage) }
        holder.itemUsername.text = itemList[position].username
        //스토리가 1개 이상이면 스토리 있음 표시
        if (itemList[position].storyCount > 0 && (username!=itemList[position].username) ) {
            holder.itemUserImage.apply {
                //배경색 지정
                borderColor = Color.parseColor("#62BEF3")
                // 사이즈 지정
                borderWidth *= 2
            }
        }
        //스토리 이미지 클릭 이빈트
        holder.itemUserImage.setOnClickListener {
            if (itemList[position].storyCount > 0) {
                try {
                    userActivity.onFragmentChange(
                        StoryFragment(
                            itemList[position].storyPost!!,
                            itemList[position].username,
                            itemList[position].userImage
                        )
                    )
                } catch (e: Exception) {
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}



@RequiresApi(Build.VERSION_CODES.N)
private fun initStoryRecycler(){
    followManager.getFollowingList(username, paramFunc = { profileList ->
        followManager.takeMiniProfile(username, paramFunc = { myProfile->
            if (myProfile!=null){
                    //내 프로필 맨 앞에 추가
                    profileList.add(0, myProfile)
                    //스토리가 0개인 프로필은 삭제
                    profileList.removeIf { it.storyCount < 1 }
                val adapter = StoryAdapter(userActivity, profileList, LayoutInflater.from(userActivity))
                storyRecycler.adapter = adapter
            }
        })
    })

}

private fun initPostingRecycler(){
    posterManager.getRandPosterList("1", username, paramFunc ={ randList->
        if (randList!=null){
            posterRecycler.adapter = RandomPosterAdapter(userActivity,randList,
                LayoutInflater.from(userActivity), posterManager)
            posterRecycler.layoutManager = LinearLayoutManager(userActivity)
        }
    })
}