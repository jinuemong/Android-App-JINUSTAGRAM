package com.example.myapplication_instargram

import android.annotation.SuppressLint
import android.content.Context

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.userFragment.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserActivity : AppCompatActivity() {
    lateinit var frManager: FragmentManager
    lateinit var username :String
    var targetUserName  = ""
    lateinit var viewClickAnimate: Animation
    lateinit var viewChangeAnimateUp:Animation
    lateinit var viewChangeAnimateLeft:Animation
    lateinit var pagerAdapter: FragmentAdapter
    lateinit var viewPager : ViewPager2
    private lateinit var tabUser : TabLayout
    lateinit var masterApp : MasterApplication
    lateinit var baseUrl :String
//    var fragmentStack  = ArrayList<Fragment>()
    var type: Int = 0
    private val tintColor= ColorStateList(
    arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(-android.R.attr.state_selected)),
        //위의 결과를 아래로 파서~ 클릭, 노클릭 - 아이콘 뷰의 이미지 색 변경 용
    intArrayOf(Color.parseColor("#FF000000"),Color.parseColor("#FF6B99B3"))
    )

    //아이콘 뷰 이미지 삽입
    private val iconView = arrayOf(
        R.drawable.ic_baseline_home_24,
        R.drawable.ic_baseline_search_24,
        R.drawable.ic_baseline_person_search_24,
        R.drawable.ic_baseline_videocam_24,
        R.drawable.ic_baseline_face_24,
    )
    override fun onResume() {
        super.onResume()
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        username = intent.getStringExtra("username").toString()
        masterApp = application as MasterApplication
        baseUrl = masterApp.baseUrl
        frManager = this@UserActivity.supportFragmentManager
        val isType  = intent.getStringExtra("type")
        if(isType!=null){ type=isType.toInt() }
        //애니메이션 어댑터
        viewClickAnimate =  AnimationUtils.loadAnimation(this@UserActivity,R.anim.wave)
        viewChangeAnimateUp =  AnimationUtils.loadAnimation(this@UserActivity,R.anim.enter_up)
        viewChangeAnimateLeft =  AnimationUtils.loadAnimation(this@UserActivity,R.anim.enter_left)
        //페이지 어댑터
        pagerAdapter = FragmentAdapter(this@UserActivity)
        pagerAdapter.addFragment(Home())
        pagerAdapter.addFragment(Search())
        pagerAdapter.addFragment(Explore())
        pagerAdapter.addFragment(FragmentVideo())
        pagerAdapter.addFragment(FragmentUserUI())
        viewPager = findViewById(R.id.view_user_view_pager)
        tabUser = findViewById(R.id.tab_user)
        viewPager.adapter = pagerAdapter

        //페이저 선택 시 해당 포지션으로 이동
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                viewPager.isUserInputEnabled = true
                super.onPageSelected(position)
            }
        })
        if (type!=0){
            viewPager.currentItem = type
            type=0
        }
        tabUser.tabIconTint = tintColor
        //tabLayout attach
        TabLayoutMediator(tabUser,viewPager){ tab, position ->
            tab.setIcon(iconView[position])
        }.attach()
    }
    fun onTestChange(fromFragment: Fragment,goFragment: Fragment){
        frManager.beginTransaction().add(R.id.view_container,goFragment)
            .addToBackStack(null)
            .commit()
        frManager.beginTransaction().hide(fromFragment).commit()
        frManager.beginTransaction().show(goFragment).commit()
    }
    //typ는 현재 useractivty의 실행 화면
    fun onFragmentChange(goFragment: Fragment){
//        Log.d("프래그먼트 추가 fragmentChange",currentFragment.toString())
//        fragmentStack.add(currentFragment)
//        Log.d("프래그먼트 스택 ",fragmentStack.toString())
//        //현재 인덱스를 보내줘서 go Fragment 로 교체 -> 인덱스는 그대로 유지
//        val index = viewPager.currentItem
//        pagerAdapter.changeFragment(index,goFragment)
//        viewPager.adapter = pagerAdapter
//        viewPager.currentItem = index
//        //아래가 일반적인 프래그먼트 교체 방법이지만 viewpager2에서는 replace를
//        // 사용 할 수 없다 따라서 새로운 fragment manager를 제작
////        frManager.beginTransaction()
////            .replace(R.id.view_container,goFragment)
////            .commitNow()

        frManager.beginTransaction().replace(R.id.view_container,goFragment)
            .addToBackStack(null)
            .commit()
    }
    fun onFragmentGoBack(fragment: Fragment){
//        if (fragmentStack.size>0) {
//            val fr = fragmentStack[fragmentStack.lastIndex]
//            fragmentStack.removeLast()
//            val index = viewPager.currentItem
//            pagerAdapter.changeFragment(index, fr)
//            viewPager.adapter = pagerAdapter
//            viewPager.currentItem = index
//        }else{
//            frManager.beginTransaction().remove(fragment).commitNow()
//            frManager.popBackStack()
//        }
        frManager.beginTransaction().remove(fragment).commit()
        frManager.popBackStack()

    }

    @SuppressLint("SimpleDateFormat")
    fun getTranslatedDate(uploadTime : String) : String{
        //날짜 데이터 비교 코드
        //날짜 데이터 받아와서 넣어주기
        val uploadTimeStamp = uploadTime.split("-", "T", ":", ".")
        //("yyyy-MM-dd HH:mm:ss")
        val time =uploadTimeStamp[0] + "-" +uploadTimeStamp[1]+"-"+uploadTimeStamp[2]+
                " "+uploadTimeStamp[3]+":"+uploadTimeStamp[4]+":"+uploadTimeStamp[5].chunked(2)[0]
        val sf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val data = sf.parse(time)
        val today = Calendar.getInstance()
        val calDate = (today.time.time-data!!.time) //현재, 저장 차이
        return if ((calDate/(60*60*24*1000))>=1){ //1일 이상
            (calDate/(60*60*24*1000)).toInt().toString()+" 일 전"
        }else if ((calDate/(60*60*1000))>=1){ //1시간 이상
            (calDate/(60*60*1000)).toInt().toString() + " 시간 전"
        }else if((calDate/(60*1000))>=1){ //분 단위
            (calDate/(60*1000)).toInt().toString() + " 분 전"
        }else{
            "몇초 전"
        }
    }

    fun setRectImage(imageString : String,view:ImageView){
        try {
            if (imageString.contains(":")) {
                Glide.with(this@UserActivity)
                    .load(Uri.parse(imageString))
                    .override(view.width,view.height)
                    .into(view)
            } else {
                Glide.with(this@UserActivity)
                    .load(Uri.parse(baseUrl + imageString))
                    .override(view.width,view.height)
                    .into(view)
            }
        }catch (e:Exception){}
    }
    fun setCircleImage(imageString : String,view:CircleImageView){
        try {
            if (imageString.contains(":")) {
                Glide.with(this@UserActivity)
                    .load(Uri.parse(imageString))
                    .override(view.width,view.height)
                    .into(view)
            } else {
                Glide.with(this@UserActivity)
                    .load(Uri.parse(baseUrl + imageString))
                    .override(view.width,view.height)
                    .into(view)
            }
        }catch (e:Exception){}
    }
}

//class AddPosterAdapterDecoration(context : Context) : RecyclerView.ItemDecoration(){
//    //아이템 간격 설정에 도움을 줌
//    val userActivity =context
//    override fun getItemOffsets(
//        outRect: Rect,
//        view: View,
//        parent: RecyclerView,
//        state: RecyclerView.State
//    ) {
//        super.getItemOffsets(outRect, view, parent, state)
//    }
//
//}

//view의 너비에 맞춰서 높이를 조절하는 함수
fun setAutoSizeView(view:View){
    //파라미터로 받아온 view에 w,h를 적용
    val vto = view.viewTreeObserver
    vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            val w = view.measuredWidth
            view.layoutParams = FrameLayout.LayoutParams(w, w)

        }

    })
}