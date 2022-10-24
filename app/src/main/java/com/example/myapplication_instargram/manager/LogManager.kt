package com.example.myapplication_instargram.manager

import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.SearchLog
import com.example.myapplication_instargram.unit.SearchLogGET
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LogManager (
    masterApp : MasterApplication
){
    //마스터 어플리케이션을 받음
    private val masterApplication = masterApp

    //로그 추가
    fun addLog(username:String,log : String,
               paramFunc:(Boolean)->Unit){
        masterApplication.service.saveLog(
            username,log
        ).enqueue(object : Callback<SearchLog> {
            override fun onResponse(call: Call<SearchLog>, response: Response<SearchLog>) {
                if (response.isSuccessful) { paramFunc(true) }
                else{ paramFunc(false)}
            }
            override fun onFailure(call: Call<SearchLog>, t: Throwable) { paramFunc(false) }
        })
    }

    //로그 삭제
    fun delLog(id : String,paramFunc:(Boolean)->Unit){
        masterApplication.service.delLog(
            id
        ).enqueue(object : Callback<SearchLog> {
            override fun onResponse(call: Call<SearchLog>, response: Response<SearchLog>) {
                if (response.isSuccessful) { paramFunc(true) }
                else{ paramFunc(false)}
            }
            override fun onFailure(call: Call<SearchLog>, t: Throwable) { paramFunc(false) }
        })
    }

    //사용자의 변환 로그 데이터 불러오기
    fun getTransformLog(username:String,paramFunc:(ArrayList<SearchLogGET>)->Unit){
        masterApplication.service.transformLog(username= username)
            .enqueue(object :Callback<ArrayList<SearchLogGET>>{
                override fun onResponse(
                    call: Call<ArrayList<SearchLogGET>>,
                    response: Response<ArrayList<SearchLogGET>>
                ) {
                    //사용자 변환 로그를 반환
                    if(response.isSuccessful){
                        response.body()?.let { paramFunc(it) }
                    }
                }
                override fun onFailure(call: Call<ArrayList<SearchLogGET>>, t: Throwable) {}
            })
    }

}