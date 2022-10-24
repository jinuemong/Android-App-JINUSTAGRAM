package com.example.myapplication_instargram.unit

import java.io.Serializable

class MessageRoom(
    var messageRoomId : Int,  //메시지룸 넘버
    var userPost : ArrayList<MessageUser>?,  // 룸 유저 리스트
):Serializable