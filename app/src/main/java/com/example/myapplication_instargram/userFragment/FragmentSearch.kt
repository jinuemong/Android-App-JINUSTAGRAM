package com.example.myapplication_instargram.userFragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication_instargram.manager.LogManager
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.followFragment.UserProfile
import com.example.myapplication_instargram.unit.MiniProfiles
import com.example.myapplication_instargram.unit.SearchDataMultiMode
import com.example.myapplication_instargram.unit.SearchDataMultiMode.Companion.item_mini_profile
import com.example.myapplication_instargram.unit.SearchDataMultiMode.Companion.item_search_log
import com.example.myapplication_instargram.unit.SearchLog
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private lateinit var username: String
private lateinit var userActivity: UserActivity
private lateinit var seRecycler: RecyclerView
private lateinit var masterApp: MasterApplication
private lateinit var  logManager :LogManager
class Search : Fragment() {
    //activity의 context 사용을 위함
    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        username = (activity as UserActivity).username
        masterApp = ((activity as UserActivity).application as MasterApplication)
        logManager = LogManager(masterApp)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seRecycler = view.findViewById(R.id.recently_search_recycler)
        initTransFormLogRecycler()

        //검색 뷰 설정
        val searchView = view.findViewById<SearchView>(R.id.se_search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String): Boolean {
                //제출이 된다면 검색로고 등록
                if (p0 != "") {
                    logManager.addLog(username,p0, paramFunc = {
                        if (it){searchingLogProfileRecycler(p0) }
                    })
                }else{
                    initTransFormLogRecycler()
                }
                return false
            }
            override fun onQueryTextChange(p0: String): Boolean {
                if (p0 != "") {
                    searchingLogProfileRecycler(p0) //검색 중에 검색 결과 확인
                }else{
                    initTransFormLogRecycler() //검색 종료 시 원래 쿼리로
                }
                return false
            }
        })
    }

}

