package com.example.myapplication_instargram.manager

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.followFragment.UserProfile
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.setAutoSizeView
import com.example.myapplication_instargram.unit.*
import com.example.myapplication_instargram.userServeFragment.CommentFragment
import com.example.myapplication_instargram.userServeFragment.PosterFragment
import com.example.myapplication_instargram.userServeFragment.ViewListFragment
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class PosterManager( masterApplication: MasterApplication) {
    val masterApp = masterApplication

    fun getPosterList(username : String, paramFunc : (ArrayList<Poster>?)->Unit){
        masterApp.service.getPosterList(username)
            .enqueue(object : Callback<ArrayList<Poster>> {
                override fun onResponse(
                    call: Call<ArrayList<Poster>>,
                    response: Response<ArrayList<Poster>>
                ) {
                    if (response.isSuccessful){
                        paramFunc(response.body())
                    }else{
                        paramFunc(null)
                    }
                }
                override fun onFailure(call: Call<ArrayList<Poster>>, t: Throwable) {
                    paramFunc(null)
                }

            })
    }
    //type1: 내 팔로잉 한 유저의 포스터  type2 : 팔로잉 하지 않은 유저의 포스터
    fun getRandPosterList(type:String,username: String, paramFunc: (ArrayList<PosterRandom>?) -> Unit){
        masterApp.service.getRandomPosterList(type,username)
            .enqueue(object : Callback<ArrayList<PosterRandom>>{
                override fun onResponse(
                    call: Call<ArrayList<PosterRandom>>,
                    response: Response<ArrayList<PosterRandom>>
                ) {
                    if (response.isSuccessful){
                        paramFunc(response.body())
                    }else{
                        paramFunc(null)
                    }
                }
                override fun onFailure(call: Call<ArrayList<PosterRandom>>, t: Throwable) {
                    paramFunc(null)
                }
            })
    }

    fun deletePoster(posterId:Int,paramFunc: (Boolean) -> Unit){
        masterApp.service.delPoster(posterId)
            .enqueue(object :Callback<Poster>{
                override fun onResponse(call: Call<Poster>, response: Response<Poster>) {
                    if (response.isSuccessful){
                        paramFunc(true)
                    }else{paramFunc(false)}
                }

                override fun onFailure(call: Call<Poster>, t: Throwable) {
                    paramFunc(false)
                }

            })
    }
    fun getImageList(posterId:String,paramFunc:(ArrayList<OneImage>?)->Unit){
        masterApp.service.getImageList(posterId)
            .enqueue(object : Callback<ArrayList<OneImage>>{
                override fun onResponse(
                    call: Call<ArrayList<OneImage>>,
                    response: Response<ArrayList<OneImage>>
                ) {
                    if(response.isSuccessful){
                        paramFunc(response.body())
                    }else{
                        paramFunc(null)
                    }
                }
                override fun onFailure(call: Call<ArrayList<OneImage>>, t: Throwable) { paramFunc(null) }
            })
    }
    //좋아요 확인
    fun isLiker(posterId:String,liker:String,paramFunc: (Like?) -> Unit){
        masterApp.service.isLikerCheck(liker, posterId)
            .enqueue(object : Callback<ArrayList<Like>>{
                override fun onResponse(
                    call: Call<ArrayList<Like>>,
                    response: Response<ArrayList<Like>>
                ) {
                    if (response.isSuccessful){
                        if (response.body()!!.size<1){
                            //가짜 liker -> 좋아요 중이 아님
                            paramFunc(Like(-1,-1,",",""))
                        }else{
                            paramFunc(response.body()!![0])
                        }
                    }else{ paramFunc(null) }
                }
                override fun onFailure(call: Call<ArrayList<Like>>, t: Throwable) {
                    paramFunc(null)
                }

            })
    }

    //좋아요 추가 or 삭제 -> 클릭 시 처리 이벤트 0(추가), 1(삭제) ,2(오류)
    fun addOrDelLiker(posterId: String,username: String,paramFunc: (Int) -> Unit){
        isLiker(posterId, username, paramFunc = { isLike ->
            if (isLike != null) {
                if (isLike.likeId==-1) { //좋아요 아님 -> 좋아요
                    masterApp.service.addLiker(posterId, username)
                        .enqueue(object : Callback<Like> {
                            override fun onResponse(
                                call: Call<Like>,
                                response: Response<Like>
                            ) {
                                if (response.isSuccessful) {
                                    paramFunc(0)
                                }else {
                                    paramFunc(2)
                                }
                            }

                            override fun onFailure(call: Call<Like>, t: Throwable) {
                                paramFunc(2)
                            }

                        })
                } else { //좋아요 중 - > 좋아요 삭제
                    masterApp.service.delLiker(isLike.likeId)
                        .enqueue(object :Callback<Like>{
                            override fun onResponse(call: Call<Like>, response: Response<Like>) {
                                if(response.isSuccessful){
                                    paramFunc(1)
                                }else{
                                    paramFunc(2)
                                }
                            }

                            override fun onFailure(call: Call<Like>, t: Throwable) {
                                paramFunc(2)
                            }

                        })
                }
            }
        })
    }

    //댓글 추가
    fun addComment(posterId: String, username: String,body:String, paramFunc: (Comment?) -> Unit) {
        masterApp.service.addComment(posterId, username,body)
            .enqueue(object : Callback<Comment> {
                override fun onResponse(
                    call: Call<Comment>,
                    response: Response<Comment>
                ) {
                    if (response.isSuccessful) {
                        paramFunc(response.body())
                    }
                }

                override fun onFailure(call: Call<Comment>, t: Throwable) {
                    paramFunc(null)
                }

            })
    }

    //댓글 삭제
    fun delUserComment(commentId:Int, paramFunc: (Boolean) -> Unit) {
        masterApp.service.delComment(commentId)
            .enqueue(object : Callback<Comment> {
                override fun onResponse(
                    call: Call<Comment>,
                    response: Response<Comment>
                ) {
                    if (response.isSuccessful) {
                        paramFunc(true)
                    }else{
                        paramFunc(false)
                    }
                }

                override fun onFailure(call: Call<Comment>, t: Throwable) {
                    paramFunc(false)
                }

            })
    }
}


