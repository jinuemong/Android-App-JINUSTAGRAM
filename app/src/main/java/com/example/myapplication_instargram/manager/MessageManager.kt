package com.example.myapplication_instargram.manager

import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class MessageManager (
    masterApplication: MasterApplication
){
    private val masterApp = masterApplication

    fun createMessageRoom(username:String,targetUserName:String,
    paramFunc:(Int)->Unit){
        //메시지 방 만들기
        //if 3 fail return >  -1: create user fail -2:create targetUser fail -3: create room fail
        masterApp.service.createMessageRoom()
            .enqueue(object : Callback<MessageRoom>{
                override fun onResponse(call: Call<MessageRoom>, response: Response<MessageRoom>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            //메시지룸에 자신 추가
                            masterApp.service.createMessageUser(it.messageRoomId,
                            username,true,0,0,targetUserName)
                                .enqueue(object: Callback<MessageUser>{
                                    override fun onResponse(
                                        call: Call<MessageUser>, response: Response<MessageUser>
                                    ) {
                                        if (response.isSuccessful){
                                            paramFunc(it.messageRoomId)
                                        }else{
                                            paramFunc(-1)
                                        }
                                    }
                                    override fun onFailure(call: Call<MessageUser>, t: Throwable) {
                                        paramFunc(-1)
                                    }
                                })
                            //메시지룸에 상대 추가
                            masterApp.service.createMessageUser(it.messageRoomId,
                            targetUserName,true,0,0,username)
                                .enqueue(object: Callback<MessageUser>{
                                    override fun onResponse(
                                        call: Call<MessageUser>, response: Response<MessageUser>
                                    ) {
                                        if (response.isSuccessful) {
                                            paramFunc(it.messageRoomId)
                                        }else{
                                            paramFunc(-2)
                                        }
                                    }
                                    override fun onFailure(call: Call<MessageUser>, t: Throwable) {
                                        paramFunc(-2)
                                    }
                                })
                        }
                    }else{
                        paramFunc(-3)
                    }
                }
                override fun onFailure(call: Call<MessageRoom>, t: Throwable) {
                    paramFunc(-3)
                }
            })
    }

    //현재 메시지름 존재 확인
    // 존재 하면 연결 , 비 존재 시 생성
    fun isExistMessageRoom(username:String,targetUserName:String,
                           paramFunc:(Int)->Unit){
        masterApp.service.findMessageRoom(username,targetUserName)
            .enqueue(object :Callback<Int>{
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful){
                        response.body()?.let {
                            paramFunc(it)
                        }
                    }else{
                        paramFunc(-2)
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    paramFunc(-2)
                }

            })
    }

    //메시지 방 삭제 -1 : "fail connection"
    fun delMessageRoom(messageRoomId:Int,
                       paramFunc:(Int)->Unit){
        masterApp.service.delMessageRoom(messageRoomId)
            .enqueue(object: Callback<MessageRoom>{
                override fun onResponse(call: Call<MessageRoom>, response: Response<MessageRoom>) {
                    if ( response.isSuccessful){
                        response.body()?.messageRoomId?.let { paramFunc(it) }
                    }else{
                        paramFunc(-1)
                    }
                }
                override fun onFailure(call: Call<MessageRoom>, t: Throwable) {
                    paramFunc(-1)
                }

            })

    }
    //메시지 보내기 (텍스트)  전송 실패 시 id 값으로 -1 전송
    fun sendTextMessage(messageRoomId : Int,username : String,body:String,
    paramFunc: (Message?) -> Unit){
        masterApp.service.sendMessageText(messageRoomId,username,body)
            .enqueue(object : Callback<Message?>{
                override fun onResponse(call: Call<Message?>, response: Response<Message?>) {
                    if (response.isSuccessful){
                        paramFunc(response.body())
                    }else{
                        paramFunc(null)
                    }
                }
                override fun onFailure(call: Call<Message?>, t: Throwable) { paramFunc(null)}
            })
    }
    fun delMessage(messageId :Int,paramFunc: (Int) -> Unit){
        //메시지 삭제 , 전송 실패 시 id 값으로 -1 전송
        masterApp.service.delMessage(messageId)
            .enqueue(object : Callback<Message>{
                override fun onResponse(call: Call<Message>, response: Response<Message>) {
                    if (response.isSuccessful){
                        paramFunc(1)
                    }else{
                        paramFunc(-1)
                    }
                }
                override fun onFailure(call: Call<Message>, t: Throwable) {
                    paramFunc(-1)
                }

            })
    }
    fun getMessageUserList(messageRoomId:Int , paramFunc:(ArrayList<MessageUser>?)->Unit){
        masterApp.service.getMessageUser(messageRoomId)
            .enqueue(object :Callback<ArrayList<MessageUser>>{
                override fun onResponse(
                    call: Call<ArrayList<MessageUser>>,
                    response: Response<ArrayList<MessageUser>>
                ) {
                    if (response.isSuccessful){
                        paramFunc(response.body())
                    }
                }

                override fun onFailure(call: Call<ArrayList<MessageUser>>, t: Throwable) {
                    paramFunc(null)
                }

            })
    }

    //해당 유저의 메시지 룸 리스트 얻기 null 값이 온다면 에러
    fun getMessageRoomList(username:String,paramFunc: (ArrayList<MessageRoomProfile>?) -> Unit){
        masterApp.service.getMessageRoomList(username)
            .enqueue(object : Callback<ArrayList<MessageRoomProfile>>{
                override fun onResponse(
                    call: Call<ArrayList<MessageRoomProfile>>,
                    response: Response<ArrayList<MessageRoomProfile>>
                ) {
                    if (response.isSuccessful){
                        paramFunc(response.body())
                    }else{
                        paramFunc(null)
                    }
                }
                override fun onFailure(call: Call<ArrayList<MessageRoomProfile>>, t: Throwable) {
                    paramFunc(null)
                }

            })
    }


    //메시지 룸의 id 값으로 메시지 리스트 얻기
    fun getMessageList(id:Int,paramFunc: (MessageList?) -> Unit){
        masterApp.service.getMessageList(id)
            .enqueue(object : Callback<MessageList>{
                override fun onResponse(call: Call<MessageList>, response: Response<MessageList>) {
                    if (response.isSuccessful){
                        paramFunc(response.body())
                    }else{
                        paramFunc(null)
                    }
                }
                override fun onFailure(call: Call<MessageList>, t: Throwable) {
                    paramFunc(null)
                }
            })
    }

    //마지막 활성화 메시지 갱신
    fun patchLastMessageId(id:Int,lastMessageId :Int){
        masterApp.service.updateLastMessage(id,lastMessageId)
            .enqueue(object :Callback<MessageUser>{
                override fun onResponse(call: Call<MessageUser>, response: Response<MessageUser>) {}
                override fun onFailure(call: Call<MessageUser>, t: Throwable) {}

            })
    }
    //마지막 읽은 메시지 갱신
    fun patchLastReadMessageId(id:Int,lastReadMessageId :Int){
        masterApp.service.updateLastReadMessage(id,lastReadMessageId)
            .enqueue(object :Callback<MessageUser>{
                override fun onResponse(call: Call<MessageUser>, response: Response<MessageUser>) {}

                override fun onFailure(call: Call<MessageUser>, t: Throwable) {}

            })
    }
    //메시지방 활성화
    fun patchActive(id:Int){
        masterApp.service.activeMessageRoom(id,true)
            .enqueue(object :Callback<MessageUser>{
                override fun onResponse(call: Call<MessageUser>, response: Response<MessageUser>) {}

                override fun onFailure(call: Call<MessageUser>, t: Throwable) {}

            })
    }
    //메시지방 비활성화 - > lastMessageId 도 설정
    fun patchUnActive(id:Int ,lastMessageId:Int){
        masterApp.service.activeMessageRoom(id,false)
            .enqueue(object :Callback<MessageUser>{
                override fun onResponse(call: Call<MessageUser>, response: Response<MessageUser>) {
                    if (response.isSuccessful){
                        patchLastMessageId(id,lastMessageId)
                    }
                }
                override fun onFailure(call: Call<MessageUser>, t: Throwable) {}
            })
    }
}