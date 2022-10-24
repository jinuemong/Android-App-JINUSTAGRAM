package com.example.myapplication_instargram

import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.Message
import com.example.myapplication_instargram.unit.Poster
import com.example.myapplication_instargram.unit.MiniProfiles
import com.example.myapplication_instargram.unit.Story
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

//multipart 형식으로 보낼 때 필요한 함수
class ViewModel : ViewModel(){
    //게시물 전송 - 파일 형식 (연속)
    fun uploadPoster(filePath: ArrayList<Uri>,username: String,body: String,
                     activity: Activity, paramFunc: (Boolean) -> Unit) {
        viewModelScope.launch {
            try{
                val userNameRequestBody: RequestBody = username.toPlainRequestBody()
                val bodyRequestBody: RequestBody = body.toPlainRequestBody()
                val imageMultipartList = ArrayList<MultipartBody.Part>()
                for (file in filePath){
                    val path = getImageFilePath(activity,file)
                    val imageFile = File(path)
                    val imageRequestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imageMultipartBody: MultipartBody.Part =
                        MultipartBody.Part.createFormData(
                            "Oneimage",
                            imageFile.name,
                            imageRequestBody
                        )
                    imageMultipartList.add(imageMultipartBody)
                }

                (activity.application as MasterApplication).service
                    .addPoster(userNameRequestBody,bodyRequestBody,imageMultipartList)
                    .enqueue(object :Callback<Poster?>{
                        override fun onResponse(call: Call<Poster?>, response: Response<Poster?>) {
                            if (response.isSuccessful){
                                paramFunc(true)
                            }else{
                                paramFunc(false)
                            }
                        }

                        override fun onFailure(call: Call<Poster?>, t: Throwable) {
                            paramFunc(false)
                        }

                    })
            }catch (e:Exception){
                paramFunc(false)
            }
        }
    }

    //스토리 전송 - 파일 형식(파일 형식으로 보내 주어야 받을수 있다)
    fun uploadStory(filePath: Uri, username:String, activity: Activity,
                    paramFunc: (Boolean) -> Unit){
        viewModelScope.launch {
            try {
                val path = getImageFilePath(activity, filePath)
                val file = File(path)
                val imageRequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipartBody: MultipartBody.Part =
                    MultipartBody.Part.createFormData(
                        "storyImage",
                        file.name,
                        imageRequestBody
                    )
                val userNameRequestBody: RequestBody = username.toPlainRequestBody()

                (activity.application as MasterApplication).service
                    .addStory(userNameRequestBody, imageMultipartBody)
                    .enqueue(object : Callback<Story> {
                        override fun onResponse(
                            call: Call<Story>, response: Response<Story>
                        ) {
                            if (response.isSuccessful) {
                                paramFunc(true)
                            } else {
                                paramFunc(false)
                            }
                        }

                        override fun onFailure(call: Call<Story>, t: Throwable) {
                            paramFunc(false)
                        }
                    })
            }catch (e:Exception){
                paramFunc(false)
            }

        }
    }

    //스토리 전송 -비트맵 형식
//    fun uploadStory(bitmap: Bitmap?, username:String, activity: Activity,
//                    paramFunc : (Boolean)->Unit) {
//        viewModelScope.launch{
//            val userNameRequestBody: RequestBody = username.toPlainRequestBody()
//            val bitmapRequestBody =
//                bitmap?.let { BitmapRequestBody(it) }
//            val bitmapMultipartBody: MultipartBody.Part? =
//                if (bitmapRequestBody == null) null
//                else MultipartBody.Part.createFormData("storyImage", "storyImage$username", bitmapRequestBody)
//            try {
//
//                (activity.application as MasterApplication).service
//                    .addStory(userNameRequestBody,bitmapMultipartBody)
//                    .enqueue(object :Callback<Story>{
//                        override fun onResponse(
//                            call: Call<Story>, response: Response<Story>
//                        ) {
//                            if (response.isSuccessful){
//                                paramFunc(true)
//                            }else{
//                                paramFunc(false)
//                            }
//                        }
//                        override fun onFailure(call: Call<Story>, t: Throwable) {
//                            paramFunc(false)
//                        }
//                    })
//            }catch (e:Exception){
//                paramFunc(false)
//            }
//        }
//        //viewModelScope.launch : 다른 화면 전환 시 비동기 처리를 도와줌
//        //ex > 다운로드 중 데이터 다른 화면 실행 시 계속 다운
//
//    }

