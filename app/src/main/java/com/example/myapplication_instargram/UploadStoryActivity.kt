package com.example.myapplication_instargram

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Glide
import com.example.myapplication_instargram.manager.PopAdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.math.atan2
import kotlin.math.sqrt

class UploadStoryActivity : AppCompatActivity() {
//    private lateinit var  imageUrl : String //최종 제출 이미지
    //업로드 버튼 , 뒤로가기 버튼,  보여지는 이미지 ,
    private lateinit var  uploadButton : ImageView
    private lateinit var  backButton:ImageView
    private lateinit var  uploadImageBack:ConstraintLayout
    private lateinit var uploadImage:ImageView
    private var username :String =""
    private var selectedImageUrl =""

    private var matrix: Matrix = Matrix() //기존 매트릭스
    private var savedMatrix: Matrix = Matrix() // 작업 후 이미지 매핑 매트릭스

    private var startPoint : PointF = PointF()//한 손가락  터치 시 이동 포인트

    private var  midPoint: PointF = PointF()//두 손가락  터치 시 중심 포인트
    private var oldDistance  : Float = 0f //터치 후 두손 사이의 거리
    private var oldDegree:Double = 0.0  //두 손가락 사이의 각도
    private var touchMode = 0

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_story)
        username= intent.getStringExtra("username").toString()
        selectedImageUrl =  intent.getStringExtra("selectedImageUrl").toString()
        initView()
        setUpListener()
    }
    private fun initView(){
        uploadButton = findViewById(R.id.upload_story_button)
        backButton= findViewById(R.id.upload_story_back_button)
        uploadImageBack = findViewById(R.id.upload_story_back)
        uploadImage = findViewById(R.id.upload_story_image)
        Glide.with(this@UploadStoryActivity)
            .load(Uri.parse(selectedImageUrl))
            .into(uploadImage)
        //비트맵 이미지를 받아와서 원본 사이즈로 키우기
        uploadImage.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    coroutineScope.launch {
                        if (selectedImageUrl != "") {
                            val originalDeferred = coroutineScope.async(Dispatchers.IO) {
                                uploadImage.drawToBitmap()
                            }

                            val originalBitmap = originalDeferred.await()
                            val filteredDeferred = coroutineScope.async(Dispatchers.Default) {
                                originalBitmap
                            }

                            val filterBitmap = filteredDeferred.await()
                            val resizeBitmap = Bitmap.createScaledBitmap(
                                filterBitmap,
                                uploadImage.width,
                                uploadImage.height,
                                true
                            )
                            uploadImage.setImageBitmap(resizeBitmap)
                        }
                    }
                    uploadImage.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setUpListener(){
        //뒤로가기
        backButton.setOnClickListener {
            finish()
        }

        //터치 리스너
        uploadImage.setOnTouchListener{ _,event->
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
        if(username!="") {
            uploadImageBack.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        //너비와 높이를 구해옴
                        val w = uploadImageBack.width
                        val h = uploadImageBack.height

                        //버튼 클릭 시 등록 캔버스에 배경 + 사이즈 조절된 이미지 합성
                        uploadButton.setOnClickListener {
                            val bmOver =
                                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                            val backBitmap = uploadImageBack.drawToBitmap()
                            val canvas = Canvas(bmOver)
                            canvas.drawBitmap(backBitmap, 0f, 0f, null)
                            val uri = getImageUri(this@UploadStoryActivity,bmOver)
                            //여기서 bmOver를 전송해주어야 함
                            //UploadStoryViewModel 에서 스토리를 업로드 해주는 코드 실행
                            ViewModel().uploadStory(
                                uri, //url로 건내줘야 함 !!!!!!!!!!!!!!!!!!!!!!!!!!
                                username,
                                this@UploadStoryActivity,
                                paramFunc = { isUpload ->
                                    if (isUpload) {
                                        val userIntent = Intent(
                                            this@UploadStoryActivity,
                                            UserActivity::class.java
                                        )
                                        userIntent.putExtra("username", "" + username)
                                        startActivity(userIntent)
                                    } else {
                                        val popAd = PopAdManager(this@UploadStoryActivity)
                                        popAd.setPop("업로드 실패", "", "확인")
                                    }
                                })
                        }
                        uploadImageBack.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
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
    private fun getMidPoint(e : MotionEvent): PointF {
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

            uploadImage.imageMatrix = matrix
        }
    }

    //한 손으로 이미지 움직일 때 이벤트
    private fun moveSingleEvent(e: MotionEvent){
        matrix.set(savedMatrix)
        matrix.postTranslate(e.x-startPoint.x,e.y-startPoint.y)
        uploadImage.imageMatrix = matrix
    }

    //url to bitmap  > IMAGE_URL= main image
//    private fun getOriginalBitmap(): Bitmap =
//        URL(selectedImageUrl).openStream().use {
//            BitmapFactory.decodeStream(it)
//
//        }

    //bitmap to url
    private fun getImageUri(context: Context, bitmap: Bitmap) : Uri{
        val bytes  = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver,bitmap,username+"Story",null)
        return Uri.parse(path)
    }
}

