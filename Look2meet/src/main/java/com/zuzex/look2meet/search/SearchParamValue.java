package com.zuzex.look2meet.search;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by selim on 26/06/14.
 */
public class SearchParamValue  implements Parcelable {

    public String id;
    public String value;

    public SearchParamValue(String value, String id) {
        this.id = id;
        this.value = value;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(value);
    }

    public static final Parcelable.Creator<SearchParamValue> CREATOR = new Parcelable.Creator<SearchParamValue>() {
        public SearchParamValue createFromParcel(Parcel in) {
            return new SearchParamValue(in);
        }
        public SearchParamValue[] newArray(int size) {
            return new SearchParamValue[size];
        }
    };

    private SearchParamValue(Parcel in) {
        id = in.readString();
        value = in.readString();
    }

    @Override
    public String toString() {
        return value;
    }
}
