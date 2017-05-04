package com.zuzex.look2meet.tests;

import android.test.InstrumentationTestCase;

import com.zuzex.look2meet.api.apiData.LoginRequest;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dgureev on 6/2/14.
 */
public class AddUserTests extends InstrumentationTestCase {
    public void testAddUser() throws Exception {
        LoginRequest l = new LoginRequest();
        String email = "test@test.te";
        String password = "123123";
        String phone = "321321";
        Boolean rem = true;
        l.setEmail(email);
        l.setPassword(password);
        l.setPhone(phone);
        l.setRemember(rem);
        Map<String, String> result = new HashMap<String, String>();
        result.put("email", email);
        result.put("password", password);
        result.put("phone", phone);
        result.put("remember", Boolean.toString(rem));
        Assert.assertEquals(l.toMap(),result);
    }
}
