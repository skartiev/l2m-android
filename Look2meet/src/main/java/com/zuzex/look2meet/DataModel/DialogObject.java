package com.zuzex.look2meet.DataModel;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sanchirkartiev on 11.07.14.
 */

public class DialogObject extends UserProfile implements Serializable {
	public boolean isDialogStatus;
	public int dialogId;
	public int userFromId;
	public int userToId;
	public int organizId;
	public String organizName;

    public Date date = new Date(0);
//	[{"userFromId":"6","unreadMessages":0,"city_name":"Ростов-на-Дону","sex":"female","dialogStatus":"offline","isVip":0,"status":"plan","tarifOptions":false,"userPaidGiftUntil":false,"country_id":"1","object":{"id":"509167","name":"Черная дыра"},"country_name":"Россия","online":1,"city_id":"1750","id":"7","title":"","totalcheckins":"5","userToId":"7","isFavorite":1,"avatarUrl":"http:\/\/look2meet.com\/uploads\/img\/E9\/2C\/E92C8CE0-6C0B-4F08-99B2-4F740A87A80D\/70x70.png","age":"18","name":"Дженнифер Лоренс","isSelf":0,"dialogId":"4"},{"userFromId":"1","unreadMessages":1,"city_name":"Ростов-на-Дону","sex":"male","dialogStatus":"offline","isVip":0,"tarifOptions":false,"userPaidGiftUntil":false,"country_id":"1","object":{"id":"1","name":"Телефон доверия, Управление Федеральной службы РФ по контролю за оборотом наркотиков по Республике Хакасия"},"country_name":"Россия","online":0,"city_id":"1750","id":"1","title":"Какой-нибудь статус","totalcheckins":"8","userToId":"6","isFavorite":1,"avatarUrl":"http:\/\/look2meet.com\/assets\/img\/user_default_male.png","age":"23","name":"Администратор","isSelf":0,"dialogId":"5"}]

	public DialogObject(JSONObject object)
    {
        super.parseFromJson(object);
	    this.parseFromJson(object);
    }

    public DialogObject(int dialogId, int userFromId, int userToId) {
        super.fillDefaultParams();
        this.dialogId = dialogId;
        this.userFromId = userFromId;
        this.userToId = userToId;
    }

	public void parseFromJson(JSONObject jsonObject)
	{
		// TODO: return commented when fixed fill of dialogObject
		JSONObject user = jsonObject;
		if (jsonObject.optJSONObject("user") != null)
			user = jsonObject.optJSONObject("user");
		super.parseFromJson(user);
		this.isDialogStatus	= jsonObject.optString("dialogStatus").equalsIgnoreCase("online");
		// TODO remove if (right is "id")
		this.dialogId       = jsonObject.optInt("dialogId");
		if (this.dialogId == 0)
			this.dialogId	= jsonObject.optInt("id");
		this.unreadMessages	= jsonObject.optInt("unreadMessages");
		this.userFromId		= jsonObject.optInt("userFromId");
		this.userToId		= jsonObject.optInt("userToId");
		JSONObject organization = jsonObject.optJSONObject("object");
		if (organization != null) {
			this.organizId  = organization.optInt("id");
			this.organizName= organization.optString("name");
		}
	}

}
