package com.example.myapplication_instargram.server

import com.example.myapplication_instargram.unit.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

//url을 관리 할 수 있는 파일
interface RetrofitService {

    //로그인
    @POST("user/login/")
    //field 형식을 사용할 때 필요
    @FormUrlEncoded
    fun login(
        @Field("username") username : String,
        @Field("password") password : String
    ): Call<User>

    //등록
    @POST("user/register/")
    @FormUrlEncoded
    fun register(
        @Field("email") email : String,
        @Field("username") username : String,
        @Field("password") password : String,
    ): Call<User>

    // 내 프로필
    @GET("user/profile/")
    fun getUserProfile(
        @Query(value = "search",encoded = true)username: String
    ):Call<ArrayList<Profile>>

    // 미니 프로필
    @GET("user/miniprofile/")
    fun getUserProfileMini(
        @Query(value = "search",encoded = true)query: String
    ):Call<ArrayList<MiniProfiles>>

    //프로필 수정
    @Multipart
    @POST("user/update/profile/")
    fun updateProfile(
        @PartMap data: HashMap<String, RequestBody>, //username, customName,userComment를 묶어줌
        @Part userImage : MultipartBody.Part?,
    ):Call<MiniProfiles>


    //추천 유저(explore)
    @POST("user/randomuser/")
    @FormUrlEncoded
    fun getExploreList(
        @Field("username") username: String
    ): Call<ArrayList<MiniProfiles>>

    //팔로워 추가
    @POST("user/follower/")
    @FormUrlEncoded
    fun addFollower(
        @Field("username") username: String,
        @Field("fromUser") owner: String
    ): Call<Follow>

    //팔로잉 추가
    @POST("user/following/")
    @FormUrlEncoded
    fun addFollowing(
        @Field("username") username: String,
        @Field("toUser") owner: String
    ): Call<Follow>

