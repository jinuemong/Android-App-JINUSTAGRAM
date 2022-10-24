package com.example.myapplication_instargram.manager




import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.Follow
import com.example.myapplication_instargram.unit.MiniProfiles
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowManager(
    masterApp : MasterApplication
){
    //마스터 어플리케이션을 받음
    private val masterApplication = masterApp
    // 미니 프로필 받기
    fun takeMiniProfile(username:String,paramFunc: (MiniProfiles?)->Unit){
        masterApplication.service.getUserProfileMini(username)
            .enqueue(object : Callback<ArrayList<MiniProfiles>>{
                override fun onResponse(
                    call: Call<ArrayList<MiniProfiles>>,
                    response: Response<ArrayList<MiniProfiles>>
                ) {
                    if (response.isSuccessful && response.body()!=null){
                        paramFunc(response.body()!![0])
                    }else{
                        paramFunc(null)
                    }
                }
                override fun onFailure(call: Call<ArrayList<MiniProfiles>>, t: Throwable) {
                    paramFunc(null)
                }
            })
    }

    //팔로워 추가
    fun addFollower(username: String, owner: String,
                    paramFunc : (Boolean)->Unit)  { //콜백 함수로 우선 실행
        masterApplication.service.addFollower(
            owner, username
        ).enqueue(object : Callback<Follow> {
            override fun onResponse(call: Call<Follow>, response: Response<Follow>) {
                if (response.isSuccessful) {
                    Log.d("addFollower: Response", response.body()?.owner.toString())
                    paramFunc(true)
                }
                else{ paramFunc(false)}
            }
            override fun onFailure(call: Call<Follow>, t: Throwable) {paramFunc(false)}
        })
    }

    //팔로잉 추가
    fun addFollowing(username: String, owner: String,
                     paramFunc : (Boolean)->Unit){
        masterApplication.service.addFollowing(
            username, owner
        ).enqueue(object : Callback<Follow> {
            override fun onResponse(call: Call<Follow>, response: Response<Follow>) {
                if (response.isSuccessful) {
                    Log.d("addFollowing: Response", response.body()?.owner.toString())
                    paramFunc(true)
                }else{ paramFunc(false) }
            }
            override fun onFailure(call: Call<Follow>, t: Throwable) {paramFunc(false)}
        })
    }

    //팔로워 제거
    fun delFollower(username: String, owner: String,
                    paramFunc : (Boolean)->Unit)  { //콜백 함수로 우선 실행
        masterApplication.service.delFollower(
            owner,username
        ).enqueue(object : Callback<Follow> {
            override fun onResponse(call: Call<Follow>, response: Response<Follow>) {
                if (response.isSuccessful) {
                    Log.d("delFollower: Response", response.body()?.owner.toString())
                    paramFunc(true)
                }
                else{ paramFunc(false)}
            }
            override fun onFailure(call: Call<Follow>, t: Throwable) {paramFunc(false)}
        })
    }

    //팔로잉 제거
    fun delFollowing(username: String, owner: String,
                     paramFunc : (Boolean)->Unit){
        masterApplication.service.delFollowing(
            username, owner
        ).enqueue(object : Callback<Follow> {
            override fun onResponse(call: Call<Follow>, response: Response<Follow>) {
                if (response.isSuccessful) {
                    Log.d("delFollowing: Response", response.body()?.owner.toString())
                    paramFunc(true)
                }else{ paramFunc(false) }
            }
            override fun onFailure(call: Call<Follow>, t: Throwable) {paramFunc(false)}
        })
    }

    //팔로잉 중인지 확인
    fun isFollowing(username:String,toUser:String,paramFunc : (Boolean)->Unit){
        masterApplication.service.isFollowing(username,toUser).enqueue(object :Callback<ArrayList<Follow>>{
            override fun onResponse(call: Call<ArrayList<Follow>>, response: Response<ArrayList<Follow>>) {
                if (response.isSuccessful){
                    if (response.body()!!.size!=0){ paramFunc(true) }
                    else{ paramFunc(false) }
                }
        }
            override fun onFailure(call: Call<ArrayList<Follow>>, t: Throwable) {  }
        })
    }

    //팔로워 인지 확인
    fun isFollower(username:String,fromUser:String,paramFunc : (Boolean)->Unit){
        masterApplication.service.isFollower(username,fromUser).enqueue(object :Callback<ArrayList<Follow>>{
            override fun onResponse(call: Call<ArrayList<Follow>>, response: Response<ArrayList<Follow>>) {
                if (response.isSuccessful){
                    if (response.body()!!.size!=0){ paramFunc(true) }
                    else{ paramFunc(false) }
                }else { paramFunc(false) }
            }
            override fun onFailure(call: Call<ArrayList<Follow>>, t: Throwable) {  }
        })
    }
    //팔로워 리스트를 얻음
    fun getFollowerList(userProfile:String, paramFunc: (ArrayList<MiniProfiles>) -> Unit){
        masterApplication.service.getFollowerList(userProfile).enqueue(object :Callback<ArrayList<MiniProfiles>>{
            override fun onResponse(
                call: Call<ArrayList<MiniProfiles>>, response: Response<ArrayList<MiniProfiles>>
            ) { if (response.isSuccessful){ paramFunc(response.body()!!)}
            }override fun onFailure(call: Call<ArrayList<MiniProfiles>>, t: Throwable) {}
        })
    }

    //팔로잉 리스트를 얻음
    fun getFollowingList(userProfile:String, paramFunc: (ArrayList<MiniProfiles>) -> Unit){
        masterApplication.service.getFollowingList(userProfile).enqueue(object :Callback<ArrayList<MiniProfiles>>{
            override fun onResponse(
                call: Call<ArrayList<MiniProfiles>>, response: Response<ArrayList<MiniProfiles>>
            ) { if (response.isSuccessful){ paramFunc(response.body()!!)}
            }override fun onFailure(call: Call<ArrayList<MiniProfiles>>, t: Throwable) {}
        })
    }

    fun setButtonsListener(
        type : Int, // 버튼의 type1 : 팔로워 관리, type2: 팔로잉 관리
        button : Button,
        user: String,
        owner : String,
        paramFunc:(Boolean)->Unit
    ) {

        if (user==owner) {button.visibility = View.GONE} //자신 팔로우 불가
        if (type==1){ changeButtonFollow( user, owner,button)  }
                else{ changeButtonFollowing(user,owner,button) }

        button.setOnClickListener {
            if(button.currentTextColor ==Color.parseColor("#62BEF3")){
                //언팔로잉중 -> 팔로잉
                addFollower(user, owner,
                    paramFunc = { followSuccess ->
                        addFollowing( user, owner,
                            paramFunc = { followingSuccess ->
                                if (followSuccess and followingSuccess) {
                                    if (type==1){ changeButtonFollow( user, owner,button) }
                                    else{ changeButtonFollowing(user,owner,button) }
                                    paramFunc(true) //팔로우
                                }
                            })
                    })

            }else{
                //팔로잉 중 -> 언 팔로우
                delFollower(user, owner,
                    paramFunc = { followSuccess ->
                        delFollowing(user, owner,
                            paramFunc = { followingSuccess ->
                                if (followSuccess and followingSuccess) {
                                    if (type==1){ changeButtonFollow( user, owner,button)}
                                    else{ changeButtonFollowing(user,owner,button) }
                                    paramFunc(false) //언 팔로우
                                }
                            })
                    })
            }
        }
    }

    private fun changeButtonFollow(username:String,fromUser: String, button:Button){
        isFollower(username,fromUser, paramFunc = { isFollower->
            if(isFollower){
                button.setTextColor(Color.parseColor("#FF6B99B3"))
                if (username==fromUser){button.text = "삭제"}
                else{button.text = "팔로잉"}
            }
            else{
                button.setTextColor(Color.parseColor("#62BEF3"))
                button.text = "팔로우"
            }
        })
    }

    private fun changeButtonFollowing(username:String,targetUser : String, button:Button){
        isFollowing(username, targetUser, paramFunc = { isFollowing ->
                if (isFollowing){
                    button.setTextColor(Color.parseColor("#FF6B99B3"))
                    button.text = "팔로잉"
                }
                else{
                    button.setTextColor(Color.parseColor("#62BEF3"))
                    button.text = "팔로우"
                }
            })
    }
}