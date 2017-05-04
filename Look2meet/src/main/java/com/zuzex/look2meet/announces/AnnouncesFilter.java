package com.zuzex.look2meet.announces;

import android.os.Parcel;
import android.os.Parcelable;

import com.loopj.android.http.RequestParams;

public class AnnouncesFilter implements Parcelable {

    public String title;
    public String checkinType;
    public String category;
    public String query;
    public long dateFrom;
    public long dateTo;
    public double lat = 0.0;
    public double lon = 0.0;

    public AnnouncesFilter(double lat, double lon) {
        checkinType = "";
        query = "";
        category = "";
        dateFrom = System.currentTimeMillis() / 1000 - 72*60*60;
        dateTo = dateFrom + 72*60*60;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(checkinType);
        parcel.writeLong(dateFrom);
        parcel.writeLong(dateTo);
    }

    public static final Parcelable.Creator<AnnouncesFilter> CREATOR = new Parcelable.Creator<AnnouncesFilter>() {
        public AnnouncesFilter createFromParcel(Parcel in) {
            return new AnnouncesFilter(in);
        }
        public AnnouncesFilter[] newArray(int size) {
            return new AnnouncesFilter[size];
        }
    };

    private AnnouncesFilter(Parcel in) {
        title = in.readString();
        checkinType = in.readString();
        dateFrom = in.readLong();
        dateTo = in.readLong();
    }

    public RequestParams getData() {
        RequestParams data = new RequestParams();
        //todo not work with parameters
//        data.put("latitude", Double.toString(lat));
//        data.put("longitude", Double.toString(lon));
//        if(!checkinType.isEmpty())
//            data.put("checkin_type", checkinType);
//        if(!query.isEmpty())
//            data.put("query", query);
//        if(!category.isEmpty())
//            data.put("category", category);
//        if(dateFrom != 0 && dateTo != 0) {
//            data.put("dateStart", Long.toString(dateFrom));
//            data.put("dateEnd", Long.toString(dateFrom));
//        }
        return data;
    }
}
