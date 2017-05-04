package com.zuzex.look2meet.PushNotifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.zuzex.look2meet.utils.IntentUtil;

import org.apache.http.Header;


/**
 * Created by dgureev on 10/8/14.
 */

public class PushRegistrationIntentService extends IntentService {

    public static final String TAG = "PushRegistrationIntentService";
    public static final String INTENT_URL = "INTENT_URL";
    public static final String INTENT_STATUS_CODE = "INTENT_STATUS_CODE";
    public static final String INTENT_HEADERS = "INTENT_HEADERS";
    public static final String INTENT_DATA = "INTENT_DATA";
    public static final String INTENT_THROWABLE = "INTENT_THROWABLE";

    public static final String ACTION_START = "SYNC_START";
    public static final String ACTION_RETRY = "SYNC_RETRY";
    public static final String ACTION_CANCEL = "SYNC_CANCEL";
    public static final String ACTION_SUCCESS = "SYNC_SUCCESS";
    public static final String ACTION_FAILURE = "SYNC_FAILURE";
    public static final String ACTION_FINISH = "SYNC_FINISH";


    public static final String[] ALLOWED_ACTIONS = {ACTION_START,
            ACTION_RETRY, ACTION_CANCEL, ACTION_SUCCESS, ACTION_FAILURE, ACTION_FINISH};

    private AsyncHttpClient aClient = new SyncHttpClient();

    public PushRegistrationIntentService() {
        super("ExampleIntentService");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "onStart()");
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(INTENT_URL)) {
            aClient.get(this, intent.getStringExtra(INTENT_URL), new AsyncHttpResponseHandler() {
                @Override
                public void onStart() {
                    sendBroadcast(new Intent(PushRegistrationIntentService.ACTION_START));
                    Log.d(TAG, "onStart");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Intent broadcast = new Intent(PushRegistrationIntentService.ACTION_SUCCESS);
                    broadcast.putExtra(INTENT_STATUS_CODE, statusCode);
                    broadcast.putExtra(INTENT_HEADERS, IntentUtil.serializeHeaders(headers));
                    broadcast.putExtra(INTENT_DATA, responseBody);
                    sendBroadcast(broadcast);
                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Intent broadcast = new Intent(PushRegistrationIntentService.ACTION_FAILURE);
                    broadcast.putExtra(INTENT_STATUS_CODE, statusCode);
                    broadcast.putExtra(INTENT_HEADERS, IntentUtil.serializeHeaders(headers));
                    broadcast.putExtra(INTENT_DATA, responseBody);
                    broadcast.putExtra(INTENT_THROWABLE, error);
                    sendBroadcast(broadcast);
                    Log.d(TAG, "onFailure");
                }

                @Override
                public void onCancel() {
                    sendBroadcast(new Intent(PushRegistrationIntentService.ACTION_CANCEL));
                    Log.d(TAG, "onCancel");
                }

                @Override
                public void onRetry(int retryNo) {
                    sendBroadcast(new Intent(PushRegistrationIntentService.ACTION_RETRY));
                    Log.d(TAG, String.format("onRetry: %d", retryNo));
                }

                @Override
                public void onFinish() {
                    sendBroadcast(new Intent(PushRegistrationIntentService.ACTION_FINISH));
                    Log.d(TAG, "onFinish");
                }
            });
        }
    }
}