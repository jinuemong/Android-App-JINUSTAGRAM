package com.example.myapplication_instargram.unit

import java.io.Serializable

//userid와 토큰을 받기 위한 객체 생성

data class User(
    var username : String?  = null,
    var last_login : String? = null,
    var token : String? = null
) : Serializable //상속