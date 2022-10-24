package com.example.myapplication_instargram.unit

import java.io.Serializable

class MessageUser (
    var id : Int,
    var messageRoomId : Int,
    var username : String,
    var isActive : Boolean,
    var lastMessageId:Int, //마지막 활성화 메시지 아이디
    var lastReadMessageId : Int,   //마지막 읽은 메시지 아이디
    var targetUser : String //상대 유저
): Serializable