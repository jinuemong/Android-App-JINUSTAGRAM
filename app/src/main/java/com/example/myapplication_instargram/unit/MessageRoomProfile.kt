package com.example.myapplication_instargram.unit

import java.io.Serializable

//메시지 목록의 프로필 보여주기
class MessageRoomProfile(
    var messageRoomId : Int,  //메시지룸 넘버
    var username : String,
    var userImage : String,
    var lastMessageId:Int,
    var lastMessage : String,
    var lastMessageTime : String,
    var storyCount : Int,
    var storyPost : ArrayList<Story>?=null,
): Serializable