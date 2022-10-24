package com.example.myapplication_instargram

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.myapplication_instargram.manager.PopAdManager
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    lateinit var insertButton : Button
    lateinit var loginButton : Button
    lateinit var userNameView : EditText
    lateinit var userPwView : EditText
    lateinit var mainIntent : Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView(this@MainActivity)
        setUpListener()
    }

    private fun setUpListener(){
        loginButton.setOnClickListener {
            login(this@MainActivity)
        }
        insertButton.setOnClickListener {
            mainIntent = Intent(this@MainActivity,InsertActivity::class.java)
            startActivity(mainIntent)
        }
    }

    //로그인 구현
    private fun login(activity: Activity){
        val username = getUserName()
        val password = getUserPass()
        val popAd = PopAdManager(this@MainActivity)
        (application as MasterApplication).service.login(
            username,password
        ).enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful){ //응답을 잘 받음
                    val user = response.body()
                    val token = user!!.token
                    if (token != null) { //비동기식 이기 때문에 나중에 실행됨
                        saveUserToken(token,this@MainActivity)
                    }
                    (application as MasterApplication).createRetrofit()
                    mainIntent = Intent(this@MainActivity, UserActivity::class.java)
                    mainIntent.putExtra("username",""+username)
                    startActivity(mainIntent)
                }else{
                    popAd.setPop("로그인 실패","아이디, 비밀번호 확인","확인")
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                popAd.setPop("연결 오류", "로그인 실패", "확인")
            }

        })
    }
    fun saveUserToken(token:String,activity: Activity){
        //토큰이 악용되면 안됨 -> main 에서만 sharedPreferences 활용
        val sp = activity.getSharedPreferences("login_sp",Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString("login_sp",token)
        editor.apply()
    }
    private fun initView(activity: Activity){ //초기화 함수
        insertButton = findViewById(R.id.insert_button)
        loginButton = findViewById(R.id.login_button)
        userNameView = findViewById(R.id.user_name_input)
        userPwView = findViewById(R.id.user_password_input)
    }
    private fun getUserName() : String{
        return userNameView.text.toString()
    }
    private fun getUserPass() : String{
        return userPwView.text.toString()
    }
}