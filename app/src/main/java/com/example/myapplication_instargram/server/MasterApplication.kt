package com.example.myapplication_instargram.server


import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//여기서 설정하면 다른 액티비티에서 사용 가능
class MasterApplication:Application(

) { //액티비티 보다 우선 실행

    lateinit var service: RetrofitService
    val baseUrl = "https://6ce8-211-35-149-70.jp.ngrok.io"
    override fun onCreate() {
        super.onCreate()
        //chrome://inspect/#devices  - 네트워크 통신 확인
        Stetho.initializeWithDefaults(this)
        createRetrofit()
    }

    fun createRetrofit(){
        //토큰을 받기 위해서 헤더를 연결
        val header = Interceptor{
            //인터셉터에서 통신을 가로채서 우선 실행
            val original  = it.request() //여기서 통신을 잡아둠 -> 통신 개조
            if(checkIsLogin()){
                getUserToken().let { token->
                   val request =original.newBuilder()
                        .header("Authorization", "token $token")
                        .build()
                    it.proceed(request)
                }
            }else{
                it.proceed(original)
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(header)
            .addNetworkInterceptor(StethoInterceptor())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .addConverterFactory(GsonConverterFactory.create())
                // 데이터를 파싱 할 converter를 추가
            .client(client)
            .build()
        service = retrofit.create(RetrofitService::class.java)
    }

    fun checkIsLogin() : Boolean {
        val sp = getSharedPreferences("login_sp",Context.MODE_PRIVATE)
        val token = sp.getString("login_sp","null")
        return token!="null"
        //토큰이 default 값으로 설정 된 null이 아니라면 true 리턴
    }

    fun getUserToken() : String? {
        val sp = getSharedPreferences("login_sp",Context.MODE_PRIVATE)
        val token = sp.getString("login_sp","null")
        return if (token == "null") null
        else token
    }
}