package com.zuzex.look2meet.DataModel;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.utils.EscapedJSONObject;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dgureev on 7/1/14.
 */
public class UserProfile implements Serializable {

    public static String CHECKIN_TYPE_NOW = "now";
    public static String CHECKIN_TYPE_SOON = "soon";
    public static String CHECKIN_TYPE_PLAN = "plan";

    public int id;
    public int city_id;
    public int country_id;
    public int age;
    public int checkinsCount;
    public String name;
    public String city_name;
    public String country_name;
    public String sex;
    public String title;
    public String phone;
    public String avatarUrl;
    public String adminEmail;
    public boolean isVip;
    public int avatarId;
    public boolean isFavorite;
    public boolean isInBlackList;
    public String guid;
    public int likes;
    public int isLike;
    public boolean isOnline;

    public int tarif_max_video;
    public int tarif_max_photos;
    public int tarif_cnt_profiles;
    public boolean tarif_invisible;
	public double tarif_userPaidGiftUntil;
    public String tarif_opt_name;
    public int unreadMessages;
    public boolean isSelf;
    public int maxSymbolsInMessage;

    public List<Field> fields;
    public ArrayList<UserProfileMedia> photos;
    public ArrayList<UserProfileMedia> video;
    public ArrayList<Integer> added_files;
    public ArrayList<Integer> deleted_files;
    public RequestParams updatedFields;
    public ArrayList<Checkin> checkinsList;
    public String checkinType = "";

    public enum FieldDataType {
        DEFAULT,
        ENUM,
        TEXT,
        VARCHAR,
        TINYINT,
        INT_UNSIGNED
    }

    public static class Field {
        public boolean isEditable;
        public boolean isNull;
        public boolean required;
        public boolean isCheckbox;
        public String title;
        public FieldDataType dataType;
        public String value;
        public String name;
        public String defaultValue;
        public List<String> options;

        public Field() {
        }

        public Field(String title, String value) {
            this.title = title;
            this.value = value;
            this.dataType = FieldDataType.DEFAULT;
            this.name = "";
            this.isNull = false;
            this.required = true;
            this.defaultValue = "";
            this.options = null;
            this.isCheckbox = false;
            this.isEditable = false;
        }

        public static List<Field> parseFromJsonArray(JSONArray jsonFields) {
            ArrayList<Field> fields = new ArrayList<Field>();

            if (jsonFields == null)
                return fields;

            for(int i = 0; i < jsonFields.length(); i++) {
                JSONObject obj = jsonFields.optJSONObject(i);

                String title = obj.optString("title", "");
                Field field = new Field();
                if(title.isEmpty()) {
                    field.isEditable = false;
                    field.isNull = obj.optBoolean("isNull");
                    field.required = false;
                    field.name = "";
                    field.title = obj.optString("label");
                    field.value = field.isNull ? "" : obj.optString("value");
                    field.defaultValue = "";
                    field.isCheckbox = obj.optInt("isCheckbox") == 0 ? false : true;
                    field.options = null;
                    fields.add(field);
                } else {
                    field.isNull = obj.optBoolean("isNull");
                    field.required = obj.optBoolean("required");
                    field.name = obj.optString("name");
                    field.title = obj.optString("title");
                    field.value = field.isNull ? "" : obj.optString("value");
                    field.defaultValue = obj.optString("default");
                    field.dataType = getDataTypeFromString(obj.optString("dataType"));
                    field.options = new ArrayList<String>();
                    if(field.dataType == FieldDataType.ENUM) {
                        JSONArray optionsArray = obj.optJSONArray("options");
                        if(optionsArray != null) {
                            for(int j = 0; j < optionsArray.length(); ++j) {
                                field.options.add(optionsArray.optString(j));
                            }
                        }
                    }
                    fields.add(field);
                }
            }
            return fields;
        }

        private static FieldDataType getDataTypeFromString(String dataTypeString) {
          if(dataTypeString.equals("enum")) {
              return FieldDataType.ENUM;
          } else if(dataTypeString.equals("text")) {
              return FieldDataType.TEXT;
          } else if(dataTypeString.equals("varchar")) {
              return FieldDataType.VARCHAR;
          } else if(dataTypeString.equals("tinyint")) {
            return FieldDataType.TINYINT;
          } else if(dataTypeString.equals("int unsigned")) {
              return FieldDataType.INT_UNSIGNED;
          }
          return FieldDataType.DEFAULT;
        };
    }

