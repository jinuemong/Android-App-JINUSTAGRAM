package com.example.myapplication_instargram.unit

import java.io.Serializable

class Poster (
    var posterId : Int,
    var username : String,
    var body : String? = null,
    val uploadTime : String,
    var imagePost : ArrayList<OneImage>?,
    var commentPost : ArrayList<Comment>?,
    var likePost : ArrayList<Like>?,
    val commentCount :Int,
    val likeCount:Int,
    val imageCount:Int,
): Serializable