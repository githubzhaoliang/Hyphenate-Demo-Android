package com.hyphenate.easeui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.util.DateUtils;
import internal.org.apache.http.entity.mime.MIME;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by wei on 2016/10/9.
 */
public class EaseConversationListAdapter extends EaseSortedListAdapter<EMConversation> {

    private Context mContext;
    private EaseListItemClickListener mOnItemClickListener;

    public EaseConversationListAdapter(Context context, Comparator<EMConversation> comparator) {
        super(context, EMConversation.class, comparator);
        mContext = context;
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //set the view holder
        return new ConversationListHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.ease_row_conversation_list, parent, false));
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        ConversationListHolder holder = (ConversationListHolder) viewHolder;
        EMConversation conversation = getItem(position);
        // get username or group id
        String username = conversation.getUserName();
        if (conversation.getType() == EMConversation.EMConversationType.GroupChat) {
            // group message, show group avatar
            holder.mAvatarView.setImageResource(R.mipmap.ease_ic_group_default);
            EMGroup group = EMClient.getInstance().groupManager().getGroup(username);
            holder.mNameView.setText(group != null ? group.getGroupName() : username);
        } else if (conversation.getType() == EMConversation.EMConversationType.ChatRoom) {
            holder.mAvatarView.setImageResource(R.mipmap.ease_ic_group_default);
            EMChatRoom room = EMClient.getInstance().chatroomManager().getChatRoom(username);
            holder.mNameView.setText(
                    room != null && !TextUtils.isEmpty(room.getName()) ? room.getName() : username);
        } else {
            //single chat conversation
            EaseUserUtils.setUserAvatar(mContext, username, holder.mAvatarView);
            EaseUserUtils.setUserNick(username, holder.mNameView);
        }

        if (conversation.getUnreadMsgCount() > 0) {
            // show unread message count
            holder.mUnreadNumView.setText(String.valueOf(conversation.getUnreadMsgCount()));
            holder.mUnreadNumView.setVisibility(View.VISIBLE);
        } else {
            holder.mUnreadNumView.setVisibility(View.INVISIBLE);
        }

        if (conversation.getAllMsgCount() != 0) {
            // show the content of latest message
            EMMessage lastMessage = conversation.getLastMessage();
            String content = null;
            //if(cvsListHelper != null){
            //    content = cvsListHelper.onSetItemSecondaryText(lastMessage);
            //}
            //holder.message.setText(EaseSmileUtils.getSmiledText(getContext(), EaseCommonUtils.getMessageDigest(lastMessage, (this.getContext()))),
            //        TextView.BufferType.SPANNABLE);
            holder.mMessageView.setText(EaseCommonUtils.getMessageDigest(lastMessage, mContext));
            if (content != null) {
                holder.mMessageView.setText(content);
            }
            //show the message time
            holder.mTimeView.setText(
                    DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
            if (lastMessage.direct() == EMMessage.Direct.SEND
                    && lastMessage.status() == EMMessage.Status.FAIL) {
                holder.mMsgStateView.setVisibility(View.VISIBLE);
            } else {
                holder.mMsgStateView.setVisibility(View.GONE);
            }
        }

        //set item onclick listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    @Override protected boolean areItemsTheSame(EMConversation item1, EMConversation item2) {
        return item1.getUserName().equals(item2.getUserName());
    }

    @Override
    protected boolean areItemContentsTheSame(EMConversation oldItem, EMConversation newItem) {
        return oldItem.equals(newItem);
    }

    private void filter(String query) {
        //final String lowerCaseQuery = query.toLowerCase();
        //
        //final List<EMConversation> filteredModelList = new ArrayList<>();
        //for (ExampleModel model : models) {
        //    final String text = model.getText().toLowerCase();
        //    if (text.contains(lowerCaseQuery)) {
        //        filteredModelList.add(model);
        //    }
        //}
    }

    public void setOnItemClickListener(EaseListItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * view holder class
     */
    static class ConversationListHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_avatar) ImageView mAvatarView;
        @BindView(R.id.txt_name) TextView mNameView;
        @BindView(R.id.img_msg_state) ImageView mMsgStateView;
        @BindView(R.id.txt_message) TextView mMessageView;
        @BindView(R.id.txt_time) TextView mTimeView;
        @BindView(R.id.txt_unread_msg_number) TextView mUnreadNumView;

        public ConversationListHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}