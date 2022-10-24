package com.example.myapplication_instargram

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout.HORIZONTAL
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication_instargram.Message.MessageRoomFragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.io.File
import java.net.URL

import java.text.SimpleDateFormat
import java.util.*

private lateinit var getTakePicture: ActivityResultLauncher<Uri>
private lateinit var gridLayoutManager: GridLayoutManager
private var selectCount = 0 //선택 개수
private var pictureUri: Uri? = null //이미지 url
private var selectedImageUrl: String = "" // 선택 이미지 uri

class SelectPicActivity : AppCompatActivity() {
    private var uriArr = arrayListOf("1") //맨 처음은 카메라 아이콘
    private lateinit var goBackButton: ImageView
    private lateinit var okButton: TextView
    private lateinit var selectRecyclerView: RecyclerView //이미지 리스트
    private lateinit var readGalleryListener: PermissionListener //갤러리 권한
    lateinit var takePicListener: PermissionListener //사진권한
    override fun onDestroy() {
        super.onDestroy()
        uriArr.clear()
        selectCount=0
        pictureUri =null
        selectedImageUrl = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_pic)
        gridLayoutManager = GridLayoutManager(applicationContext, 3)
        val roomId = intent.getIntExtra("roomId",0)
        val username = intent.getStringExtra("username")
        //권한 설정 관련 - 카메라
        takePicListener = object : PermissionListener {
            override fun onPermissionGranted() {
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                //권한 허용 x
            }

        }

        //권한 설정 관련 - 갤러리
        readGalleryListener = object : PermissionListener {
            override fun onPermissionGranted() {
                uriArr.clear()
                uriArr = arrayListOf("1")
                getAllShownImagesPath()
                //연결 성공 시 어댑터 연결
                val adapter = SelectPicAdapter(
                    this@SelectPicActivity,
                    LayoutInflater.from(this@SelectPicActivity), uriArr
                )
                selectRecyclerView.layoutManager = gridLayoutManager
                selectRecyclerView.adapter = adapter

                //리싸이클러 뷰를 선언한 후에 터치 리스너를 구현하는 방법!
                //어뎁터가 아닌 곳에서 해당 아이템의 터치 리스너 구현 방법
//                selectRecyclerView.addOnItemTouchListener(object :
//                    RecyclerView.OnItemTouchListener {
//                    //여기서 터치 이벤트 구현
//                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                        val child = rv.findChildViewUnder(e.x,e.y)
//                        if (child!=null){
//                            val position = rv.getChildAdapterPosition(child)
//                            if (position==0){
//                                val view = rv.layoutManager?.findViewByPosition(position)
//                                val itemImageBack = view?.findViewById<ImageView>(R.id.item_pic_image_back)
//                                if (itemImageBack?.visibility==View.INVISIBLE){
//                                    if (selectCount<1){
//                                        itemImageBack.visibility = View.VISIBLE
//                                        TedPermission.with(applicationContext)
//                                            .setPermissionListener(takePicListener)
//                                            .setPermissions(
//                                                Manifest.permission.CAMERA
//                                            )
//                                            .check()
//                                        pictureUri  = createImageFile()
//                                        getTakePicture.launch(pictureUri)
//                                    }else{
//                                        itemImageBack.visibility =View.INVISIBLE
//                                        selectCount--
//                                        selectedImageUrl = ""
//                                    }
//                                }
//                            }
//                        }
//                        return false
//                    }
//
//                    //To change body of created functions use File | Settings | File Templates.
//                    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
//                    //To change body of created functions use File | Settings | File Templates.
//                    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
//                        TODO("Not yet implemented")
//                    }
//
//                })

            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                //권한 허용 x
            }
        }


        initView() //뷰 초기화