    //업데이트 프로필 전송
    fun updateProfile(id:Int,filePath: Uri, username: String,customName:String, userComment:String,
                      activity: Activity,paramFunc : (Boolean)->Unit){
        viewModelScope.launch{
            //request값은 int가 올수 없다 - 스트링 변환 후 requestbody로 변환
            val idRequestBody : RequestBody = id.toString().toPlainRequestBody()
            val usernameRequestBody : RequestBody = username.toPlainRequestBody()
            val customNameRequestBody: RequestBody = customName.toPlainRequestBody()
            val userCommentRequestBody: RequestBody = userComment.toPlainRequestBody()
            val textHashMap = hashMapOf<String, RequestBody>() //키 - 벨류 값으로 묶어준다
            textHashMap["id"] = idRequestBody
            textHashMap["username"] = usernameRequestBody
            textHashMap["customName"] = customNameRequestBody
            textHashMap["userComment"] = userCommentRequestBody

            //이미지 처리
            val path = getImageFilePath(activity, filePath)
            val file = File(path)
            val imageRequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipartBody: MultipartBody.Part =
                MultipartBody.Part.createFormData(
                    "userImage",
                    file.name,
                    imageRequestBody
                )
            try {
                (activity.application as MasterApplication).service
                    .updateProfile(textHashMap,imageMultipartBody)
                    .enqueue(object :Callback<MiniProfiles>{
                        override fun onResponse(
                            call: Call<MiniProfiles>,
                            response: Response<MiniProfiles>
                        ) {
                            if (response.isSuccessful){
                                paramFunc(true)
                            }else{
                                paramFunc(false)
                            }
                        }
                        override fun onFailure(call: Call<MiniProfiles>, t: Throwable) {
                            paramFunc(false)
                        }

                    })
            }catch (e:Exception){
                paramFunc(false)
            }
        }
    }

    //이미지 메시지 보내기
    fun sendImageMessage(messageRoomId:Int, writer:String,filePath: Uri,activity: Activity,
    paramFunc: (Message?) -> Unit){
        viewModelScope.launch {
            try{
                val path = getImageFilePath(activity,filePath)
                val file  = File(path)
                val imageRequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipartBody = MultipartBody.Part
                    .createFormData(
                        "messageImage",
                        file.name,
                        imageRequestBody
                    )
                val writerRequestBody = writer.toPlainRequestBody()
                val messageRoomIdRequestBody = messageRoomId.toString().toPlainRequestBody()
                (activity.application as MasterApplication).service
                    .sendMessageImage(messageRoomIdRequestBody,writerRequestBody,
                    imageMultipartBody).enqueue(object :Callback<Message?>{
                        override fun onResponse(
                            call: Call<Message?>,
                            response: Response<Message?>
                        ) {
                            if (response.isSuccessful){
                                paramFunc(response.body())
                            }else{
                                paramFunc(null)
                            }
                        }
                        override fun onFailure(call: Call<Message?>, t: Throwable) {
                            paramFunc(null)
                        }

                    })

            }catch (e:Exception){
                paramFunc(null)
            }
        }
    }
    //비트맵 형식으로 보내기
//    inner class BitmapRequestBody(private  val bitmap : Bitmap) : RequestBody(){
//        override fun contentType(): MediaType = "image/jpeg".toMediaType()
//        override fun writeTo(sink: BufferedSink) {
//            bitmap.compress(Bitmap.CompressFormat.JPEG,99,sink.outputStream())
//        }
//    }

    private fun getImageFilePath(activity: Activity, contentUri: Uri): String {
        var columnIndex = 0
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity.contentResolver.query(contentUri, projection, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        return cursor.getString(columnIndex)
    }


    //string 을 plain Text Request로 바꿔주는 확장 함수  : l2hyunwoo(저작권)
    private fun String?.toPlainRequestBody () =
        requireNotNull(this).toRequestBody("text/plain".toMediaTypeOrNull())



}