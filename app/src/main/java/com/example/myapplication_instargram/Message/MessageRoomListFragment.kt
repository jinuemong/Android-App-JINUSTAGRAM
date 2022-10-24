package com.example.myapplication_instargram.Message

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication_instargram.R
import com.example.myapplication_instargram.UserActivity
import com.example.myapplication_instargram.manager.MessageManager
import com.example.myapplication_instargram.server.MasterApplication
import com.example.myapplication_instargram.unit.MessageRoomProfile
import com.example.myapplication_instargram.unit.MessageUser
import com.example.myapplication_instargram.userServeFragment.StoryFragment
import de.hdodenhof.circleimageview.CircleImageView

private lateinit var messageManager: MessageManager
private lateinit var messageRoomList: RecyclerView
private lateinit var userActivity: UserActivity
private lateinit var fm:FragmentManager
class MessageRoomListFragment : Fragment() {
    lateinit var messageListView : View
    private lateinit var goBackButton : ImageView
    private lateinit var usernameMain : TextView
    private lateinit var username: String
    private lateinit var masterApp : MasterApplication
    //생명 주기 관리 (뒤로가기 버튼의 )
    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userActivity = (context as UserActivity)
        username = (activity as UserActivity).username
        //뒤로가기 버튼 재구현
        fm= (activity as UserActivity).supportFragmentManager //프래그먼트 매니저
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                userActivity.onFragmentGoBack(this@MessageRoomListFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        messageListView = inflater.inflate(R.layout.fragment_message_list, container, false)
        masterApp = ((activity as UserActivity).application as MasterApplication)
        messageManager = MessageManager(masterApp)
        return messageListView

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.startAnimation(userActivity.viewChangeAnimateLeft)
        initView(messageListView)
        setUpListener()
        initMessageListRecycler(username)
    }

    private fun initView(view : View){
        goBackButton =view.findViewById(R.id.message_list_back_button)
        usernameMain  =view.findViewById(R.id.message_list_user_name)
        messageRoomList =view.findViewById(R.id.message_list_recycler)

        usernameMain.text  =username
    }
    private fun setUpListener(){
        goBackButton.setOnClickListener {
            userActivity.onFragmentGoBack(this@MessageRoomListFragment)
        }
    }
}

// 메시지 리스트 표시 어댑터
class MessageRoomListViewAdapter(
    private val itemList : ArrayList<MessageRoomProfile>,
    private val inflater: LayoutInflater
):RecyclerView.Adapter<MessageRoomListViewAdapter.ViewHolder>(){
    inner class ViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        val itemUserImage : CircleImageView
        val itemUserName : TextView
        val itemLastMessage : TextView
        val itemLastMessageDay : TextView
        val itemDelButton : TextView
        val itemClickButton : LinearLayout
        val itemCount : TextView
        init {
            itemUserImage = itemView.findViewById(R.id.item_message_user_image)
            itemUserName = itemView.findViewById(R.id.item_message_user_name)
            itemLastMessage = itemView.findViewById(R.id.item_message_last)
            itemLastMessageDay = itemView.findViewById(R.id.item_message_time)
            itemDelButton = itemView.findViewById(R.id.item_message_delete)
            itemClickButton = itemView.findViewById(R.id.Lin_item_message)
            itemCount = itemView.findViewById(R.id.item_no_read_num)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =  inflater.inflate(R.layout.item_mini_message,parent,false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemUserName.text = itemList[position].username

        userActivity.setCircleImage(itemList[position].userImage,holder.itemUserImage)

        //스토리 있는 경우
        if(itemList[position].storyCount>0){
            holder.itemUserImage.apply {
                //배경색 지정
                borderColor  = Color.parseColor("#62BEF3")
                // 사이즈 지정
                borderWidth *= 2
            }
            //스토리로 이동
            holder.itemUserImage.setOnClickListener {
                try{
                    userActivity.onFragmentChange(
                        StoryFragment(
                            itemList[position].storyPost!!,
                            itemList[position].username,
                            itemList[position].userImage,
                        )
                    )
                }catch (e:Exception){ }
            }
        }

        holder.itemLastMessage.text = itemList[position].lastMessage
        if(itemList[position].lastMessageTime=="") {
            holder.itemLastMessageDay.text =""
        }else{
            val messageTimeDate = itemList[position].lastMessageTime.split("-","T")
            //22-09-12 형식으로 변환
            holder.itemLastMessageDay.text =
                (messageTimeDate[0].chunked(2)[1] + "." + messageTimeDate[1] + "." + messageTimeDate[2])
        }

        messageManager.getMessageList(itemList[position].messageRoomId, paramFunc = { messagePostList->
            var messagePosition=0
            if (messagePostList!=null){
                val messageUserList = messagePostList.messageUserPost
                val messageList = messagePostList.messagePost
                var lastReadMessage = 0
                if (messageUserList != null) {
                    for (messageUser in messageUserList){
                        if (messageUser.username== userActivity.username){
                            lastReadMessage = messageUser.lastReadMessageId
                        }
                    }
                }
                if (messageList!=null){
                    var count =0
                    for(message in messageList){
                        if (message.messageId==lastReadMessage){
                            messagePosition = count
                        }else{
                            count+=1
                        }
                    }
                    holder.itemCount.text = (messageList.size-messagePosition).toString()
                }

            }
            holder.itemClickButton.setOnClickListener {
                userActivity.targetUserName = itemList[position].username
                userActivity.onFragmentChange(MessageRoomFragment(itemList[position].messageRoomId,messagePostList,messagePosition))
            }
        })
    }

    override fun getItemCount(): Int{
        return itemList.size
    }

}
//메시지 리스트 초기화
private fun initMessageListRecycler(username :String){
    messageManager.getMessageRoomList(username , paramFunc = { list ->
        if (list!=null) {
            val dataList = ArrayList<MessageRoomProfile>()
            val adapter = MessageRoomListViewAdapter(dataList, LayoutInflater.from(userActivity))
            messageRoomList.adapter = adapter
            messageRoomList.layoutManager = LinearLayoutManager(userActivity)
            //메시지룸 삭제를 위한 리싸이클러 뷰 리스너
            messageRoomList.addOnItemTouchListener(object :
                RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child != null) {
                        val position = rv.getChildAdapterPosition(child)
                        val view = rv.layoutManager?.findViewByPosition(position)
                        val itemDelButton = view?.findViewById<ImageView>(R.id.item_message_delete)
                        itemDelButton?.setOnClickListener {
                            //메시지룸 삭제
                            messageManager.getMessageUserList(dataList[position].messageRoomId, paramFunc = { messageUserList->
                                if (messageUserList!=null){
                                    lateinit var myInfo : MessageUser
                                    lateinit var otherInfo : MessageUser
                                    for (messageUser in messageUserList){
                                        if (messageUser.username== userActivity.username){ myInfo=messageUser }
                                        else{ otherInfo = messageUser }
                                    }
                                    try {
                                        //비활성화
                                        messageManager.patchUnActive(myInfo.id, dataList[position].lastMessageId)
                                        if(!otherInfo.isActive){
                                            //둘다 false 이므로 메시지방 삭제
                                            messageManager.delMessageRoom(dataList[position].messageRoomId, paramFunc = {
                                                if (it!=-1){
                                                    dataList.removeAt(position)
                                                    adapter.notifyItemRemoved(position)
                                                    adapter.notifyItemRangeChanged(
                                                        position,
                                                        rv.adapter!!.itemCount - position
                                                    )
                                                }
                                            })
                                        }
                                    }catch (e:Exception){}
                                }
                            })
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
    })
}
