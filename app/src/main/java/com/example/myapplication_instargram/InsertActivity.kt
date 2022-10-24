package com.example.myapplication_instargram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import com.example.myapplication_instargram.manager.PopAdManager
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InsertActivity : AppCompatActivity() {
    lateinit var userEmailView: EditText
    lateinit var userNameView: EditText
    lateinit var userPass1View: EditText
    lateinit var userPass2View: EditText
    lateinit var insertButton: Button
    lateinit var insertBackButton: Button
    lateinit var insertIntent: Intent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert)
        initView()
        setUpListener()

    }

    fun setUpListener() {
        //등록 버튼 구현
        insertButton.setOnClickListener {
            register()
        }
        //뒤로 가기 버튼 구현
        insertBackButton.setOnClickListener {
            insertIntent = Intent(this@InsertActivity, MainActivity::class.java)
            startActivity(insertIntent)
        }
    }

    private fun register() {
        val userEmail = getUserEmail()
        val userName = getUserName()
        val pw1 = getUserPass1()
        val pw2 = getUserPass2()
        val popAd  = PopAdManager(this@InsertActivity)
        if (userEmail=="" || userName=="" || pw1=="" || pw2==""){
            popAd.setPop("입력을 완료 해주세요","회원가입 실패","확인")
        }
        else if (pw1!=pw2){
            popAd.setPop("비밀번호가 서로 다름","회원가입 실패","확인")

        }else{
            (application as MasterApplication).service.register(
                userEmail,userName, pw1
            ).enqueue(object: Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful){
                        popAd.setPop(userName + "님", "회원가입 완료", "확인")
                        popAd.popButton.setOnClickListener {
                            insertIntent = Intent(this@InsertActivity, MainActivity::class.java)
                            startActivity(insertIntent)
                        }
                    }else{
                        val err = response.code()
                        if (err==404){
                            popAd.setPop("회원가입 실패","중복 이메일 혹은 아이디","확인")
                        }else{
                            popAd.setPop("연결 오류","회원가입 실패","확인")
                        }
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    popAd.setPop("연결 오류","회원가입 실패","확인")
                }

            })
        }
    }

    fun initView() { //초기화 함수
        userEmailView= findViewById(R.id.insert_user_email_input)
        userNameView = findViewById(R.id.insert_username_input)
        userPass1View = findViewById(R.id.insert_user_password_input)
        userPass2View = findViewById(R.id.insert_user_password_input2)
        insertButton = findViewById(R.id.insert_activity_button)
        insertBackButton = findViewById(R.id.insert_activity_back_button)
    }

    private fun getUserEmail(): String {
        return userEmailView.text.toString()
    }

    private fun getUserName(): String {
        return userNameView.text.toString()
    }

    private fun getUserPass1(): String {
        return userPass1View.text.toString()
    }

    private fun getUserPass2(): String {
        return userPass2View.text.toString()
    }
}