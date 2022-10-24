package com.example.myapplication_instargram.unit

import android.media.Image

class MessageDataMultiMode (
    val type : Int,
    val id : Int,
    val writer : String,
    val uploadTime : String,
    val body : String, //텍스트 메시지
    val messageImage : String,
){
    companion object{
        const val item_text_message_my = 0
        const val item_image_message_my= 1
        const val item_text_message_other = 2
        const val item_image_message_other= 3
    }
}