    public class Checkin {
        public String type;
        public String object_name;
        public int object_id;
        public Checkin(JSONObject checkin) {
            EscapedJSONObject checkinJson = null;
            try {
                checkinJson = new EscapedJSONObject(checkin.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                this.type = checkinJson.optString("type", "");
                this.object_id  = checkinJson.optInt("object_id", -1);
                this.object_name = checkinJson.optString("object_name", "");
            };
        }
    }

    public UserProfile() {
        this.fillDefaultParams();
    }

    protected ArrayList<UserProfileMedia> createMedia(JSONArray jsonMedia) {
        ArrayList<UserProfileMedia> media = new ArrayList<UserProfileMedia>();
	    if (jsonMedia == null)
		    return media;
        for(int index = 0; index< jsonMedia.length(); index++) {
            media.add(new UserProfileMedia(jsonMedia.optJSONObject(index)));
        }
        return media;
    }

	public void parseFromJson(JSONObject jsonObject) {
        EscapedJSONObject mainInfo = null;
        try {
            mainInfo = new EscapedJSONObject(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            fillDefaultParams();
        }
        JSONArray jsonFields = mainInfo.optJSONArray("addFields");
		fields = Field.parseFromJsonArray(jsonFields);
		id = mainInfo.optInt("id");
		age = mainInfo.optInt("age");
		checkinsCount = mainInfo.optInt("checkinsCnt");
		name = mainInfo.optString("name");
		avatarUrl = mainInfo.optString("avatarUrl");
        avatarUrl = avatarUrl.replace("50x50", "200x200");
        avatarUrl = avatarUrl.replace("70x70", "200x200");
		adminEmail = mainInfo.optString("adminEmail");
		isVip = intToBool(mainInfo.optInt("isVip"));
        avatarId = mainInfo.optInt("public_avatar_id");
        isSelf = mainInfo.optInt("isSelf")== 0? false: true;
        maxSymbolsInMessage = mainInfo.optInt("maxSymbolsInMessage");

        if(maxSymbolsInMessage == 0)
            maxSymbolsInMessage = 1000;

		if(mainInfo.isNull("city_id")) {
			city_id = 0;
		} else {
			city_id = mainInfo.optInt("city_id");
		}
		if(mainInfo.isNull("country_id")) {
			country_id = 0;
		} else {
			country_id = mainInfo.optInt("country_id");
		}

		city_name = mainInfo.optString("city_name");
        if(city_name.equals("false")) {
            city_name = "";
        }
		sex = mainInfo.optString("sex");
		country_name = mainInfo.optString("country_name");
		photos = createMedia(mainInfo.optJSONArray("photos"));
		video = createMedia(mainInfo.optJSONArray("video"));

        JSONObject tarifOptions = mainInfo.optJSONObject("tarifOptions");
		if (tarifOptions != null) {
			tarif_max_video = tarifOptions.optInt("max_video");
			tarif_max_photos = tarifOptions.optInt("max_photos");
			tarif_cnt_profiles = tarifOptions.optInt("cnt_profiles");
			tarif_opt_name = tarifOptions.optString("name");
			tarif_invisible = tarifOptions.optBoolean("invisible");
		}
		tarif_userPaidGiftUntil = mainInfo.optDouble("userPaidGiftUntil");

		updatedFields = new RequestParams();
		added_files = new ArrayList<Integer>();
        deleted_files = new ArrayList<Integer>();

        checkinsList = createCheckins(mainInfo.optJSONArray("checkins"));
        isFavorite = mainInfo.optInt("isFavorite") != 0;
        isInBlackList = mainInfo.optBoolean("isInBlackList");
        guid = mainInfo.optString("guid");
        likes = mainInfo.optInt("likes");

        isLike = mainInfo.optInt("isLike");
        //isLike = mainInfo.optBoolean("isLike");
        isOnline = UserProfile.intToBool(mainInfo.optInt("online"));
        if(mainInfo.has("unreadMessages")){
            unreadMessages = mainInfo.optInt("unreadMessages");
        }
        checkinType = mainInfo.optString("checkin_type");
	}

    protected ArrayList<Checkin> createCheckins(JSONArray checkinsArray) {
        ArrayList<Checkin> checkins = new ArrayList<Checkin>();
        if(checkinsArray != null) {
            for(int i = 0; i < checkinsArray.length(); i++) {
                checkins.add(new Checkin(checkinsArray.optJSONObject(i)));
            }
        }
        return checkins;
    }

	public void fillDefaultParams() {
		fields = new ArrayList<Field>();
		id = -1;
		age = 0;
		checkinsCount = 0;
		name = "";
		avatarUrl = "";
		adminEmail = "";
		city_id = 0;
		city_name = "";
		sex = "";
		video = new ArrayList<UserProfileMedia>();
		country_id = 0;
		country_name = "";
		photos = new ArrayList<UserProfileMedia>();
		updatedFields = new RequestParams();
		isVip = false;
        deleted_files = new ArrayList<Integer>();
        unreadMessages = -1;
        maxSymbolsInMessage = 1000;
	}

    public void addFile(int file_id) {
        if(added_files != null) {
            added_files.add(file_id);
        }
    }
    public void addAvatarId(int avatarId) { this.avatarId = avatarId;}

    public void deleteFile(int file_id) {
        if(deleted_files != null) {
            deleted_files.add(file_id);
        }
    }

    public RequestParams get() {
        updatedFields.put("user[age][value]", String.valueOf(age));
        updatedFields.put("user[age][isNull]", "false");
        updatedFields.put("user[city_id][value]", String.valueOf(city_id));
        updatedFields.put("user[city_id][isNull]", "false");
        updatedFields.put("user[country_id][value]", String.valueOf(country_id));
        updatedFields.put("user[country_id][isNull]", "false");
        updatedFields.put("user[name][value]", name);
        updatedFields.put("user[name][isNull]", "false");
        updatedFields.put("user[sex][value]", sex);
        updatedFields.put("user[sex][isNull]", "false");
        updatedFields.put("idAvatar", String.valueOf(avatarId));

        if(added_files.size() > 0) {
            for(int i = 0; i< added_files.size(); i++) {
                updatedFields.put("added_files[]", String.valueOf(added_files.get(i)));
            }
            added_files.clear();
        }

        if(deleted_files.size() > 0) {
            for(int i = 0; i< deleted_files.size(); i++) {
                updatedFields.put("deleted_files[]", String.valueOf(deleted_files.get(i)));
            }
            deleted_files.clear();
        }

        for (int i=0; i<fields.size(); i++) {
            if(fields.get(i).value.length()>0)  {
                updatedFields.put("user["+fields.get(i).name+"][isNull]","false");
            }
            else
            {
                updatedFields.put("user["+fields.get(i).name+"][isNull]","true");
            }
            updatedFields.put("user["+fields.get(i).name+"][value]",fields.get(i).value);
        }

//        updatedFields.put("user[idAvatar][isNull]", "true");
//        updatedFields.put("user[added_files][isNull]", "true");
//        updatedFields.put("user[deleted_files][isNull]", "true");
        return updatedFields;
    }

    private volatile static UserProfile instance;

    public static UserProfile getInstance() {
        if (instance == null) {
            synchronized (UserProfile.class) {
                if (instance == null) {
                    instance = new UserProfile();
                }
            }
        }
        return instance;
    }

    public void reload(final IUpdateStatus iMyUserProfile) {
        Look2meetApi.getInstance().getUserProfile(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONObject data = response.optJSONObject("data");
                parseFromJson(data);
                iMyUserProfile.onUpdateSuccess(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                GlobalHelper.loggSend("ERROR_GET_USER");
                iMyUserProfile.onUpdateError();
            }
        });

    }

    private static boolean intToBool(int num) {
        if(num == 0) {
            return false;
        }
        return true;
    }

    public String getLocaleCheckinType(Context context) {
        if(checkinType == null) {
            return "";
        }
        String translated_checkin_type = "";
        if(checkinType.equals(UserProfile.CHECKIN_TYPE_NOW)) {
            translated_checkin_type = context.getString(R.string.checkin_type_now);
        } else if(checkinType.equals(UserProfile.CHECKIN_TYPE_PLAN)) {
            translated_checkin_type = context.getString(R.string.checkin_type_plan);
        }else if(checkinType.equals(UserProfile.CHECKIN_TYPE_SOON)) {
            translated_checkin_type = context.getString(R.string.checkin_type_soon);
        }
        return translated_checkin_type;
    }
}