//검색 기록 or 검색 중  리싸이클러(유저 정보 or 단순 로그를 받음)
class SearchLogRecyclerViewAdapter(
    private val itemList: List<SearchDataMultiMode>, //멀티 모델 중에서 선택해서 받음
    private val inflater: LayoutInflater,
    private val masterApp: MasterApplication,
    private val type : Int // type1 : 검색 기록 , typ2 : 검색 중
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return itemList[position].type
    }
    //유저 정보 번환
    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemButton: Button
        val itemUsername: TextView
        val itemCustomName: TextView
        val itemGetUserProfile : LinearLayout
        val itemUserImage: CircleImageView
        val closeButton: ImageView

        init {
            itemGetUserProfile  = itemView.findViewById(R.id.Lin_item_ex_username)
            itemUsername = itemView.findViewById(R.id.item_ex_username)
            itemUserImage = itemView.findViewById(R.id.item_ex_user_image)
            itemCustomName = itemView.findViewById(R.id.item_ex_custom_name)
            itemButton = itemView.findViewById(R.id.item_ex_user_following_button)
            itemButton.visibility = View.GONE
            closeButton = itemView.findViewById(R.id.item_ex_X_button)
            //검색 기록 일 때 만 x 버튼 보여줌
            if (type==1){ closeButton.visibility = View.VISIBLE }
        }
    }

    //단순 로고 반환
    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLog: TextView
        val closeButton: ImageView
        init {
            itemLog = itemView.findViewById(R.id.item_ex_log)
            closeButton = itemView.findViewById(R.id.item_ex_X_button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            item_mini_profile -> {
                //viewType = 0일 때 유저의 프로필을 반환한다
                //유저 프로필
                view = inflater.inflate(R.layout.item_mini_profile, parent, false)
                ProfileViewHolder(view)
            }
            item_search_log -> {
                //viewType = 1 단순 로고
                view = inflater.inflate(R.layout.item_search_log, parent, false)
                LogViewHolder(view)
            }
            else -> throw RuntimeException("알 수 없는 뷰 타입 ")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (itemList[position].type) {
            //프로필 이미지의 타입을 받아옴
            item_mini_profile -> {
                val viewHolder = (holder as ProfileViewHolder)
                viewHolder.itemUsername.text = itemList[position].username

                userActivity.setCircleImage(itemList[position].userImage,viewHolder.itemUserImage)

                viewHolder.itemCustomName.text = itemList[position].customName

                viewHolder.closeButton.setOnClickListener {
                    //x 버튼 클릭시 삭제
                    logManager.delLog(itemList[position].id.toString(), paramFunc = {
                        if (it){initTransFormLogRecycler() }
                    })
                }
                //유저 이름 + 커스텀 클릭 시 유저 프로필로 연결 + log 등록
                viewHolder.itemGetUserProfile.setOnClickListener {
                    //만약에 검색 중에 클릭 했으면 log 등록
                    if (type==2){
                        logManager.addLog(username,itemList[position].username, paramFunc = {})
                    }
                    userActivity.onFragmentChange(UserProfile(itemList[position].username))

                }

                viewHolder.itemUserImage.setOnClickListener {

                } //유저 스토리 클릭 - >유저 스토리 연결
            }
            //단순 검색 타입(로그만)을 받아옴
            item_search_log -> {
                val viewHolder = (holder as LogViewHolder) //로그 홀더
                viewHolder.itemLog.text = itemList[position].log
                viewHolder.itemLog.setOnClickListener {
                    // 로그 클릭 시 재 검색 - > 로그에 추가
                }
                viewHolder.closeButton.setOnClickListener {
                    //x 버튼 클릭시 삭제
                    masterApp.service.delLog(itemList[position].id.toString())
                        .enqueue(object : Callback<SearchLog> {
                            override fun onResponse(
                                call: Call<SearchLog>,
                                response: Response<SearchLog>
                            ) { initTransFormLogRecycler() }
                            override fun onFailure(call: Call<SearchLog>, t: Throwable) {}
                        })
                }
            }
            else -> {}
        }
    }
    override fun getItemCount(): Int {
        return itemList.size
    }
}
//검색 기록 리싸이클러 - 프로필이 있다면 변환
private fun initTransFormLogRecycler(){
    logManager.getTransformLog(username, paramFunc= { logList ->
        val multiList = ArrayList<SearchDataMultiMode>()
        for (searchLog in  logList) {
            val isProfile = searchLog.log==null

            if (isProfile){
                multiList.add(SearchDataMultiMode(
                    item_mini_profile,
                    searchLog.id!!.toInt(),
                    searchLog.username!!, "",
                    searchLog.userImage!!,
                    searchLog.customName!!))
            }else{
                multiList.add(SearchDataMultiMode(
                    item_search_log,
                    searchLog.id!!.toInt(),
                    searchLog.username_id!!, searchLog.log!!,
                    "", ""))
            }
        }
        //어댑터 연결
        val adapter = SearchLogRecyclerViewAdapter(
            multiList,
            LayoutInflater.from(userActivity), masterApp,1
        )
        seRecycler.adapter = adapter
        seRecycler.layoutManager = LinearLayoutManager(userActivity)
    })
}

//검색 결과 or 검색 중 리싸이클러
private fun searchingLogProfileRecycler(p0:String) {
    getProfile ( p0,paramFunc ={ profileList->
        val multiList = ArrayList<SearchDataMultiMode>()
        for (searchProfile in  profileList) {
            multiList.add(SearchDataMultiMode(
                SearchDataMultiMode.item_mini_profile,
                0, //사용 안함 -> null값
                searchProfile.username, "",
                searchProfile.userImage,
                searchProfile.customName))

        }
        //어댑터 연결
        val adapter = SearchLogRecyclerViewAdapter(
            multiList,
            LayoutInflater.from(userActivity), masterApp,2
        )
        seRecycler.adapter = adapter
        seRecycler.layoutManager = LinearLayoutManager(userActivity)
    })
}


//검색중인 프로필을 얻음 init(검색창)
fun getProfile(p0:String,paramFunc:(ArrayList<MiniProfiles>)->Unit){
    masterApp.service.searchingLogProfile(search1=p0, search2 = p0)
        .enqueue(object :Callback<ArrayList<MiniProfiles>>{
            override fun onResponse(
                call: Call<ArrayList<MiniProfiles>>, response: Response<ArrayList<MiniProfiles>>
            ) {
                if(response.isSuccessful){
                    response.body()?.let { paramFunc(it) }
                }
            }
            override fun onFailure(call: Call<ArrayList<MiniProfiles>>, t: Throwable) {}
        })
}
