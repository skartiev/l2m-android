package com.zuzex.look2meet.api.apiData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dgureev on 5/29/14.
 */
public class LoginRequest {
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemember() {
        return Boolean.toString(this.remember);
    }

    public void setRemember(Boolean remember) {
        this.remember = remember;
    }

    //    email (String) Email (необязательно, если заполнен phone)
    private String email;
    //    phone (String) Телефон (необязательно, если заполнен email)
    private String phone;
    //    password (String) Пароль
    private String password;
    //    remember (Bool) «Запомнить меня»
    private Boolean remember;

    public LoginRequest() {
        this.email = "";
        this.password = "";
        this.phone = "";
        this.remember = false;
    }

    public Map<String, String> toMap() {
        Map<String, String> params = new HashMap<String, String>();
        Method[] methods = this.getClass().getMethods();
        for(Method m : methods)
        {
            try {
                if (m.getName().startsWith("get") && !m.getName().contains("getClass")) {
                    String value = (String) m.invoke(this);
                    params.put(m.getName().substring(3).toLowerCase(), value);
                }
            }
            catch (IllegalAccessException e) {
                e.fillInStackTrace();
            }
            catch (InvocationTargetException e) {
                e.fillInStackTrace();
            }
        }
        return params;
    };
}
