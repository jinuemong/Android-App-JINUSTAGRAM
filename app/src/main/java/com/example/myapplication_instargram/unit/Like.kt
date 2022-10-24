package com.example.myapplication_instargram.unit

import java.io.Serializable

class Like (
    var likeId : Int,
    var posterId : Int,
    var liker : String,
    var likerImage: String,
    var uploadTime : String?= null
): Serializable