package com.example.myapplication_instargram.unit

import java.io.Serializable

class MessageList(
    var messageRoomId : Int,  //메시지룸 넘버
    var messagePost : ArrayList<Message>?,  // 메시지 리스트
    var messageUserPost:ArrayList<MessageUser>?
): Serializable