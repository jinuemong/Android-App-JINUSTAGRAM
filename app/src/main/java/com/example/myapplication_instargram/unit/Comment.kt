package com.example.myapplication_instargram.unit

import java.io.Serializable

class Comment (
    var commentId : Int,
    var posterId : Int,
    var writer : String,
    var writerImage: String,
    var uploadTime : String? = null,
    var body : String? = null
): Serializable