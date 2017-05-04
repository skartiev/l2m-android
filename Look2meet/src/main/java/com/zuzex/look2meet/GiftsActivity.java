package com.zuzex.look2meet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.DialogObject;
import com.zuzex.look2meet.DataModel.Gift;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.chat.ChatActivity;
import com.zuzex.look2meet.socket.SocketWorker;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GiftsActivity extends MenuActivity {
    GridView gridViewFree;
    GridView gridViewPremium;
    ArrayList<Gift> giftsListFree;
    ArrayList<Gift> giftsListPremium;
	public int idUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts);
        getActionBar().hide();
        giftsListPremium = new ArrayList<Gift>();
        giftsListFree = new ArrayList<Gift>();
        gridViewFree = (GridView) findViewById(R.id.giftsGridViewFirst);
        gridViewPremium = (GridView) findViewById(R.id.giftsGridViewSecond);

	    idUser = getIntent().getIntExtra("id",0);

	    gridViewFree.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//			    Log.wtf("onItemClick", i +" | "+ l);
			    Gift gift = giftsListFree.get(i);
			    sendGift(gift);
		    }
	    });

	    gridViewPremium.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			    double currTime = System.currentTimeMillis()/1000;
//			    Log.wtf("onItemClick", i +" | "+ l);
//			    Log.wtf("onItemClick", idUser + " paid: "+ currTime +" | "+ UserProfile.getInstance().tarif_userPaidGiftUntil);
			    if (currTime < UserProfile.getInstance().tarif_userPaidGiftUntil) {
				    Gift gift = giftsListPremium.get(i);
				    sendGift(gift);
			    } else {
				    GlobalHelper.showAlert("Необходимо приобрести подписку на подарки.", null);
//				    [GlobalHelper showMessage:LOC(@"Необходимо приобрести подписку на подарки.",@"gifts") withTitle:@""];
			    }
		    }
	    });

        Look2meetApi.getInstance().getGiftList(1,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                parseResponseJson(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
        });
    }

	private void sendGift(final Gift gift) {
        Look2meetApi.getInstance().addDialogsWithUser(idUser, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (GlobalHelper.successStatusFromJson(response, true, null)) {
                    JSONObject data = response.optJSONObject("data");
                    DialogObject dialogObject = new DialogObject(data);
                    SocketWorker.getInstance().sendMessageToChat(dialogObject.dialogId, "", null, gift.paths, null);
                    Intent chatIntent = new Intent(getBaseContext(), ChatActivity.class);
                    chatIntent.putExtra("currDialogObject", dialogObject);
                    startActivity(chatIntent);
                }
            }
        });

	}

    private void parseResponseJson(JSONObject jsonObject) {
        Log.i("GIFTS_LIST", jsonObject.toString());
        try {
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray gifts = data.getJSONArray("gifts");
            for (int i=0;i<gifts.length();i++)
            {
                JSONObject tempJson = (JSONObject) gifts.get(i);
                String path = tempJson.getString("path");
                Boolean free = tempJson.getBoolean("free");
                String url = tempJson.getString("url");
                Gift gift = new Gift(path, free, url);
                if(free) {
                    giftsListFree.add(gift);
                }
                else
                {
                    giftsListPremium.add(gift);
                }
            }
            GiftLoaderAdapter giftLoaderAdapter = new GiftLoaderAdapter(this, giftsListFree);
            gridViewFree.setAdapter(giftLoaderAdapter);
            GiftLoaderAdapter premiumGiftLoaderAdapter = new GiftLoaderAdapter(this,giftsListPremium);
            gridViewPremium.setAdapter(premiumGiftLoaderAdapter);
        } catch (JSONException e) {
            GlobalHelper.loggSend("JSON_EXCEPTION");
            e.printStackTrace();
        }
    }

    private void LoggSend(String message) {
        //SystemInfo info = new SystemInfo(UserProfile.getInstance().id,message);
       // Logging logging = new Logging("look2meet-Android-JSON_EXCEPTION", info.getString());
       // logging.SendMessage();
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        YandexMetrica.onResumeActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        YandexMetrica.onPauseActivity(this);
    }
}
