package com.zuzex.look2meet.DataModel;

import com.zuzex.look2meet.api.GlobalHelper;

import org.json.JSONObject;

/**
 * Created by dgureev on 7/3/14.
 */
public class MediaUploadResponse {

	public String guid;
	public int id = -1;
	public String type;
	public String ext;
	public String name;

	public JSONObject uploadInfo;

    //{"data":{"guid":"BF3B1396-8155-48A0-AE10-C6D583E17ABC","id":485,"type":"image","ext":"jpg","name":"JPEG_20140704_141607_1526560082.jpg"},"isLoginExpired":false,"success":true}
    public MediaUploadResponse(JSONObject jsonObject) {
        if(GlobalHelper.successStatusFromJson(jsonObject)) {
            JSONObject data = jsonObject.optJSONObject("data");
            uploadInfo = data;
            guid = data.optString("guid");
            id = data.optInt("id");
            type = data.optString("type");
            ext = data.optString("ext");
            name = data.optString("name");
        }
    }
}
