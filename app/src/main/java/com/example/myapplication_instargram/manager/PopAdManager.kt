package com.example.myapplication_instargram.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.myapplication_instargram.R

//팝업창 기본 옵션

class PopAdManager(
    activity: Activity,
) {
    private val popActivity = activity
    val layoutInflater: LayoutInflater = LayoutInflater.from(popActivity)
    @SuppressLint("InflateParams")
    val view: View = layoutInflater.inflate(R.layout.pop_custom, null)
    var popText1: TextView = view.findViewById<TextView>(R.id.pop_text1)
    var popText2: TextView = view.findViewById<TextView>(R.id.pop_text2)
    var popButton: Button = view.findViewById<Button>(R.id.pop_button)
//    popText1.text = "중복된 아이디"
//    popText2.text = "회원가입 실패"
//    popButton.text = "확인"

    fun setPop(    Text1 : String,
                   Text2 : String,
                   ButtonText : String){
        val viewChangeAnimateUp =  AnimationUtils.loadAnimation(popActivity,R.anim.enter_up)
        view.startAnimation(viewChangeAnimateUp)
        popText1.text = Text1
        popText2.text = Text2
        popButton.text = ButtonText
        val ad = AlertDialog.Builder(popActivity)
            .setView(view)
            .show()
        popButton.setOnClickListener {
            ad.cancel()
        }

    }
}
