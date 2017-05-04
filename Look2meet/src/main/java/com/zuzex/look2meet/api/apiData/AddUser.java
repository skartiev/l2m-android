package com.zuzex.look2meet.api.apiData;

import com.loopj.android.http.RequestParams;

/**
 * Created by dgureev on 5/29/14.
 */
public class AddUser {
    //    user[email] (String) Email пользователя
    private String email;
    //    user[password] (String) Пароль пользователя
    private String password;
    //    user[confirm_password] (String) Подтверждениме пароля
    private String confirm_password;
    //    user[name] (String) Имя пользователя
    private String name;
    //    user[phone] (String) Телефон (необязятельное)
    private String phone;
    //    user[role] (String) Роль пользователя в системе. Возможные значения: user, adw
    private String role;
    //    user[sex] (String) Пол. Возможные значения: male, female
    private String sex;
    //    user[lat] (Float) Координаты пользователя.Широта (необязятельное)
    private float lat;
    //    user[lon] (Float) Координаты пользователя.Долгота (необязятельное)
    private float lon;

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLat(double lat) {
        this.lat = (float) lat;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        if(sex) {
            this.sex = "male";
        }
        else {
            this.sex = "female";
        }
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }
    public void setLon(double lon) {
        this.lon = (float) lon;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getConfirm_password() {
        return confirm_password;
    }

    public void setConfirm_password(String confirm_password) {
        this.confirm_password = confirm_password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AddUser() {
        this.confirm_password= "";
        this.password = "";
        this.name= "";
        this.email = "";
        this.role = "user";
        this.sex = "male";
        this.phone = "";
        this.lat = 0;
        this.lon = 0;
    }

    public AddUser(String email, String password, String confirm_password, String name, String phone, String role, String sex, float lat, float lon) {
        this.email = email;
        this.password = password;
        this.confirm_password = confirm_password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.sex = sex;
        this.lat = lat;
        this.lon = lon;
    }

    public RequestParams getParams() {
        RequestParams params = new RequestParams();
        params.put("user[email]", email);
        params.put("user[name]", this.name);
        params.put("user[password]", this.password);
        params.put("user[confirm_password]", this.confirm_password);
        params.put("user[phone]", this.phone);
        params.put("user[role]", this.role);
        params.put("user[sex]", this.sex);
        params.put("user[lat]", Float.toString(this.lat));
        params.put("user[lon]", Float.toString(this.lon));
        return params;
    }
}