//포스터 구체적인 뷰 - > [miniProfile ,poster] -> posterRand
class RandomPosterAdapter(
    private val userActivity: UserActivity,
    private val itemList: ArrayList<PosterRandom>,
    private val inflater: LayoutInflater,
    private val posterManager: PosterManager,
): RecyclerView.Adapter<RandomPosterAdapter.ViewHolder>(){
    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val itemUserImage: CircleImageView
        val itemUserName: TextView
        val itemUserCustomName: TextView
        val itemPostingImageList : RecyclerView
        val itemHeartButton : ImageView
        val itemCommentListButton : ImageView
        val itemImageCount : TextView
        val itemHeartNum : TextView
        //좋아요 + heartPost + 개
        val itemUserNameInCommend : TextView
        val itemUserCommend: TextView
        val itemAddCommentEdit : EditText
        val itemAddCommentButton : TextView
        val itemUploadTime : TextView
        val moreInfoButton : ImageView
        init {
            itemUserImage = itemView.findViewById(R.id.postring_user_image)
            itemUserName = itemView.findViewById(R.id.posting_user_name)
            itemUserCustomName = itemView.findViewById(R.id.posting_user_custom_name)
            itemPostingImageList = itemView.findViewById(R.id.recycler_posting_images)
            itemHeartButton = itemView.findViewById(R.id.posting_user_heart)
            itemCommentListButton = itemView.findViewById(R.id.posting_user_comment)
            itemImageCount = itemView.findViewById(R.id.item_poster_image_num)
            itemHeartNum = itemView.findViewById(R.id.posting_user_heart_num)
            itemUserNameInCommend = itemView.findViewById(R.id.posting_user_name_commend)
            itemUserCommend = itemView.findViewById(R.id.posting_user_commend)
            itemAddCommentEdit = itemView.findViewById(R.id.posting_user_comment_edit)
            itemAddCommentButton = itemView.findViewById(R.id.posting_user_comment_buttonText)
            itemUploadTime = itemView.findViewById(R.id.posting_upload_time)
            moreInfoButton = itemView.findViewById(R.id.poster_setting)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandomPosterAdapter.ViewHolder {
        val view = inflater.inflate(R.layout.item_posting_poster,parent,false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemList[position].miniProfiles.userImage.let {
            userActivity.setCircleImage(it, holder.itemUserImage)
        }
        holder.itemUserName.text = itemList[position].miniProfiles.username
        holder.itemUserCustomName.text = itemList[position].miniProfiles.customName
        //이미지 연결
        posterManager.getImageList(itemList[position].poster.posterId.toString()
            , paramFunc = {imageList->
                if(imageList!=null) {
                    holder.itemImageCount.text = imageList.size.toString()
                    holder.itemPostingImageList.adapter = PosterImageAdapter(
                        userActivity, imageList, LayoutInflater.from(userActivity)
                    )
                }
        })

        var heartText = "좋아요 "  +itemList[position].poster.likeCount.toString()+" 개"
        holder.itemHeartNum.text  = heartText
        //좋아요 추가 or 삭제 버튼
        posterManager.isLiker(itemList[position].poster.posterId.toString(),userActivity.username, paramFunc = {
            if (it!=null) {
                when (it.posterId) {
                    -1 -> { holder.itemHeartButton.setColorFilter(ContextCompat.getColor(userActivity, R.color.star))}
                    else -> { holder.itemHeartButton.setColorFilter(ContextCompat.getColor(userActivity, R.color.red)) }

                }
            }
        })
        val itemCount = itemList[position].poster.likeCount
        holder.itemHeartButton.setOnClickListener { view->
            view.startAnimation(userActivity.viewClickAnimate)
            posterManager.addOrDelLiker(itemList[position].poster.posterId.toString(),
            userActivity.username, paramFunc = {
                    when (it) {
                        0 -> {
                            holder.itemHeartButton.setColorFilter(ContextCompat.getColor(userActivity,R.color.red))
                            heartText = "좋아요 "  +(itemCount+1).toString()+" 개"
                            holder.itemHeartNum.text  = heartText
                        }
                        1 -> {
                            holder.itemHeartButton.setColorFilter(ContextCompat.getColor(userActivity,R.color.star))
                            heartText = "좋아요 $itemCount 개"
                            holder.itemHeartNum.text  = heartText
                        }
                        else -> {}
                    }
                })
        }

        holder.itemUserNameInCommend.text = itemList[position].miniProfiles.username
        holder.itemUserCommend.text = itemList[position].poster.body

        //댓글 추가
        holder.itemAddCommentButton.setOnClickListener {
            if(holder.itemAddCommentEdit.text.isNotEmpty()){
                posterManager.addComment(itemList[position].poster.posterId.toString(),
                userActivity.username,holder.itemAddCommentEdit.text.toString(), paramFunc = {
                    if (it!=null){holder.itemAddCommentEdit.setText("")}
                    })
            }
        }

        //날짜 데이터 현재 시간과 비교 , 변환해서 넣어주기
        val translateData = userActivity.getTranslatedDate(itemList[position].poster.uploadTime)
        holder.itemUploadTime.text = translateData

        //해당 좋아요 모음 프래그먼트로 이동
        holder.itemHeartNum.setOnClickListener {
            userActivity.onFragmentChange(ViewListFragment(itemList[position].poster.likePost,null))
        }
        //해당 댓글 모음 프래그먼트로 이동
        holder.itemCommentListButton.setOnClickListener {
            userActivity.onFragmentChange(CommentFragment(
                itemList[position].poster.posterId.toString(),
                itemList[position].miniProfiles.username,
                itemList[position].miniProfiles.userImage,
                itemList[position].poster.body.toString(),
                itemList[position].poster.uploadTime,
                itemList[position].poster.commentPost)
            )
        }
    }
    override fun getItemCount(): Int { return itemList.size }
}

//ui에 있는 게시물 item_mini_poster.xml 사용 - 이미지 수가 1개라면 mini_poster_count -> INVISIBLE
class MiniPosterAdapter(
    private val userActivity: UserActivity,
    private val itemList:ArrayList<Poster>,
    private val inflater: LayoutInflater,
) :RecyclerView.Adapter<MiniPosterAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val countImage : ImageView
        val mainImage : ImageView
        init {
            countImage = itemView.findViewById(R.id.mini_poster_count )
            mainImage = itemView.findViewById(R.id.mini_poster_image )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_mini_poster,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //이미지가 1개 미만이면 갯수 나타내는 이미지 삭제
        if(itemList[position].imageCount<=1){
            holder.countImage.visibility = View.INVISIBLE
        }
        //첫번째 이미지를 메인 이미지로 설정
        val mainImage = itemList[position].imagePost?.get(0)?.Oneimage
        if (mainImage!=null) {
            setAutoSizeView(holder.mainImage)
            userActivity.setRectImage(mainImage,holder.mainImage)
        }
        holder.mainImage.setOnClickListener {
            userActivity.onFragmentChange(PosterFragment(itemList[position].username,itemList,position))
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}

//포스터 하나의 이미지들 리싸이클러 -> ImagePost
class PosterImageAdapter(
    private val userActivity: UserActivity,
    private val itemList:ArrayList<OneImage>,
    private val inflater: LayoutInflater,
) :RecyclerView.Adapter<PosterImageAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val image : ImageView
        init {
            image = itemView.findViewById(R.id.one_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_one_image,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemList[position].Oneimage?.let { userActivity.setRectImage(it,holder.image) }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}


//게시물 좋아요  리싸이클러 -> LikePost
class LikerAdapter(
    private val userActivity: UserActivity,
    private val itemList:ArrayList<Like>,
    private val inflater: LayoutInflater,
) :RecyclerView.Adapter<LikerAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val userImage : CircleImageView
        val nameView : TextView
        val goProfile: LinearLayout
        init {
            userImage = itemView.findViewById(R.id.item_rect_profile_image)
            nameView= itemView.findViewById(R.id.item_rect_profile_name)
            goProfile = itemView.findViewById(R.id.item_rect_profile_main)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_circle_rect_mini_profile,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        userActivity.setCircleImage(itemList[position].likerImage,holder.userImage)
        holder.nameView.text = itemList[position].liker

        //시청 기록을 닫고 프로필로 이동
        holder.goProfile.setOnClickListener {
            userActivity.onFragmentChange(UserProfile(itemList[position].liker))
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}

class PosterAdapter(
    private val username:String,
    private val userImage:String,
    private val userCustomName:String,
    private val userActivity: UserActivity,
    private val itemList: ArrayList<Poster>,
    private val inflater: LayoutInflater,
    private val posterManager: PosterManager,
): RecyclerView.Adapter<PosterAdapter.ViewHolder>(){
    @SuppressLint("ClickableViewAccessibility")
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val itemUserImage: CircleImageView
        val itemUserName: TextView
        val itemUserCustomName: TextView
        val itemPostingImageList : RecyclerView
        val itemHeartButton : ImageView
        val itemCommentListButton : ImageView
        val itemImageCount : TextView
        val itemHeartNum : TextView
        //좋아요 + heartPost + 개
        val itemUserNameInCommend : TextView
        val itemUserCommend: TextView
        val itemAddCommentEdit : EditText
        val itemAddCommentButton : TextView
        val itemUploadTime : TextView
        val moreInfoButton : ImageView
        val itemId : TextView
        init {
            itemUserImage = itemView.findViewById(R.id.postring_user_image)
            itemUserName = itemView.findViewById(R.id.posting_user_name)
            itemUserCustomName = itemView.findViewById(R.id.posting_user_custom_name)
            itemPostingImageList = itemView.findViewById(R.id.recycler_posting_images)
            itemHeartButton = itemView.findViewById(R.id.posting_user_heart)
            itemCommentListButton = itemView.findViewById(R.id.posting_user_comment)
            itemImageCount = itemView.findViewById(R.id.item_poster_image_num)
            itemHeartNum = itemView.findViewById(R.id.posting_user_heart_num)
            itemUserNameInCommend = itemView.findViewById(R.id.posting_user_name_commend)
            itemUserCommend = itemView.findViewById(R.id.posting_user_commend)
            itemAddCommentEdit = itemView.findViewById(R.id.posting_user_comment_edit)
            itemAddCommentButton = itemView.findViewById(R.id.posting_user_comment_buttonText)
            itemUploadTime = itemView.findViewById(R.id.posting_upload_time)
            moreInfoButton = itemView.findViewById(R.id.poster_setting)
            itemId = itemView.findViewById(R.id.posting_id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterAdapter.ViewHolder {
        val view = inflater.inflate(R.layout.item_posting_poster,parent,false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SimpleDateFormat", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        userActivity.setCircleImage(userImage, holder.itemUserImage)
        holder.itemUserName.text = username
        holder.itemUserCustomName.text = userCustomName
        //이미지 연결
        posterManager.getImageList(itemList[position].posterId.toString()
            , paramFunc = {imageList->
                if(imageList!=null) {
                    holder.itemImageCount.text = imageList.size.toString()
                    holder.itemPostingImageList.adapter = PosterImageAdapter(
                        userActivity, imageList, LayoutInflater.from(userActivity)
                    )
                }
            })
        holder.itemId.text = itemList[position].posterId.toString()

        var heartText = "좋아요 "  +itemList[position].likeCount.toString()+" 개"
        holder.itemHeartNum.text  = heartText
        //좋아요 추가 or 삭제 버튼
        posterManager.isLiker(itemList[position].posterId.toString(),userActivity.username, paramFunc = {
            if (it!=null) {
                when (it.posterId) {
                    -1 -> { holder.itemHeartButton.setColorFilter(ContextCompat.getColor(userActivity, R.color.star))}
                    else -> { holder.itemHeartButton.setColorFilter(ContextCompat.getColor(userActivity, R.color.red)) }

                }
            }
        })
        holder.itemHeartButton.setOnClickListener { view->
            view.startAnimation(userActivity.viewClickAnimate)
            posterManager.addOrDelLiker(itemList[position].posterId.toString(),
                userActivity.username, paramFunc = {
                    when (it) {
                        0 -> {
                            holder.itemHeartButton.setColorFilter(ContextCompat.getColor(userActivity,R.color.red))
                            heartText = "좋아요 "  +(itemList[position].likeCount+1).toString()+" 개"
                            holder.itemHeartNum.text  = heartText
                        }
                        1 -> {
                            holder.itemHeartButton.setColorFilter(ContextCompat.getColor(userActivity,R.color.star))
                            heartText = "좋아요 "  +itemList[position].likeCount.toString()+" 개"
                            holder.itemHeartNum.text  = heartText
                        }
                        else -> {}
                    }
                })
        }

        holder.itemUserNameInCommend.text = username
        holder.itemUserCommend.text = itemList[position].body

        //댓글 추가
        holder.itemAddCommentButton.setOnClickListener {
            if(holder.itemAddCommentEdit.text.isNotEmpty()){
                posterManager.addComment(itemList[position].posterId.toString(),
                    userActivity.username,holder.itemAddCommentEdit.text.toString(), paramFunc = {
                        if(it!=null){holder.itemAddCommentEdit.setText("")}
                    })
            }
        }

        //날짜 데이터 현재 시간과 비교 , 변환해서 넣어주기
        val translateData = userActivity.getTranslatedDate(itemList[position].uploadTime)
        holder.itemUploadTime.text = translateData

        //해당 좋아요 모음 프래그먼트로 이동
        holder.itemHeartNum.setOnClickListener {
            userActivity.onFragmentChange(ViewListFragment(itemList[position].likePost,null))
        }
        //해당 댓글 모음 프래그먼트로 이동
        holder.itemCommentListButton.setOnClickListener {
            userActivity.onFragmentChange(CommentFragment(
                itemList[position].posterId.toString(),
                username,
                userImage,
                itemList[position].body.toString(),
                itemList[position].uploadTime,
                itemList[position].commentPost)
            )
        }
        //현재 보는 뷰가 본인의 뷰일 때 삭제 가능
        if(userActivity.username==username){
            holder.moreInfoButton.visibility=View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    private fun sendPosition(){

    }
}