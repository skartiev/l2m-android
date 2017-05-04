package com.zuzex.look2meet.chat;

import com.zuzex.look2meet.api.GlobalHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class ChatMessage implements Serializable {
	public boolean  isIncoming;

	public int  idUser;
	public String   text;
	public Date     date;
//	public String   userName;
//	public String   userImagePath;
	public ArrayList<ChatMessageAttachment> attachmentsArr;

//  {"files":[],"text":" Hello!!","toUser":"7","event":"dialog","isAdd":0,"idUser":"6","date":1.404906284634726E12,"dialogId":4}
//  {"files":[{"guid":"858D2B52-28EF-4E4D-9BD8-DE3B06496A39","id":13,"type":"image","ext":"jpg","name":"0D3937F7-7B65-4541-A97F-B9A12204820F.jpg"}],"text":"And....","toUser":"6","event":"dialog","isAdd":1,"idUser":"7","date":1.404906906127602E12,"dialogId":4}
//	@property(nonatomic) BOOL isShowDate;
//	@property(nonatomic) BOOL isSendByMe;
//	@property(nonatomic,retain) NSString	*text;
//	@property(nonatomic,retain) NSDate		*date;
//	@property(nonatomic,retain) NSString	*userName;
//	@property(nonatomic,retain) NSString	*userImagePath;
//	@property(nonatomic,retain) NSMutableArray	*attachmentsArr;
//
//	@property(nonatomic) DefAttachTypeRBR	type;
//	@property(nonatomic,retain) NSString	*iconPath;
//	@property(nonatomic,retain) NSString	*filePath;
//	@property(nonatomic,retain) NSDictionary *uploadInfo;
//	@property(nonatomic) BOOL isNeedUpload;

//    public ChatMessage(String text, Date time, boolean incoming) {
//        this(text, null, time, incoming);
//    }

//    public ChatMessage(String text, String userName, Date date, boolean incoming) {
//        this.text = text;
//        this.userName = userName;
//        this.date = date;
//        this.incoming = incoming;
//    }

	public ChatMessage(JSONObject object) {
		this.parseFromJson(object);
	}

	public void parseFromJson(JSONObject jsonObject) {
		text = jsonObject.optString("text");
		idUser = jsonObject.optInt("idUser");

		long dateNumber = jsonObject.optLong("date", 0);
		date = new Date(dateNumber);

		attachmentsArr = new ArrayList<ChatMessageAttachment>();
		JSONArray files = jsonObject.optJSONArray("files");
		if (files != null) {
			for (int i = 0; i < files.length(); i++) {
				JSONObject attJson = files.optJSONObject(i);
				if (attJson != null) {
					ChatMessageAttachment att = new ChatMessageAttachment(attJson);
					attachmentsArr.add(att);
				} else {
//					Log.wtf("chat_attach_parser","null ");
				}
			}
		}

		String giftUrl	= jsonObject.optString("giftUrl");
		if (giftUrl != null && giftUrl.length() > 1) {
			ChatMessageAttachment att = new ChatMessageAttachment();
			att.type = ChatMessageAttachment.ChatMessageAttachmentType_image;
			att.guid = giftUrl;
			att.filePath = att.iconPath = GlobalHelper.urlForGiftByPath(giftUrl);
			attachmentsArr.add(att);
		}
	}

	public void updateIsIncomingBySelfId(Integer currUserId) {
		if (currUserId != this.idUser)
			this.isIncoming = true;
	}
}


