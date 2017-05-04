package com.zuzex.look2meet.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.media.FullScreenViewActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";
    private final List<ChatMessage> chatMessages;
    private  LayoutInflater inflater;
    private Context context;
    private ArrayList<Emoticon> emoticons;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages, ArrayList<Emoticon> emoticons) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.emoticons = emoticons;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public ChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
       return chatMessages.get(position).idUser;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
//	    Log.wtf("list getView", "check");
        View vi = view;
        ViewHolder holder = new ViewHolder();
        ChatMessage chatMessage = getItem(position);

        if (view == null) {
            vi = inflater.inflate(R.layout.activity_chat_message, null);
            holder = createViewHolder(vi);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        setAlignment(holder, chatMessage.isIncoming);

        putMessage(chatMessage.text, holder.txtMessage);

//        holder.txtMessage.setText(Html.fromHtml(chatMessage.text));
//        if (chatMessage.userName != null) {
//            holder.txtInfo.setText(chatMessage.userName + ": " + getTimeText(chatMessage.date));
//        } else {
            holder.txtInfo.setText(getTimeText(chatMessage.date));
//        }

	    holder.imagesGrid.removeAllViews();
	    if (chatMessage.attachmentsArr != null) {
		    for (int i = 0; i < chatMessage.attachmentsArr.size(); i++) {
			    ChatMessageAttachment att = chatMessage.attachmentsArr.get(i);

			    ImageView imgView = new ImageView(context);
			    imgView.setMaxHeight(300);
			    imgView.setMaxWidth(300);
//			    imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//			    imgView.setImageResource(R.drawable.no_image);
			    ImageLoader.getInstance().displayImage(att.iconPath, imgView);
			    holder.imagesGrid.addView(imgView);
			    imgView.setId(att.type);
			    imgView.setTag(att.guid);
			    imgView.setOnClickListener(new View.OnClickListener() {
				    @Override
				    public void onClick(View view) {
//					    Log.wtf("imgView.OnClickListener", "zzzz" + view.getId() +"|"+ view.getTag() +"|"+ view.getResources());
					    showExpandedAttach((String) view.getTag(),view.getId());
				    }
			    });
		    }
	    }
	    vi.invalidate();
        return vi;
    }

    private void setAlignment(ViewHolder holder, boolean isIncoming) {
        if (isIncoming) {
            holder.contentWithBG.setBackgroundResource(R.drawable.chat_incoming_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.chat_outgoing_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
	    holder.imagesGrid = (GridLayout) v.findViewById(R.id.imagesGrid);
        return holder;
    }

    private String getTimeText(Date date) {
        return DateFormat.format(DATE_FORMAT, date).toString();
    }

    public class ViewHolder {
        public ViewHolder() {};
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
	    public GridLayout imagesGrid;
    }

	private void showExpandedAttach(String guid, Integer attType) {
		if (attType == ChatMessageAttachment.ChatMessageAttachmentType_image) {
			ArrayList<String> arr = getAttachList(attType);
			int index = getIndexOfElementInArray(guid, arr);

			Intent fullScreenActivity = new Intent(context, FullScreenViewActivity.class);
			fullScreenActivity.putExtra("filePathArray", arr);
			fullScreenActivity.putExtra("position", index);
			context.startActivity(fullScreenActivity);
//			Log.wtf("showExpandedAttach", index + "|" + arr.toString());
		}
		if (attType == ChatMessageAttachment.ChatMessageAttachmentType_video) {
			ArrayList<String> arr = getAttachList(attType);
			int index = getIndexOfElementInArray(guid, arr);

			String videoUrl = arr.get(index);
			if(videoUrl != null && videoUrl.length() > 0) {
				Uri videoUri = Uri.parse(videoUrl);
				Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
				context.startActivity(intent);
			}
//			Log.wtf("showExpandedAttach", index + "|" + arr.toString());
		}
	}

	private ArrayList<String> getAttachList(Integer attType) {
		ArrayList<String> arr = new ArrayList<String>();
		if (chatMessages != null) {
			for (int i = 0; i < chatMessages.size(); i++) {
				ChatMessage chatMessage = chatMessages.get(i);
				if (chatMessage.attachmentsArr != null) {
					for (int j = 0; j < chatMessage.attachmentsArr.size(); j++) {
						ChatMessageAttachment att = chatMessage.attachmentsArr.get(j);
						if (att.type == attType) {
							arr.add(att.filePath);
						}
					}
				}
			}
		}
		return arr;
	}

	private int getIndexOfElementInArray(String element, ArrayList<String> array) {
		if (array != null && element != null && element.length() > 0) {
			for (int i = 0; i < array.size(); i++) {
				String str = array.get(i);
				if (str.contains(element)) {
					return i;
				}
			}
		}
		return 0;
	}

    private void putMessage(String msg, TextView tv) {
        for(Emoticon emoticon: emoticons) {
            msg = msg.replace(emoticon.text, "<img src=\"" + emoticon.url + "\">");
        }

        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            public Drawable getDrawable(String source) {
                Emoticon em = findEmoticonWithUrl(source);
                if(em != null) {
                    return em.drawable;
                } else {
                    return null;
                }
            }
        };
        tv.setText(Html.fromHtml(msg, imageGetter, null));
    }

    private Emoticon findEmoticonWithUrl(String url) {
        for (Emoticon emoticon: emoticons) {
            if(emoticon.url.equals(url)) {
                return emoticon;
            }
        }
        return null;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
