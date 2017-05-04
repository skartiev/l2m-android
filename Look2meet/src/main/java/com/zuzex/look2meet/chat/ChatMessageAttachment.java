package com.zuzex.look2meet.chat;

import com.zuzex.look2meet.api.GlobalHelper;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by romanabashin on 16.07.14.
 */

//enum ChatMessageAttachmentType {
//	ChatMessageAttachmentType_document,
//	ChatMessageAttachmentType_image,
//	ChatMessageAttachmentType_video,
//	ChatMessageAttachmentType_audio
//};


public class ChatMessageAttachment implements Serializable {
//	public String guid;
	public String iconPath;
	public String filePath;
	public String guid;
	public Integer type = ChatMessageAttachmentType_file;

	public static Integer ChatMessageAttachmentType_file = 0;
	public static Integer ChatMessageAttachmentType_image = 1;
	public static Integer ChatMessageAttachmentType_video = 2;
	public static Integer ChatMessageAttachmentType_audio = 3;

	public ChatMessageAttachment () {	}

	public ChatMessageAttachment (JSONObject jsonObject) {
		this.parseFromJson(jsonObject);
	}

	public void parseFromJson(JSONObject jsonObject) {
		String type = jsonObject.optString("type");
		String ext = jsonObject.optString("ext");
		guid = jsonObject.optString("guid");

		if (type.equals("image")) {
			this.type = ChatMessageAttachmentType_image;
			iconPath = GlobalHelper.urlForImageByGuid(guid,ext);
			filePath = iconPath;
		} else if (type.equals("video")) {
			this.type = ChatMessageAttachmentType_video;
			iconPath = GlobalHelper.urlForVideoThumbnailByGuid(guid);
			filePath = GlobalHelper.urlForVideoByGuid(guid,ext);
		}
	}
}
