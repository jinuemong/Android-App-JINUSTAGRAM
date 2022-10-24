package com.example.myapplication_instargram

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Glide
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.manager.PopAdManager
import com.example.myapplication_instargram.unit.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var goBack: TextView
    private lateinit var okButton: TextView
    private lateinit var userImage: ImageView
    private lateinit var updateImageView: TextView
    private lateinit var updateUsername: EditText
    private lateinit var updateCustomName: EditText
    private lateinit var updateComment: EditText
    private lateinit var userId: EditText
    private lateinit var userName: String

    //권한 리스트
    private val permissionList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    //파일 불러오기
    private lateinit var getContentImage : ActivityResultLauncher<String>

    //권한 허용
    private lateinit var requestMultiplePermission : ActivityResultLauncher<Array<String>>

    //카메라 사진 얻기 + 저장
    private var pictureUri: Uri? = null
    private lateinit var getTakePicture :ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        userName = intent.getStringExtra("username").toString()
        initView() //뷰 초기화
        setUpListener() //리스너 초기화

        //파일 불러오기 초기화
        getContentImage = registerForActivityResult(
            ActivityResultContracts
                .GetContent()
        ) { uri ->
            uri.let {
                pictureUri = it
                Glide.with(this@UpdateProfileActivity)
                    .load(uri)
                    .into(userImage)
            }
        }

        // 권한을 허용하도록 요청
        requestMultiplePermission = registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()
        ) { results ->
            results.forEach {
                if (!it.value) {
                    finish()
                } //권한이 없다면 종료
            }
        }

        //카메라 사진을 저장 2가지 방법
        //1. 결과로 uri 얻기
        getTakePicture = registerForActivityResult(
            ActivityResultContracts
                .TakePicture()
        ) {
            if (it) {
                pictureUri.let { uri->
                    pictureUri = uri
                    Glide.with(this@UpdateProfileActivity)
                        .load(uri)
                        .into(userImage)
                }

            }
        }
        //2. 결과로 bitmap 얻기
        // 카메라를 실행하며 결과로 비트맵 이미지를 얻음
//    private val getTakePicturePreview = registerForActivityResult(ActivityResultContracts
//    .TakePicturePreview()) { bitmap ->
//        bitmap.let { binding.mainImg.setImageBitmap(bitmap) }
//    }


    }

    private fun initView() {
        userImage = findViewById(R.id.update_user_image)
        updateUsername = findViewById(R.id.update_username)
        updateCustomName = findViewById(R.id.update_custom_name)
        updateComment = findViewById(R.id.update_comment)
        userId = findViewById(R.id.update_user_id)

        //기초값 설정
        (application as MasterApplication).service.getUserProfile(userName)
            .enqueue(object : Callback<ArrayList<Profile>> {
                override fun onResponse(
                    call: Call<ArrayList<Profile>>,
                    response: Response<ArrayList<Profile>>
                ) {
                    if (response.isSuccessful) {
                        try {
                            val profile = response.body()!![0]
                            userId.setText(profile.id.toString())
                            Glide.with(this@UpdateProfileActivity)
                                .load(Uri.parse(profile.userImage))
                                .into(userImage)
                            updateUsername.setText(profile.username.toString())
                            updateCustomName.setText(profile.customName.toString())
                            updateComment.setText(profile.userComment.toString())
                        } catch (e: Exception) {
                        }
                    }
                }

                override fun onFailure(call: Call<ArrayList<Profile>>, t: Throwable) {}

            })
    }

    private fun setUpListener() {
        val userIntent = Intent(this@UpdateProfileActivity, UserActivity::class.java)

        goBack = findViewById(R.id.update_close)
        okButton = findViewById(R.id.update_ok)
        updateImageView = findViewById(R.id.update_image_button)
        //뒤로
        goBack.setOnClickListener {
            userIntent.putExtra("username", "" + userName)
            userIntent.putExtra("type", "" + 4)
            startActivity(userIntent)
        }
        //완료
        okButton.setOnClickListener {
            val id = userId.text.toString().toInt()

            //업데이트 뷰 모델 호출  - UploadViewModel.updateProfile
             ViewModel().updateProfile(id, getImageUri(this@UpdateProfileActivity, userImage.drawToBitmap()),
                updateUsername.text.toString(), updateCustomName.text.toString(),updateComment.text.toString(),
            this@UpdateProfileActivity, paramFunc = { isUpdate->
                    if (isUpdate) {
                        userIntent.putExtra("username", "" + updateUsername.text.toString())
                        userIntent.putExtra("type", "" + 4)
                        startActivity(userIntent)
                    }else{
                        val popAd = PopAdManager(this@UpdateProfileActivity)
                        popAd.setPop("수정 실패","중복 값 or 비정상적인 값","확인")
                    }
                })
        }
            updateImageView.setOnClickListener { view ->
                requestMultiplePermission.launch(permissionList)
                val popupMenu = PopupMenu(applicationContext, view)
                menuInflater.inflate(R.menu.pop_up_camera, popupMenu.menu) //버튼과 메뉴 연결
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener { menu ->

                    when (menu.itemId) {
                        R.id.select_pic -> { //사진 촬영
                            pictureUri = createImageFile()
                            getTakePicture.launch(pictureUri)
                            return@setOnMenuItemClickListener true
                        }
                        else -> { // 사진 고르기- 갤러리
                            getContentImage.launch("image/*")
                            // 파일 형식 제한 -> 이미지 파일만 탐색
                            return@setOnMenuItemClickListener true
                        }
                    }
                }
            }
        }

    //카메라 uri를 받아오는 과정
    @SuppressLint("SimpleDateFormat")
    fun createImageFile(): Uri? {
        val now = SimpleDateFormat("yyMMdd_HHmmss").format(Date())
        val content = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img_$now.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
    }
    //bitmap to url
    private fun getImageUri(context: Context, bitmap: Bitmap) : Uri{
        val bytes  = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver,bitmap,"userProfile",null)
        return Uri.parse(path)
    }

}
