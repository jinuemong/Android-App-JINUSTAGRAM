package com.example.myapplication_instargram

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication_instargram.manager.PopAdManager
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

private lateinit var gridLayoutManager: GridLayoutManager
private lateinit var linearLayoutManager: LinearLayoutManager
private lateinit var readGalleryListener: PermissionListener //갤러리 권한
class UploadPosterActivity : AppCompatActivity() {
    private lateinit var  username :String
    private val uriArr = ArrayList<Uri>()
    private val selectedUrlList = ArrayList<Uri>()
    private var selectCount = 0 //선택 개수
    private lateinit var goBackButton: ImageView
    private lateinit var uploadButton: TextView
    private lateinit var selectedNum: TextView
    private lateinit var body : EditText
    private lateinit var selectedPicRecycler: RecyclerView
    private lateinit var galleryRecycler: RecyclerView
    private lateinit var picListAdapter : PicListAdapter
    private lateinit var selectedPicAdapter:  SelectedPicAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_poster)

        username = intent.getStringExtra("username").toString()
        initView()

        //권한 설정 관련 - 갤러리
        readGalleryListener = object : PermissionListener {
            override fun onPermissionGranted() {
                uriArr.clear()
                selectedUrlList.clear()
                getAllShownImagesPath()
                //연결 성공 시 어댑터 연결
                picListAdapter = PicListAdapter(
                    this@UploadPosterActivity,
                    LayoutInflater.from(this@UploadPosterActivity), uriArr
                )
                galleryRecycler.adapter = picListAdapter
                galleryRecycler.layoutManager = gridLayoutManager
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                //권한 허용 x
            }
        }

        //권한 설정 - 갤러리
        TedPermission.with(applicationContext)
            .setPermissionListener(readGalleryListener)
            .setPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).check()

        //선택된 사진 리스트
        selectedPicAdapter = SelectedPicAdapter(this@UploadPosterActivity,
        LayoutInflater.from(this@UploadPosterActivity),selectedUrlList)
        selectedPicRecycler.adapter = selectedPicAdapter
        selectedPicRecycler.layoutManager = linearLayoutManager
        selectedPicRecycler.addItemDecoration(AddAdapterDecoration())

        //onCreate에서 리싸이클러 뷰 터치 리스너 구현
        galleryRecycler.addOnItemTouchListener(object :RecyclerView.OnItemTouchListener{
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
               val child = rv.findChildViewUnder(e.x,e.y)
                if (child!=null){
                    val position = rv.getChildAdapterPosition(child)
                    val view = rv.layoutManager?.findViewByPosition(position)
                    val itemBack = view?.findViewById<ImageView>(R.id.item_pic_image_back)

                    //검색한 값이 없는 경우 -> 새로 추가
                    if (!selectedUrlList.contains(uriArr[position])){
                        //처음 클릭 - 이미지를 클릭 상태로 변경 > 배경 등장
                        if (selectCount < 10) { //10개만 선택 가능
                            itemBack?.visibility = View.VISIBLE
                            selectCount++
                            selectedNum.text = selectCount.toString()
                            selectedUrlList.add(uriArr[position])
                            selectedPicAdapter.notifyItemInserted(selectedPicAdapter.itemCount-1)
                            selectedPicRecycler.smoothScrollToPosition(selectedPicAdapter.itemCount - 1)
                        }
                    }else{
                        //이미 눌린 상태 - 리스트에서 제거
                        itemBack?.visibility = View.INVISIBLE
                        selectCount--
                        selectedNum.text = selectCount.toString()
                        val removePosition = selectedUrlList.indexOf(uriArr[position])
                        selectedUrlList.remove(uriArr[position])
                        selectedPicAdapter.notifyItemRemoved(removePosition)
                    }
                }
                return true
            }
            //사용 x
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        setUpListener()
        goBackButton.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uriArr.clear()
        selectCount=0
        selectedUrlList.clear()
    }

    private fun initView(){
        body = findViewById(R.id.update_comment)
        goBackButton = findViewById(R.id.upload_poster_back_button)
        uploadButton = findViewById(R.id.upload_poster_ok_button)
        //선택된 리스트
        selectedPicRecycler = findViewById(R.id.upload_poster_selected_pic_list_recycler)
        selectedNum = findViewById(R.id.upload_poster_select_num)
        //갤러리 리스트
        galleryRecycler = findViewById(R.id.upload_poster_select_pic_recycler)
        gridLayoutManager = GridLayoutManager(applicationContext, 3)
        linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL


    }
    private fun setUpListener(){
        goBackButton.setOnClickListener {
            finish()
        }

        uploadButton.setOnClickListener {
            if (selectCount!=0){
                ViewModel().uploadPoster(
                    selectedUrlList,
                    username,
                    body.text.toString(),
                    this@UploadPosterActivity,
                    paramFunc = {isUpload ->
                        if (isUpload){
                            val userIntent = Intent(
                                this@UploadPosterActivity,
                                UserActivity::class.java)
                            userIntent.putExtra("username", "" + username)
                            startActivity(userIntent)
                        }else{
                            val popAd = PopAdManager(this@UploadPosterActivity)
                            popAd.setPop("업로드 실패", "", "확인")
                        }
                    })
            }
        }

    }

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
                    uriArr.add(uriImage)
                }
            }
            cursor.close()
        }
    }
}

//각 이미지를 보여주는 어댑터터
private class PicListAdapter(
    private val activity: Activity,
    private val inflater: LayoutInflater,
    private val itemList: ArrayList<Uri>,
) : RecyclerView.Adapter<PicListAdapter.ViewHolder>() {
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
        Glide.with(activity)
            .load(itemList[position])
            .into(holder.itemImage)
    }
    override fun getItemCount(): Int {
        return itemList.size
    }


}

private class SelectedPicAdapter(
    private val activity: Activity,
    private val inflater: LayoutInflater,
    private val itemList : ArrayList<Uri>
)
: RecyclerView.Adapter<SelectedPicAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val itemImage :ImageView
        init {
            itemImage = itemView.findViewById(R.id.one_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_one_image_for_upload,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(activity)
            .load(itemList[position])
            .into(holder.itemImage)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}

private class AddAdapterDecoration : RecyclerView.ItemDecoration(){
    //아이템 간격 설정에 도움을 줌
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

//        val position = parent.getChildAdapterPosition(view) - 개별 관리 가능
//        val count = state.itemCount -갯수 카운터
        val offset = -350
        outRect.right = offset
    }
}