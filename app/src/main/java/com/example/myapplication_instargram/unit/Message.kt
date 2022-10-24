package com.example.myapplication_instargram.unit

import java.io.Serializable

class Message(
    var messageId : Int,
    var messageRoomId : Int,
    var writer : String,
    var uploadTime : String,
    var body : String?,
    var messageImage : String?
): Serializable