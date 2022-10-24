package com.example.myapplication_instargram.unit

import java.io.Serializable

class MiniProfiles (
    var username : String,
    var userImage : String,
    var customName : String,
    var storyCount : Int,
    var storyPost : ArrayList<Story>?=null,
):Serializable