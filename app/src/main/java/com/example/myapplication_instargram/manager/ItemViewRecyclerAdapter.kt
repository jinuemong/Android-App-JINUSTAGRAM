package com.example.myapplication_instargram.manager

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.followFragment.UserProfile
import com.example.myapplication_instargram.unit.MiniProfiles
import com.example.myapplication_instargram.userServeFragment.StoryFragment
import de.hdodenhof.circleimageview.CircleImageView

//itemProfileView 의 어댑터
class ItemViewRecyclerAdapter(
    private val itemList : ArrayList<MiniProfiles>,
    private val inflater : LayoutInflater,
    masterApp : MasterApplication,
    private val username : String,
    private val userActivity: UserActivity,
    private val type : Int, //팔로워, 팔로잉 종류 < 1: 팔로워 2: 팔로잉>
): RecyclerView.Adapter<ItemViewRecyclerAdapter.ViewHolder>(){
    private val followManager = FollowManager(masterApp)
    private lateinit var user :String
    private lateinit var owner :String
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val itemUsername : TextView
        val itemUserImage : CircleImageView
        val itemCustomName : TextView
        val itemButton : Button
        val itemGetUserProfile : LinearLayout
        init{
            itemUsername = itemView.findViewById(R.id.item_ex_username)
            itemUserImage = itemView.findViewById(R.id.item_ex_user_image)
            itemCustomName = itemView.findViewById(R.id.item_ex_custom_name)
            itemButton = itemView.findViewById(R.id.item_ex_user_following_button)
            itemGetUserProfile  = itemView.findViewById(R.id.Lin_item_ex_username)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewRecyclerAdapter.ViewHolder {
        //뷰를 그려주는 곳
        val view = inflater.inflate(R.layout.item_mini_profile,parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ItemViewRecyclerAdapter.ViewHolder, position: Int) {
        //뷰 홀더의 태그 역할 - 헤당 내용 찾기용 (묶어주는 역할)

        //이름
        holder.itemUsername.text = itemList[position].username
        //이미지
        itemList[position].userImage?.let {
            userActivity.setCircleImage(it, holder.itemUserImage)
        }

        if (itemList[position].storyCount>0){
            //스토리가 있다면 색 다르게
            holder.itemUserImage.apply {
                //배경색 지정
                borderColor  = Color.parseColor("#62BEF3")
                // 사이즈 지정
                borderWidth *= 2
            }
            //스토리로 이동
            holder.itemUserImage.setOnClickListener {
                try{
                        userActivity.onFragmentChange(
                            StoryFragment(
                                itemList[position].storyPost!!,
                                itemList[position].username,
                                itemList[position].userImage,
                            )
                        )
                }catch (e:Exception){ }
            }
        }
        //커맨드
        holder.itemCustomName.text = itemList[position].customName

        //버튼 설정
        if (type==1){
            user = itemList[position].username.toString()
            owner = username
        }else{
            user = username
            owner = itemList[position].username.toString()
        }

        //버튼 클릭 리스너
        holder.itemButton.setOnClickListener { super.notifyItemChanged(position) } //변화 적용
        followManager.setButtonsListener(type,holder.itemButton,user,owner, paramFunc = {})

        //프로필 클릭
        holder.itemGetUserProfile.setOnClickListener {
            userActivity.onFragmentChange(UserProfile(itemList[position].username.toString()))

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


}

