package com.example.myapplication_instargram.userFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapplication_instargram.*
import com.example.myapplication_instargram.Message.MessageRoomListFragment
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.Profile
import com.example.myapplication_instargram.followFragment.FollowMainFragment
import com.example.myapplication_instargram.manager.MiniPosterAdapter
import com.example.myapplication_instargram.manager.PosterManager
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import de.hdodenhof.circleimageview.CircleImageView

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class FragmentUserUI : Fragment() {
    lateinit var fm:FragmentManager
    lateinit var userUiView : View
    lateinit var userNameView : TextView
    lateinit var userNameViewIn : TextView
    lateinit var userImageView: CircleImageView
    lateinit var userPostNum : TextView
    lateinit var uploadButton: ImageView
    lateinit var userFollowerNum : TextView
    lateinit var userFollowingNum : TextView
    lateinit var userFollower : LinearLayout
    lateinit var userFollowing : LinearLayout
    lateinit var userCommandView:  TextView
    lateinit var userButton: Button
    lateinit var userPosterRecycler : RecyclerView
    lateinit var gridLayoutManager: GridLayoutManager
    lateinit var masterapp:MasterApplication
    lateinit var posterManager: PosterManager
    lateinit var username : String
    lateinit var userActivity : UserActivity
    lateinit var menuButton: ImageView
    private lateinit var slidePanel : SlidingUpPanelLayout
    lateinit var slideSetting :LinearLayout
    lateinit var slideMessage : LinearLayout
    lateinit var slideUpdate: LinearLayout
    lateinit var slideLogout: LinearLayout
    private lateinit var swipe: SwipeRefreshLayout
    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        fm = (activity as UserActivity).supportFragmentManager //프래그먼트 매니저
        masterapp = ((activity as UserActivity).application as MasterApplication)
        posterManager = PosterManager(masterapp)
        gridLayoutManager = GridLayoutManager(userActivity,3)


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userUiView = inflater.inflate(R.layout.fragment_user_ui,container,false)
        username = (activity as UserActivity).username
        return userUiView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(userUiView)
        setUpListener()
        swipe.setOnRefreshListener {
            initUi()
            swipe.isRefreshing=false
        }
    }
    //뷰 관리
    private fun initView(UserUiView:View) {
        menuButton = UserUiView.findViewById(R.id.frag_user_menu_button)
        userNameView = UserUiView.findViewById(R.id.frag_user_id)
        userNameViewIn = UserUiView.findViewById(R.id.custom_name)
        userImageView = UserUiView.findViewById(R.id.frag_user_image)
        userPostNum = UserUiView.findViewById(R.id.frag_user_post_num)
        userFollowerNum = UserUiView.findViewById(R.id.frag_user_follower_num)
        userFollowingNum = UserUiView.findViewById(R.id.frag_user_following_num)
        uploadButton = UserUiView.findViewById(R.id.frag_user_upload_button)
        userFollower = UserUiView.findViewById(R.id.Lin_user_ui_follower)
        userFollowing = UserUiView.findViewById(R.id.Lin_user_ui_following)
        userCommandView= UserUiView.findViewById(R.id.frag_user_command)
        userButton= UserUiView.findViewById(R.id.frag_user_update_button)
        userPosterRecycler =  UserUiView.findViewById(R.id.frag_user_recycler)
        slidePanel = userUiView.findViewById(R.id.main_frame)
        slideSetting =  UserUiView.findViewById(R.id.setting_slidePanel_in_ui)
        slideMessage =  UserUiView.findViewById(R.id.message_slidePanel_in_ui)
        slideUpdate =  UserUiView.findViewById(R.id.update_slidePanel_in_ui)
        slideLogout =  UserUiView.findViewById(R.id.logout_slidePanel_in_ui)
        swipe =  UserUiView.findViewById(R.id.swipe_user_ui)
        initUi()
    }
    private fun initUi(){
        masterapp.service.getUserProfile(username = username)
            .enqueue(object : Callback<ArrayList<Profile>> {
                override fun onResponse(call: Call<ArrayList<Profile>>,
                                        response: Response<ArrayList<Profile>>) {
                    try {
                        if (response.isSuccessful) {
                            val profile = response.body()!![0]
                            userNameView.text = profile.username
                            userNameViewIn.text = profile.customName
                            userCommandView.text = profile.userComment
                            profile.userImage?.let { userActivity.setCircleImage(it,userImageView) }
                            userPostNum.text = profile.posterCount.toString()
                            userFollowerNum.text = profile.followerCount.toString()
                            userFollowingNum.text = profile.followingCount.toString()
                            userCommandView.text = profile.userComment

                        }
                    }catch (e:Exception){}
                }

                override fun onFailure(call: Call<ArrayList<Profile>>, t: Throwable) {
                    Log.d("test1",t.message.toString())
                }

            })
        posterManager.getPosterList(username, paramFunc = { posterList->
            if(posterList!=null) {
                val adapter = MiniPosterAdapter(userActivity,
                    posterList, LayoutInflater.from(userActivity))
                userPosterRecycler.adapter = adapter
                userPosterRecycler.layoutManager = gridLayoutManager
            }
        })
    }

    private fun setUpListener(){
        //팔로워 보기
        userFollower.setOnClickListener {
            userActivity.onFragmentChange(
                FollowMainFragment(0,username)
            )
        }

        //팔로잉 보기
        userFollowing.setOnClickListener {
            userActivity.onFragmentChange(
                FollowMainFragment(1,username)
            )
        }

        //프로필 수정으로 이동
        userButton.setOnClickListener {
            val upIntent = Intent(userActivity, UpdateProfileActivity::class.java)
            upIntent.putExtra("username",""+username)
            startActivity(upIntent)
        }

        //슬라이드 패널 리스너
        val state = slidePanel.panelState
        menuButton.setOnClickListener {
            if (state == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            }
            // 열린 상태일 경우 닫기
            else if (state == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidePanel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
        }
        slideSetting.setOnClickListener {

        }
        slideMessage.setOnClickListener {
            userActivity.onFragmentChange(MessageRoomListFragment())
        }
        slideUpdate.setOnClickListener {

        }
        slideLogout.setOnClickListener {
            val mainIntent = Intent(userActivity, MainActivity::class.java)
            startActivity(mainIntent)
        }

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
                    }
                }
            }
        }

    }

}
