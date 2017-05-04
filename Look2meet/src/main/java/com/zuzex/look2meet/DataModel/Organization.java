package com.zuzex.look2meet.DataModel;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.utils.EscapedJSONObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;

public class Organization implements Parcelable {

    public int id;
    public String guid;

    public String name;
    public String address;
    public String workTime;
    public String announceShort;
    public String announceMedium;
    public String announceFull;
    public String categoryName;
    public String avatarUrl;

    public boolean isTop;
    public boolean isVip;
    public boolean isLiked;
    public boolean isFavorite;

    public int likesCount;
    public int maleCountAll;
    public int maleCountNow;
    public int maleCountSoon;
    public int maleCountPlan;
    public int femaleCountAll;
    public int femaleCountNow;
    public int femaleCountSoon;
    public int femaleCountPlan;

    public String checkinType;
    public long checkinCreate;
    public long checkinExpire;
    public long checkinExpireLeft;

    public float latitude;
    public float longitude;

    public Organization() {

    }

    public static Organization ParseFromJson(JSONObject data) {
        Organization org = new Organization();
        org.UpdateFromJson(data);
        return org;
    }


//    В результате возвращается массив object следующего вида:
//    id (int) Идентификатор объекта
//    name (String) (Наименование объекта)
//    type (String) Тип объекта. Возможные значения: temporary, constant
//    lat (Float) Координаты объекта - широта
//    long (Float) Координаты объекта - долгота
//    city_id (int) Идентификатор города
//    country_id (int) Идентификатор страны
//    category_id (int) Идентификатор категории
//    address (String) Адрес объекта
//    anons_full (String) Полное описание объекта
//    work_time (String) Рабочее время
//    city_name (String) Название города
//    country_name (String) Название страны
//    likes_cnt (int) Количество лайков
//    category_name (String) Название категории
//    isTop (boolean) Находится ли объект в топе
//    avatarUrl (String) Ссылка на аватар
//    isFavorite (boolean) Флаг, определяющий находится ли объект в «Избранное»
//    isVip (boolean) Флаг, определяющий является ли объект VIP
//    checkins (Array) Список чекинов. Представляет собой массив:
//    male_cnt_now (int) Количество мужчин, зачекининых со статусом «Я тут»
//    male_cnt_soon (int) Количество мужчин, зачекининых со статусом «Скоро буду»
//    male_cnt_plan (int) Количество мужчин, зачекининых со статусом «Планирую»
//    female_cnt_now (int) Количество женщин, зачекининых со статусом «Я тут»
//    female_cnt_soon (int) Количество женщин, зачекининых со статусом «Скоро буду»
//    female_cnt_plan (int) Количество женщин, зачекининых со статусом ««Планирую»
//    me (String) Если пользователь зачекинен на объекте, возвращает тип чекина. Возможные значения: now, soon, plan
//                                                                                                              addFields (Array) Массив дополнительных полей. Каждый элемент содержит поля:
//    label (String) Надпись для поля
//    isCheckbox (boolean) Флаг, определяющий является ли поле чекбоксом
//    value (Mixed) Значение поля для данного объекта
//    isNull (boolean) Флаг, определяющий отмечен, ли чекбокс
//    checkin_type (String) Тип чекина
//    checkin_create (int) Дата создания чекина
//    checkin_expire (int) Дата истечения чекина
//    checkin_expire_left (int) Оставшееся время чекина


