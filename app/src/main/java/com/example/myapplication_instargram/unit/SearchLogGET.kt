package com.example.myapplication_instargram.unit

import java.io.Serializable

class SearchLogGET (
        var id : String?,
        var username_id : String?,
        var log: String?, //로그 받음
        var username : String?,
        var userImage : String?,
        var customName : String?, //유저 받음
): Serializable