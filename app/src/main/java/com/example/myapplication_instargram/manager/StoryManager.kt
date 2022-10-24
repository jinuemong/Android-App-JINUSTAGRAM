package com.example.myapplication_instargram.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.followFragment.UserProfile
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.OneImage
import com.example.myapplication_instargram.unit.Story
import com.example.myapplication_instargram.unit.StoryViewer
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryManager(
    masterApplication: MasterApplication
) {
    private val masterApp = masterApplication

    //스토리 삭제
    //스토리 추가는 viewModel에서 사용
    fun deleteStory(storyId :Int, paramFunc: (Boolean) -> Unit){
        try{
            masterApp.service.delStory(storyId)
                .enqueue(object : Callback<Story> {
                    override fun onResponse(call: Call<Story>, response: Response<Story>) {
                        if (response.isSuccessful){
                            paramFunc(true)
                        }else{
                            paramFunc(false)
                        }
                    }
                    override fun onFailure(call: Call<Story>, t: Throwable) {
                        paramFunc(false)
                    }
                } )
        }catch (e:Exception){
            paramFunc(false)
        }
    }


    //내가 본 스토리인지 확인
    private fun isViewer(storyId:String,viewer:String,paramFunc: (ArrayList<StoryViewer>?) -> Unit){
        masterApp.service.isViewerCheck(viewer, storyId)
            .enqueue(object : Callback<ArrayList<StoryViewer>>{
                override fun onResponse(
                    call: Call<ArrayList<StoryViewer>>,
                    response: Response<ArrayList<StoryViewer>>
                ) {
                    if (response.isSuccessful){
                        paramFunc(response.body())
                    }else{
                        paramFunc(null)
                    }
                }
                override fun onFailure(call: Call<ArrayList<StoryViewer>>, t: Throwable) {
                    paramFunc(null)
                }

            })
    }

    //스토리 보기
    fun addViewer(storyId: Int,username: String){
        isViewer(storyId.toString(),username,paramFunc={ isView->
            if (isView!=null){
                if(isView.size<1) { //뷰어가 없으면 -> 스토리 본 상태 아님
                    masterApp.service.addStoryViewer(storyId, username)
                        .enqueue(object : Callback<StoryViewer> {
                            override fun onResponse(
                                call: Call<StoryViewer>,
                                response: Response<StoryViewer>
                            ) {
                            }

                            override fun onFailure(call: Call<StoryViewer>, t: Throwable) {}
                        })
                }
            }
        })
    }
}

//스토리 뷰어 리싸이클러 -> storyViewerPost
class StoryViewerAdapter(
    private val userActivity: UserActivity,
    private val itemList:ArrayList<StoryViewer>,
    private val inflater: LayoutInflater,
) :RecyclerView.Adapter<StoryViewerAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val userImage : CircleImageView
        val nameView : TextView
        val goProfile:LinearLayout
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
        userActivity.setCircleImage(itemList[position].viewerImage,holder.userImage)
        holder.nameView.text = itemList[position].viewer

        //시청 기록을 닫고 프로필로 이동
        holder.goProfile.setOnClickListener {
            userActivity.onFragmentChange(UserProfile(itemList[position].viewer))
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}