    //팔로워 삭제
    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true, path = "user/follower/")
    fun delFollower(
        @Field("username") username: String,
        @Field("fromUser") owner: String
    ):Call<Follow>

    //팔로잉 삭제
    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true, path = "user/following/")
    fun delFollowing(
        @Field("username") username: String,
        @Field("toUser") owner: String
    ):Call<Follow>

    //팔로잉 확인
    @GET("user/following/")
    fun isFollowing(
        @Query(value = "username",encoded = true)username: String,
        @Query(value = "toUser",encoded = true)toUser: String,
        ):Call<ArrayList<Follow>>

    //팔로워 확인
    @GET("user/follower/")
    fun isFollower(
        @Query(value = "username",encoded = true)username: String,
        @Query(value = "fromUser",encoded = true)fromUser: String,
    ):Call<ArrayList<Follow>>

    //팔로워 목록 가져오기
    @POST("user/myfollower/")
    @FormUrlEncoded
    fun getFollowerList(
        @Field("username") username: String
    ):Call<ArrayList<MiniProfiles>>

    //팔로잉 목록 가져오기
    @POST("user/myfollowing/")
    @FormUrlEncoded
    fun getFollowingList(
        @Field("username") username: String
    ):Call<ArrayList<MiniProfiles>>

    //로그 남기기
    @POST("user/searchLog/")
    @FormUrlEncoded
    fun saveLog(
        @Field("username") username: String,
        @Field("log") log: String,
    ):Call<SearchLog>

    //로고 삭제
    @HTTP(method = "DELETE", hasBody = true, path = "user/searchLog/")
    @FormUrlEncoded
    fun delLog(
        @Field("id") id: String
    ):Call<SearchLog>

    // url : transformlog 사용자 로그 변환  - 프로필을 받거나 로그를 받음-
    @POST("user/transformLog/")
    @FormUrlEncoded
    fun transformLog(
        @Field("username") username: String
    ):Call<ArrayList<SearchLogGET>> //반환값이 log or miniProfile

    // url : searchingProfile 검색 중인 로그 변환 - 프로필을 받거나 []를 받음
    @POST("user/searchingProfile/")
    @FormUrlEncoded
    fun searchingLogProfile(
        @Field("username") search1: String,
        @Field("customName") search2: String
    ):Call<ArrayList<MiniProfiles>> //반환값이 Profile

    //스토리 등록
    @Multipart
    @POST("posting/story/")
    fun addStory(
        @Part("username") username: RequestBody,
        @Part storyImage: MultipartBody.Part?
    ):Call<Story>

    //스토리 삭제
    @DELETE("posting/story/{StoryId}/")
    fun delStory(
        @Path("StoryId")StoryId : Int
    ): Call<Story>

    //메시지룸 생성
    @POST("message/messageRoom/")
    fun createMessageRoom(): Call<MessageRoom>

    //메시지 유저 생성
    @POST("message/messageUser/")
    @FormUrlEncoded
    fun createMessageUser(
        @Field("messageRoomId") messageRoomId : Int,
        @Field("username") username : String,
        @Field("isActive") isActive : Boolean,
        @Field("lastMessageId") lastMessageId:Int, //마지막 활성화 메시지 아이디
        @Field("lastReadMessageId") lastReadMessageId : Int,   //마지막 읽은 메시지 아이디
        @Field("targetUser") targetUser : String
    ): Call<MessageUser>

    //메시지  룸 리스트  얻기
    @POST("message/MessageRoomProfile/")
    @FormUrlEncoded
    fun getMessageRoomList(
        @Field("username")username: String,
    ):Call<ArrayList<MessageRoomProfile>>

    //해당 메시지의 유저 얻기
    @POST("message/messageUser/")
    fun getMessageUser(
        @Query(value = "search",encoded = true)query: Int
    ):Call<ArrayList<MessageUser>>

    //메시지  리스트 얻기
    @GET("message/messageRoom/{id}/")
    fun getMessageList(
        @Path("id")messageRoomId : Int
    ):Call<MessageList>

    //메시지 룸 찾기 (커스텀)
    @POST("message/findMessageRoom/")
    @FormUrlEncoded
    fun findMessageRoom(
        @Field("username")username: String,
        @Field("targetUserName")targetUserName: String,
    ):Call<Int>

    //메시지 룸 삭제
    @DELETE("message/messageRoom/{id}/")
    @FormUrlEncoded
    fun delMessageRoom(
        @Path("id")messageRoomId : Int
    ): Call<MessageRoom>

    //메시지 보내기 (텍스트)
    @POST("message/message/")
    @FormUrlEncoded
    fun sendMessageText(
        @Field("messageRoomId") messageRoomId: Int,
        @Field("writer") writer: String,
        @Field("body") body: String
    ):Call<Message?>

    //메시지 보내기 (이미지)
    @Multipart
    @POST("message/message/")
    fun sendMessageImage(
        @Part("messageRoomId") messageRoomId: RequestBody,
        @Part("writer") writer: RequestBody,
        @Part messageImage: MultipartBody.Part?
    ):Call<Message?>

    //메시지 삭제
    @DELETE("message/message/{id}/")
    fun delMessage(
        @Path("id")messageId : Int
    ): Call<Message>

    //Last message 갱신
    @PATCH("message/messageUser/{id}/")
    fun updateLastMessage(
        @Path("id")id:Int,
        @Body lastMessageId:Int
    ):Call<MessageUser>

    //Last read message 갱신
    @PATCH("message/messageUser/{id}/")
    fun updateLastReadMessage(
        @Path("id")id:Int,
        @Body lastReadMessageId:Int
    ):Call<MessageUser>

    //메시지방 활성화 /비활성화
    @PATCH("message/messageUser/{id}/")
    fun activeMessageRoom(
        @Path("id")id:Int,
        @Body isActive:Boolean
    ):Call<MessageUser>

    //게시물 등록
    @Multipart
    @POST("posting/poster/")
    fun addPoster(
        @Part("username") username: RequestBody,
        @Part("body") body: RequestBody,
        @Part Oneimage: ArrayList<MultipartBody.Part>?
    ):Call<Poster?>

    // 게시물 리스트 얻기
    @GET("posting/poster/")
    fun getPosterList(
        @Query(value = "search",encoded = true)query: String
    ):Call<ArrayList<Poster>>

    // 게시물의 이미지 얻기
    @GET("posting/images/")
    fun getImageList(
        @Query(value = "search",encoded = true)query: String
    ):Call<ArrayList<OneImage>>

    // 게시물 리스트 얻기
    @POST("posting/randomPoster/")
    @FormUrlEncoded
    fun getRandomPosterList(
        @Field("type") type: String,
        @Field("username") username: String,
    ):Call<ArrayList<PosterRandom>>

    //게시물 삭제
    @DELETE("posting/poster/{posterId}/")
    fun delPoster(
        @Path("posterId")posterId : Int,
    ): Call<Poster>
    //스토리 뷰어 확인
    @GET("posting/storyViewer/")
    fun isViewerCheck(
        @Query(value = "viewer",encoded = true)viewer: String,
        @Query(value = "storyId",encoded = true)storyId: String,
    ):Call<ArrayList<StoryViewer>>

    //스토리 뷰어  추가
    @POST("posting/storyViewer/")
    @FormUrlEncoded
    fun addStoryViewer(
        @Field("storyId") storyId: Int,
        @Field("viewer") viewer: String
    ): Call<StoryViewer>

    //좋아요 확인
    @GET("posting/likes/")
    fun isLikerCheck(
        @Query(value = "liker",encoded = true)liker: String,
        @Query(value = "posterId",encoded = true)posterId: String,
    ):Call<ArrayList<Like>>

    //좋아요 추가
    @POST("posting/likes/")
    @FormUrlEncoded
    fun addLiker(
        @Field("posterId") posterId: String,
        @Field("liker") liker: String
    ): Call<Like>

    //좋아요 삭제
    @DELETE("posting/likes/{likeId}/")
    fun delLiker(
        @Path("likeId")likeId : Int,
    ): Call<Like>

//    //좋아요 삭제
//    @FormUrlEncoded
//    @HTTP(method = "DELETE", hasBody = true, path = "posting/likes/")
//    fun delLiker(
//        @Field("likeId") likeId: String,
//    ):Call<Like>

    //댓글 추가
    @POST("posting/comment/")
    @FormUrlEncoded
    fun addComment(
        @Field("posterId") posterId: String,
        @Field("writer") writer: String,
        @Field("body") body: String

    ): Call<Comment>

    //댓글 삭제
    @DELETE("posting/comment/{commentId}/")
    fun delComment(
        @Path("commentId")commentId : Int,
    ): Call<Comment>
}