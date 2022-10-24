package com.example.myapplication_instargram.unit


import java.io.Serializable

class Profile (
    var id : String? = null,
    var username : String? = null,
    var customName : String? =null,
    var userImage : String?= null,
    var userComment : String? = null,
    var posterCount : Int? = null,
    var followingCount: Int? = null,
    var followerCount: Int? = null,
    var posterPost : ArrayList<Poster>?=null,
    var storyPost : ArrayList<Story>?=null,
    ):Serializable

