package com.zuzex.look2meet.DataModel;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by dgureev on 7/2/14.
 */
public class UserProfileMedia implements Parcelable{
    public int id;
    public String name;
    public String extension;
    public int size;
    public String preview;
    public String url;

    public UserProfileMedia(JSONObject jsonObject) {
        if(jsonObject.has("data")) {
            jsonObject = jsonObject.optJSONObject("data");
        }
        this.id = jsonObject.optInt("id");
        this.name = jsonObject.optString("name");
        this.extension = jsonObject.optString("extension");
        this.size = jsonObject.optInt("size");
        this.preview = jsonObject.optString("preview");
        this.url = jsonObject.optString("url");
    }

    public static final Parcelable.Creator<UserProfileMedia> CREATOR = new Parcelable.Creator<UserProfileMedia>() {
        public UserProfileMedia createFromParcel(Parcel in) {
            return new UserProfileMedia(in);
        }
        public UserProfileMedia[] newArray(int size) {
            return new UserProfileMedia[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(extension);
        parcel.writeInt(size);
        parcel.writeString(preview);
        parcel.writeString(url);
    }

    private UserProfileMedia(Parcel in) {
        id = in.readInt();
        name = in.readString();
        extension = in.readString();
        size = in.readInt();
        preview = in.readString();
        url = in.readString();
    }
}