package com.example.myapplication_instargram.unit

data class SearchDataMultiMode(
    val type:Int,
    val id : Int,
    val username:String,
    val log:String,
    val userImage:String,
    val customName:String
    ){
    companion object {
        const val item_mini_profile = 0
        const val item_search_log = 1
    }
}
