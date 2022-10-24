package com.example.myapplication_instargram
import android.annotation.SuppressLint
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.URL

import kotlin.math.atan2
import kotlin.math.sqrt


class TestActivity : AppCompatActivity() {
    private lateinit var backImage: ConstraintLayout
    private lateinit var result: ImageView
    lateinit var changeButton: Button
    lateinit var mainImage : ImageView

    private var matrix: Matrix = Matrix() //기존 매트릭스
    private var savedMatrix: Matrix = Matrix() // 작업 후 이미지 매핑 매트릭스

    private var startPoint : PointF = PointF()//한 손가락  터치 시 이동 포인트

    private var  midPoint: PointF = PointF()//두 손가락  터치 시 중심 포인트
    private var oldDistance  : Float = 0f //터치 후 두손 사이의 거리
    private var oldDegree:Double = 0.0  //두 손가락 사이의 각도
    private var touchMode = 0

    //url to bitmap
    private val IMAGE_URL = "https://raw.githubusercontent.com/DevTides/JetpackDogsApp/master/app/src/main/res/drawable/dog.png"
    private val coroutineScope = CoroutineScope(Dispatchers.Main)


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        backImage = findViewById(R.id.backImage)
        changeButton = findViewById(R.id.test_change)
        result = findViewById(R.id.test3)
        mainImage = findViewById(R.id.test2)

        //비트맵 이미지를 받아와서 원본 사이즈로 키우기
        mainImage.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    coroutineScope.launch {
                        val originalDeferred = coroutineScope.async(Dispatchers.IO) {
                            getOriginalBitmap()
                        }

                        val originalBitmap = originalDeferred.await()
                        val filteredDeferred =  coroutineScope.async(Dispatchers.Default) {
                            originalBitmap
                        }

                        val filterBitmap = filteredDeferred.await()
                        val resizeBitmap = Bitmap.createScaledBitmap(filterBitmap,mainImage.width,mainImage.height,true)
                        mainImage.setImageBitmap(resizeBitmap)
                    }
                    mainImage.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

        //너비와 높이 구하기 -> 클릭시 바로 저장
        backImage.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    //너비와 높이를 구해옴 
                    val w = backImage.width
                    val h = backImage.height

                    //버튼 클릭 시 입력
                    changeButton.setOnClickListener {
                        val bmOver =
                            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val backBitmap = backImage.drawToBitmap()
                        val canvas = Canvas(bmOver)
                        canvas.drawBitmap(backBitmap, 0f, 0f, null)
                        Glide.with(this@TestActivity)
                            .load(bmOver)
                            .into(result)
                        Log.d("ddasfasf",result.toString())
                    }
                    backImage.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

        mainImage.setOnTouchListener{ v,event->
            when(event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN ->{ //한손 클릭
                    touchMode=1
                    donwSingleEvent(event)
                }
                MotionEvent.ACTION_POINTER_DOWN->{ //두손 클릭
                    if(event.pointerCount==2){
                        touchMode=2
                        downMultiEvent(event)
                    }
                }
                MotionEvent.ACTION_MOVE ->{
                    if (touchMode == 1) { //한손 움직임
                        moveSingleEvent(event)
                    }
                    else if (touchMode == 2) { //두 손 움직임
                        moveMultiEvent(event)
                    }
                }
                MotionEvent.ACTION_POINTER_UP->{
                    touchMode=0
                }
            }
            true
        }
    }



    //거리를 구함
    private fun getDistance(e: MotionEvent) : Float{ //두 손 사이의 거리
        val x = e.getX(0) -e.getX(1)
        val y = e.getY(0) -e.getY(1)
        return  (sqrt((x*x+y*y).toDouble()) ).toFloat()
    }

    //두 손가락으로 눌렀을 때 중심점과 각도를 구함
    private fun downMultiEvent(e : MotionEvent){
        oldDistance = getDistance(e)
        if (oldDistance > 5f){
            savedMatrix.set(matrix)
            midPoint = getMidPoint(e)
            val radian =  atan2(
                (e.y - midPoint.y).toDouble(),
                (e.x - midPoint.x).toDouble())
            oldDegree = (radian*180)/Math.PI
        }
    }
    //한 손가락으로 눌렀을 때 이벤트
    private fun donwSingleEvent(e:MotionEvent){
        savedMatrix.set(matrix)
        startPoint = PointF(e.x,e.y)
    }

    //중간 지점을 구함
    private fun getMidPoint(e : MotionEvent):PointF{
        val x = (e.getX(0) + e.getX(1))/2
        val y = (e.getY(0) + e.getY(1))/2
        return PointF(x,y)
    }

    //두 손으로 이미지 움직일 때 이벤트
    private fun moveMultiEvent(e: MotionEvent){
        val newDistance = getDistance(e)
        if (newDistance>5f){
            matrix.set(savedMatrix)
            val scale = newDistance/oldDistance
            matrix.postScale(scale,scale,midPoint.x,midPoint.y)

            //이미지의 각도 설정
            val nowRadian  = atan2(
                (e.y- midPoint.y).toDouble()
                , (e.x - midPoint.x).toDouble())
            //두 손가락 사이의 각도 설정
            val nowDegree = (nowRadian*100)/Math.PI
            val degree = (nowDegree-oldDegree).toFloat()
            matrix.postRotate(degree,midPoint.x,midPoint.y)

            mainImage.imageMatrix = matrix
        }
    }

    //한 손으로 이미지 움직일 때 이벤트
    private fun moveSingleEvent(e: MotionEvent){
        matrix.set(savedMatrix)
        matrix.postTranslate(e.x-startPoint.x,e.y-startPoint.y)
        mainImage.imageMatrix = matrix
    }

    //url to bitmap  > IMAGE_URL= main image
    private fun getOriginalBitmap(): Bitmap =
        URL(IMAGE_URL).openStream().use {
            BitmapFactory.decodeStream(it)
        }
}
