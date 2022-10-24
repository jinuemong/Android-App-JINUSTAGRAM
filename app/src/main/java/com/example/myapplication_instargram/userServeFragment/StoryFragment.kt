package com.example.myapplication_instargram.userServeFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.manager.StoryManager
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.Story
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.concurrent.thread

class StoryFragment(storyList : ArrayList<Story>,storyUser:String,storyUserImage:String):Fragment() {
    private var currentStoryIndex =0
    private var isStop = true
    private var storyViewerList = ArrayList<Int>()
    private val userStoryList = storyList
    private val maxStoryIndex = storyList.size
    private val userStoryName = storyUser
    private val userStoryImage  = storyUserImage
    private lateinit var userUiStory : LinearLayout
    private lateinit var username : String
    private lateinit var userActivity: UserActivity
    private lateinit var masterApp : MasterApplication
    private lateinit var storyManager:StoryManager
    private lateinit var progressBar : ProgressBar
    private lateinit var userImageInStory : CircleImageView
    private lateinit var userNameInStory : TextView
    private lateinit var timeInStory : TextView
    private lateinit var closeButton : ImageView
    private lateinit var storyImage : ImageView
    private lateinit var storyContent : LinearLayout
    private lateinit var moreInfo : ImageView
    private lateinit var goLeftStory:ImageView
    private lateinit var goRightStory:ImageView
    //생명 주기 관리 (뒤로가기 버튼의 )
    private lateinit var callback: OnBackPressedCallback
    private lateinit var fm: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        (super.onCreate(savedInstanceState))
    }

    override fun onDetach() {
        super.onDetach()
        isStop=false
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        masterApp = userActivity.application as MasterApplication
        storyManager = StoryManager(masterApp)
        username = userActivity.username

        //마지막으로 본 스토리로 이동
        for (story in userStoryList){
            for (viewUser in story.storyViewerPost){
                if (viewUser.viewer==username){
                    currentStoryIndex+=1
                    storyViewerList.add(currentStoryIndex)
                    //스택으로 이미 본 스토리 관리
                }
            }
        }
        //사이즈 초과 시 처음으로
        if (currentStoryIndex>=maxStoryIndex){currentStoryIndex = 0}

        fm = userActivity.supportFragmentManager //프래그먼트 매니저
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userActivity.onFragmentGoBack(
                    this@StoryFragment
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_posting_story,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.startAnimation(userActivity.viewChangeAnimateUp)
        initView(view)
        setUpListener()
    }

    fun initView(view:View){
        progressBar = view.findViewById(R.id.posting_story_progress_bar)//바에  구성
        userUiStory = view.findViewById(R.id.user_ui_in_story) //바에 구성
        userImageInStory = view.findViewById(R.id.posting_story_user_image)
        userNameInStory = view.findViewById(R.id.posting_story_name)
        timeInStory = view.findViewById(R.id.posting_story_time) //바에  구성
        closeButton = view.findViewById(R.id.posting_story_close)
        storyImage = view.findViewById(R.id.posting_story_image) //바에  구성
        storyContent = view.findViewById(R.id.posting_story_if_my_content)
        moreInfo = view.findViewById(R.id.posting_story_moreInfo)
        goLeftStory = view.findViewById(R.id.posting_story_left)
        goRightStory = view.findViewById(R.id.posting_story_right)
        loadStoryUi()
    }
    private fun loadStoryUi(){
        //내 스토리 아니라면 상세보기 삭제
        if (userStoryName!=username){
            storyContent.visibility = View.INVISIBLE
        }

        //이미지, 이름 적용
        userActivity.setCircleImage(userStoryImage,userImageInStory)
        userNameInStory.text = userStoryName
        try {
                thread (start = true){
                    progressBar.max = 100
                    progressBar.progress = 0
                    while (isStop) {
                        var isAuto = true
                        //스토리 뷰어 추가 (내 스토리가 아니라면)
                        if (username != userStoryList[currentStoryIndex].username &&
                            (!storyViewerList.contains(currentStoryIndex))) {
                            storyManager.addViewer(
                                userStoryList[currentStoryIndex].storyId,
                                username
                            )
                            storyViewerList.add(currentStoryIndex)
                        }
                        //쓰레드 내부에서 ui 실행
                        userActivity.runOnUiThread {
                            //ui 맨 앞으로
                            progressBar.bringToFront()
                            userUiStory.bringToFront()
                            timeInStory.text =
                                userActivity.getTranslatedDate(
                                    userStoryList[currentStoryIndex].uploadTime.toString()
                                )
                            userActivity.setRectImage(
                                userStoryList[currentStoryIndex].storyImage.toString(), storyImage
                            )
                            //왼쪽 이동
                            goLeftStory.setOnClickListener {
                                if (currentStoryIndex - 1 >= 0 && isAuto) {
                                    currentStoryIndex -= 1
                                }
                                isAuto = false
                            }
                            //오른쪽 이동
                            goRightStory.setOnClickListener {
                                if (currentStoryIndex + 1 < maxStoryIndex && isAuto) {
                                    currentStoryIndex += 1
                                }
                                isAuto = false
                            }
                        }
                        progressBar.progress = 0
                        while (progressBar.progress < 100) {
                            if (!isAuto) { break }
                            Thread.sleep(100) //1000=1초
                            progressBar.incrementProgressBy(2)
                        }
                        if (isAuto) {
                            if (currentStoryIndex + 1 < maxStoryIndex) {
                                currentStoryIndex += 1
                            }
                        } //오토모드라면+1
                    }
                //0.1초당 2씩 증가 -> 1초당 20증가 -> 5초에 100  progressbar가 100이 되면 다음
                //여기서 스토리 뷰어 추가 (상대 스토리 본것에 내 이름 추가 )
            }
            //모든 스토리 VIEW -> 메인으로 돌아가기
//            userActivity.onFragmentGoBack(this@StoryFragment)

        }catch (e :Exception){ }
    }
    private fun setUpListener(){
        //내 스토리인 경우만 보기
        if(userStoryName==username){
            //스토리 삭제 , 시청기록 보기
            moreInfo.setOnClickListener { view->
                val currentId = userStoryList[currentStoryIndex].storyId
                val popupMenu = PopupMenu(userActivity.applicationContext, view)
                userActivity.menuInflater.inflate(R.menu.pop_up_story,popupMenu.menu)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener { menu->
                    when(menu.itemId){
                        R.id.del_story->{ //스토리 삭제
                            storyManager.deleteStory(
                                currentId, paramFunc = {
                                    if (it) { //삭제 성공
                                        userActivity.onFragmentGoBack(this@StoryFragment)
                                    }
                                })
                            return@setOnMenuItemClickListener true
                        }
                        else->{ // 시청 기록 보기
                            userActivity.onFragmentChange(ViewListFragment(null,userStoryList[currentStoryIndex].storyViewerPost))
                            return@setOnMenuItemClickListener true
                        }                }
                }
            }

        }

        closeButton.setOnClickListener {
            userActivity.onFragmentGoBack(this@StoryFragment)
        }

    }
}
