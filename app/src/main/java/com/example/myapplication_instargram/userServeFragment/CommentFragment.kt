package com.example.myapplication_instargram.userServeFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.followFragment.UserProfile
import com.example.myapplication_instargram.manager.PosterManager
import com.example.myapplication_instargram.manager.StoryViewerAdapter
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.Comment
import com.example.myapplication_instargram.unit.StoryViewer
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.internal.notify
import java.util.concurrent.TimeoutException

class CommentFragment(
    private val posterId:String,
    private val username:String,
    private val userImage:String,
    private val posterBody:String,
    private val uploadTime:String,
    commentList : ArrayList<Comment>?
) : Fragment() {
    private  var commentPoster=commentList
    private lateinit var userActivity : UserActivity
    private lateinit var masterApp : MasterApplication
    private lateinit var posterManager: PosterManager
    //생명 주기 관리 (뒤로가기 버튼의 )
    private lateinit var callback: OnBackPressedCallback
    private lateinit var fm: FragmentManager

    private lateinit var goBackButton: ImageView
    private lateinit var userImageView: CircleImageView
    private lateinit var userNameView:TextView
    private lateinit var userCommentView:TextView
    private lateinit var uploadTimeView:TextView
    private lateinit var commentRecycler:RecyclerView
    private lateinit var commentEditView:EditText
    private lateinit var sendButton: ImageView

    private lateinit var adapter: CommentAdapter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = context as UserActivity
        masterApp = userActivity.application as MasterApplication
        posterManager = PosterManager(masterApp)
        fm = userActivity.supportFragmentManager //프래그먼트 매니저
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userActivity.onFragmentGoBack(this@CommentFragment)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (commentPoster == null) {
            commentPoster = ArrayList()
        }else{
            for (c in commentPoster!!){
                Log.d("sdvljsdvlsjdvlsvdl",c.body.toString())
            }
        }
        initView(view)
        setUpListener()
    }

    private fun initView(view:View){
        goBackButton = view.findViewById(R.id.comment_back_button)
        userImageView = view.findViewById(R.id.comment_user_image)
        userNameView = view.findViewById(R.id.comment_username)
        userCommentView = view.findViewById(R.id.comment_username_comment)
        uploadTimeView = view.findViewById(R.id.comment_username_time)
        commentRecycler = view.findViewById(R.id.comment_recycler)
        commentEditView = view.findViewById(R.id.comment_ty)
        sendButton = view.findViewById(R.id.comment_send_message)

        userActivity.setCircleImage(userImage,userImageView)
        userNameView.text = username
        userCommentView.text = posterBody
        uploadTimeView.text = userActivity.getTranslatedDate(uploadTime)
        adapter = CommentAdapter(userActivity, commentPoster!!, LayoutInflater.from(userActivity))
        commentRecycler.adapter = adapter

    }
    private fun setUpListener(){
        goBackButton.setOnClickListener {
            userActivity.onFragmentGoBack(this@CommentFragment)
        }
        //댓글추가
        sendButton.setOnClickListener {
            if(commentEditView.text.isNotEmpty()){
                posterManager.addComment(posterId,
                    userActivity.username,
                    commentEditView.text.toString(), paramFunc = {
                        if(it!=null){
                            commentEditView.setText("")
                            commentPoster!!.add(it)
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                            commentRecycler.smoothScrollToPosition(adapter.itemCount - 1)
                        }
                    })
            }
        }
        //댓글 삭제를 위한 리싸이클러 뷰 리스너
        commentRecycler.addOnItemTouchListener(object :
            RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    val position = rv.getChildAdapterPosition(child)
                    val view = rv.layoutManager?.findViewByPosition(position)
                    val id =
                        view?.findViewById<TextView>(R.id.comment_one_id)?.text.toString()
                    val itemDelButton = view?.findViewById<ImageView>(R.id.comment_one_close_button)
                    itemDelButton?.setOnClickListener {
                        //댓글 삭제 수행 (삭제 버튼이 활성화 된 경우만)
                        if(itemDelButton.visibility==View.VISIBLE) {
                            posterManager.delUserComment(id.toInt(), paramFunc = {
                                if (it) {
                                    commentPoster!!.removeAt(position)
                                    adapter.notifyItemRemoved(position)
                                    adapter.notifyItemRangeChanged(
                                        position,
                                        rv.adapter!!.itemCount - position
                                    )
                                }
                            })
                        }
                    }
                }
                return false
            }
            //To change body of created functions use File | Settings | File Templates.
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            //To change body of created functions use File | Settings | File Templates.
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }
}

//스토리 뷰어 리싸이클러 -> storyViewerPost
class CommentAdapter(
    private val userActivity: UserActivity,
    private val itemList:ArrayList<Comment>,
    private val inflater: LayoutInflater
) :RecyclerView.Adapter<CommentAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val commentUserImage : CircleImageView
        val commentUsernameView : TextView
        val commentBodyView : TextView
        val commentTimeView : TextView
        val commentDelView : ImageView
        val commentId : TextView
        init {
            commentUserImage = itemView.findViewById(R.id.comment_one_user_image)
            commentUsernameView= itemView.findViewById(R.id.comment_one_username)
            commentBodyView= itemView.findViewById(R.id.comment_one_username_comment)
            commentTimeView= itemView.findViewById(R.id.comment_one_username_time)
            commentDelView= itemView.findViewById(R.id.comment_one_close_button)
            commentId = itemView.findViewById(R.id.comment_one_id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_comment,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //작성자일 경우 삭제 가능
        if (userActivity.username==itemList[position].writer){
            holder.commentDelView.visibility = View.VISIBLE
        }

        userActivity.setCircleImage(itemList[position].writerImage,holder.commentUserImage)
        holder.commentUsernameView.text =itemList[position].writer
        holder.commentBodyView.text =itemList[position].body
        holder.commentTimeView.text = userActivity.getTranslatedDate(itemList[position].uploadTime.toString())
        holder.commentId.text = itemList[position].commentId.toString()
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}