    public void UpdateFromJson(JSONObject jsonObject) {
        EscapedJSONObject data = null;
        try {
            data = new EscapedJSONObject(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        id = data.optInt("id");
        if(id == 0) {
            id = data.optInt("object_id");
        }
        guid = data.optString("guid");
	    name = data.optString("name");
        address = data.optString("address");
        workTime = data.optString("work_time");
        announceShort = data.optString("anons_short");
        if(announceShort.isEmpty()) {
            announceShort = data.optString("anonsShort");
        }
        if(announceShort.equals("false")) {
            announceShort = "";
        }
        announceMedium = data.optString("anons_medium");
        announceFull = data.optString("anons_full");
        categoryName = data.optString("category_name");
        avatarUrl = data.optString("avatarUrl");
        String rx = "(\\d\\dx\\d\\d)";
        avatarUrl = avatarUrl.replaceFirst(rx, "200x200");
        if(!avatarUrl.startsWith("http")) {
            avatarUrl = Look2meetApi.HOST + avatarUrl;
        }

        likesCount = data.optInt("likes_cnt");


//        male_cnt_now (int) Количество мужчин, зачекининых со статусом «Я тут»
//        male_cnt_soon (int) Количество мужчин, зачекининых со статусом «Скоро буду»
//        male_cnt_plan (int) Количество мужчин, зачекининых со статусом «Планирую»
//        female_cnt_now (int) Количество женщин, зачекининых со статусом «Я тут»
//        female_cnt_soon (int) Количество женщин, зачекининых со статусом «Скоро буду»
//        female_cnt_plan (int) Количество женщин, зачекининых со статусом ««Планирую»
//        me (String) Если пользователь зачекинен на объекте, возвращает тип чекина. Возможные

        if(data.has("checkinsAll")) {
            JSONObject checkinsAll = data.optJSONObject("checkinsAll");
            parseCheckins(checkinsAll);
        } else if(data.has("checkins")) {
            Object checkins_tmp = data.opt("checkins");
            if (checkins_tmp instanceof JSONObject) {
                parseCheckins((JSONObject) checkins_tmp);
            } else {
                maleCountAll = data.optInt("male_cnt", 0);
                maleCountNow = data.optInt("male_cnt_now", 0);
                maleCountSoon = 0;
                maleCountPlan = 0;
                femaleCountAll = data.optInt("female_cnt", 0);
                femaleCountNow = data.optInt("female_cnt_now", 0);
                femaleCountSoon = 0;
                femaleCountPlan = 0;
            }
        }

        isTop = parseBoolean("isTop", data);
        isVip = parseBoolean("isVip", data);
        isLiked = parseBoolean("like", data);
        isFavorite = parseBoolean("isFavorite", data);

        checkinType = data.optString("checkin_type");
        if(checkinType.equals("false")) {
            checkinType = "";
        }
        checkinCreate = data.optLong("checkin_create");
        checkinExpire = data.optLong("checkin_expire");
        checkinExpireLeft = data.optLong("checkin_expire_left");

        latitude = (float)data.optDouble("lat", 0);
        longitude = (float)data.optDouble("long", 0);
    }

    private void parseCheckins(JSONObject checkins) {
        maleCountNow = checkins.optInt("male_cnt_now", 0);
        maleCountSoon = checkins.optInt("male_cnt_soon", 0);
        maleCountPlan = checkins.optInt("male_cnt_plan", 0);
        maleCountAll = maleCountNow + maleCountSoon + maleCountPlan;
        femaleCountNow = checkins.optInt("female_cnt_now", 0);
        femaleCountSoon = checkins.optInt("female_cnt_soon", 0);
        femaleCountPlan = checkins.optInt("female_cnt_plan", 0);
        femaleCountAll = femaleCountNow + femaleCountSoon + femaleCountPlan;
    }

    // Parcelable implementation

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(guid);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeString(workTime);
        parcel.writeString(announceShort);
        parcel.writeString(announceMedium);
        parcel.writeString(announceFull);
        parcel.writeString(categoryName);
        parcel.writeString(avatarUrl);
        parcel.writeByte((byte)(isTop ? 1 : 0));
        parcel.writeByte((byte)(isVip ? 1 : 0));
        parcel.writeByte((byte)(isLiked ? 1 : 0));
        parcel.writeByte((byte)(isFavorite ? 1 : 0));
        parcel.writeInt(likesCount);
        parcel.writeInt(maleCountAll);
        parcel.writeInt(maleCountNow);
        parcel.writeInt(maleCountSoon);
        parcel.writeInt(maleCountPlan);
        parcel.writeInt(femaleCountAll);
        parcel.writeInt(femaleCountNow);
        parcel.writeInt(femaleCountSoon);
        parcel.writeInt(femaleCountPlan);
        parcel.writeString(checkinType);
        parcel.writeLong(checkinCreate);
        parcel.writeLong(checkinExpire);
        parcel.writeLong(checkinExpireLeft);
        parcel.writeFloat(longitude);
        parcel.writeFloat(latitude);
    }

    public static final Parcelable.Creator<Organization> CREATOR = new Parcelable.Creator<Organization>() {
        public Organization createFromParcel(Parcel in) {
            return new Organization(in);
        }
        public Organization[] newArray(int size) {
            return new Organization[size];
        }
    };

    private Organization(Parcel in) {
        id = in.readInt();
        guid = in.readString();
        name = in.readString();
        address = in.readString();
        workTime = in.readString();
        announceShort = in.readString();
        announceMedium = in.readString();
        announceFull = in.readString();
        categoryName = in.readString();
        avatarUrl = in.readString();
        isTop = in.readByte() != 0;
        isVip = in.readByte() != 0;
        isLiked = in.readByte() != 0;
        isFavorite = in.readByte() != 0;
        likesCount = in.readInt();
        maleCountAll = in.readInt();
        maleCountNow = in.readInt();
        maleCountSoon = in.readInt();
        maleCountPlan = in.readInt();
        femaleCountAll = in.readInt();
        femaleCountNow = in.readInt();
        femaleCountSoon = in.readInt();
        femaleCountPlan = in.readInt();
        checkinType = in.readString();
        checkinCreate = in.readLong();
        checkinExpire = in.readLong();
        checkinExpireLeft = in.readLong();
        longitude = in.readFloat();
        latitude = in.readFloat();
    }

    public boolean isValidCoord() {
        if(this.longitude != 0 && this.latitude != 0) {
            return true;
        }
        return false;
    }

    public boolean parseBoolean(String key, JSONObject jsonObject) {
        boolean result = jsonObject.optBoolean(key);
        if(jsonObject.optInt(key) == 1) {
            result = true;
        }
        return result;
    }
}
