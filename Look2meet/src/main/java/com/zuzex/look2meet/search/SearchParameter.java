package com.zuzex.look2meet.search;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchParameter implements Parcelable {

    Integer id;
    String name;
    SearchParamValue value;

    public SearchParameter(String name, SearchParamValue value, Integer id) {

        this.id = id;
        this.name = name;
        this.value = value;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(name);
        out.writeParcelable(value, flags);
    }

    public static final Parcelable.Creator<SearchParameter> CREATOR = new Parcelable.Creator<SearchParameter>() {
        public SearchParameter createFromParcel(Parcel in) {
            return new SearchParameter(in);
        }
        public SearchParameter[] newArray(int size) {
            return new SearchParameter[size];
        }
    };

    private SearchParameter(Parcel in) {
        id = in.readInt();
        name = in.readString();
        value = in.readParcelable(SearchParamValue.class.getClassLoader());
    }
}