        //권한 설정 - 갤러리
        TedPermission.with(applicationContext)
            .setPermissionListener(readGalleryListener)
            .setPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).check()

        //권한 설정 -카메라
        TedPermission.with(applicationContext)
            .setPermissionListener(takePicListener)
            .setPermissions(
                Manifest.permission.CAMERA
            ).check()
        //사진 찍는 인텐트
        getTakePicture = registerForActivityResult(
            ActivityResultContracts
                .TakePicture()
        ) {
            if (it) { //사진 찍기 성공 - 카운트 ++
                pictureUri.let { url ->
                    selectedImageUrl = url.toString()
                }
            }
        }

        //뒤로 가기 클릭 시 완료
        goBackButton.setOnClickListener {
            finish() //on create 에서 실행 되어야 함
        }

        //완료 - 제출
        okButton.setOnClickListener {
            if (selectedImageUrl != "" && username!="") { //선택된 사진이 있을 때만 제출
                if(roomId==0) { //스토리 등록으로 연결
                    val toStoryIntent =
                        Intent(this@SelectPicActivity, UploadStoryActivity::class.java)
                    toStoryIntent.putExtra("selectedImageUrl", selectedImageUrl)
                    toStoryIntent.putExtra("username", username)
                    startActivity(toStoryIntent)
                }else{ //메시지 방에 전달
                    intent.putExtra("selectedImageUrl",selectedImageUrl)
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
            }
        }


    }

    private fun initView() {
        goBackButton = findViewById(R.id.select_pic_back_button)
        okButton = findViewById(R.id.select_pic_ok_button)
        selectRecyclerView = findViewById(R.id.select_pic_recycler)
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

//    @RequiresApi(Build.VERSION_CODES.R)
//    fun deleteImageFile(uri : Uri){
//        contentResolver.delete(uri,null)
//    }

    //갤러리의 모든 이미지 path를 uri로 반환
    private fun getAllShownImagesPath() {
        val uriExternal: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var columnIndexID: Int
        var imageId: Long
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                //사진 경로 url 가져오기
                columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    imageId = cursor.getLong(columnIndexID)
                    val uriImage = Uri.withAppendedPath(uriExternal, "" + imageId)
                    uriArr.add(uriImage.toString())

                }
            }
            cursor.close()
        }
    }

//    //갤러리에 저장 & 방금 찍은 사진 저장
//    private fun savePhoto(){
//        val file = File(currentPhotoPath)
//        val uri = Uri.fromFile(file)
//        MediaScannerConnection.scanFile(this, arrayOf(file.toString()),null,null)
//        selectedImageUrl = file.toUri().toString() //파일 저장
//    }


}

//class PickRingtone : ActivityResultContract<Int, Uri?>() {
//    override fun createIntent(context: Context, ringtoneType: Int) =
//        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
//            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType)
//        }
//
//    override fun parseResult(resultCode: Int, result: Intent?) : Uri? {
//        if (resultCode != Activity.RESULT_OK) {
//            return null
//        }
//        return result?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
//    }
//}

//각 이미지를 보여주는 어댑터터
private class SelectPicAdapter(
    private val selectPicActivity: SelectPicActivity,
    private val inflater: LayoutInflater,
    private val itemList: ArrayList<String>,
) : RecyclerView.Adapter<SelectPicAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView
        val itemBack: ImageView

        init {
            itemImage = itemView.findViewById(R.id.item_pic_image)
            itemBack = itemView.findViewById(R.id.item_pic_image_back)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_pic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //포지션이 0인경우는 단순 카메라 이미지
        if (position >= 1) {
            Glide.with(selectPicActivity)
                .load(Uri.parse(itemList[position]))
                .into(holder.itemImage)
        }
        //클릭시 선택 완료
        holder.itemImage.setOnClickListener {
            //갤러리 모음
            if (position >= 1) {
                //이미지 클릭 시 해당 색상 변경
                if (holder.itemBack.visibility == View.INVISIBLE) {
                    //처음 클릭 - 이미지를 클릭 상태로 변경 > 배경 등장
                    if (selectCount < 1) { //하나만 선택 가능
                        holder.itemBack.visibility = View.VISIBLE
                        selectCount++
                        selectedImageUrl = itemList[position]

                        ///itemList[position]에 url 들어있ㅇ음
                    }
                } else {
                    //이미 눌린 상태
                    holder.itemBack.visibility = View.INVISIBLE
                    selectCount--
                    selectedImageUrl = ""

                }
            } else {
                //position ==0 -> 카메라 연결
                if (holder.itemBack.visibility == View.INVISIBLE) {
                    //처음 클릭 -촬영 연결
                    if (selectCount < 1) { //하나만 선택 가능
                        val pic = selectPicActivity.createImageFile()

                        pictureUri = pic
                        getTakePicture.launch(pictureUri)

                        Glide.with(selectPicActivity)
                            .load(pictureUri)
                            .into(holder.itemImage)
                        holder.itemBack.visibility = View.VISIBLE
                        selectCount++
                        selectedImageUrl = pictureUri.toString()

                    }
                } else {
                    //클릭 취소 - 이미지 제거
                    holder.itemImage.setImageResource(R.drawable.ic_baseline_linked_camera_24)
                    holder.itemBack.visibility = View.INVISIBLE
                    selectCount--
                    selectedImageUrl = ""

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


}