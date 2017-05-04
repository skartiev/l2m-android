package com.zuzex.look2meet.PushNotifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.zuzex.look2meet.Look2meet;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.IOException;

public class GcmHelper {

    private static final String TAG = "PUSH_NOTIFICATIONS";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_VERSION = "app_version";
    private static final String SENDER_ID = "26767503074";

    private GoogleCloudMessaging gcm;
    private Context context;
    private SharedPreferences prefs;
    private String regId;

    public GcmHelper(Context ctx) {
        context = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        gcm = GoogleCloudMessaging.getInstance(context);
    }

    public void sendRegistrationId() {
        regId = getRegistrationId();
//        Log.d(TAG, "regid from preferences: " + regId);

        if (regId.isEmpty()) {
            registerInBackground();
        } else {
            sendRegIdToServer();
        }
    }

    private String getRegistrationId() {
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
//            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
//            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void,Long,String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regId;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    if(!regId.isEmpty()) {
                        sendRegIdToServer();
                    }

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(String regId) {
        int appVersion = getAppVersion(context);
//        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_VERSION, appVersion);
        editor.apply();
    }

    public void removeRegistrationId() {
        final Context ctx = Look2meet.getContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_REG_ID, "");
        String registrationId = preferences.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return;
        } else {
            Look2meetApi.getInstance().deleteRegistrationId(context, registrationId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if(GlobalHelper.successStatusFromJson(response, false, ctx)) {
                        Log.d(TAG, "Successfully regid removed from server: "+response.toString());
                    } else {
                        String errMsg = response.optString("message");
                        Log.e(TAG, "Remove regid error: " + errMsg);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    try {
                        Log.w(TAG, "STATUS CODE "+String.valueOf(statusCode));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        editor.apply();
    }

    private void sendRegIdToServer() {
        Look2meetApi.getInstance().setRegistrationId(context, regId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if(GlobalHelper.successStatusFromJson(response, false, context)) {
                    Log.w(TAG, "Successfully sent regid: " + regId+" "+response.toString());
                } else {
                    String errMsg = response.optString("message");
                    Log.w(TAG, "set registration id error: " + response.toString());
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.w(TAG, "Fail send registration"+statusCode, throwable);
            }
        });
    }
}
