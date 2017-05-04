package com.zuzex.look2meet;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.IUpdateStatus;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.PushNotifications.GcmHelper;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.socket.SocketWorker;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

public class ProfilesActivity extends MenuActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private static String TAG = "ProfilesActivity";
    private SharedPreferences preferences;
    private ListView listView;
    private ProfilesLoaderAdapter adapter;
    private GcmHelper gcmHelper;

    private ArrayList<UserProfile> userProfiles = new ArrayList<UserProfile>();
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);
        Intent intent = getIntent();
        Integer extraInfo = intent.getIntExtra("ACTIVE", 0);
        if(extraInfo.equals(0))
        {
           LinearLayout layout = (LinearLayout) findViewById(R.id.top_layout);
           layout.removeAllViews();
           LayoutInflater inflater = getLayoutInflater();
           layout.addView(inflater.inflate(R.layout.search_favorite_friends_menu_disabled, null));

           LinearLayout layoutBottom = (LinearLayout) findViewById(R.id.bottom_layout);
           layoutBottom.removeAllViews();
           layoutBottom.addView(inflater.inflate(R.layout.main_menu_disabled, null));
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        gcmHelper = new GcmHelper(this);
        listView = (ListView) findViewById(R.id.profiles_list_);
        checkGMSVersion();
    }

    private void checkGMSVersion () {
        Integer resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            //Do what you want
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                //This dialog will help the user update to the latest GooglePlayServices
                dialog.show();
            }
        }
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
        loadProfiles();
    }


    void loadProfiles() {
        preloader.launch();
        //TextView emptyTextView = (TextView) findViewById(R.id.profiles_empty_message);
        final Button emptyButton = (Button) findViewById(R.id.profiles_empty);
        Look2meetApi.getInstance().getProfiles(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                parseResponseJson(response);
                emptyButton.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                SharedPreferences.Editor e = preferences.edit();
                e.putString("apiToken", "");
                e.commit();
                preloader.cancel(getResources().getString(R.string.error), getResources().getString(R.string.message_network_error));
                emptyButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                SharedPreferences.Editor e = preferences.edit();
                e.putString("apiToken", "");
                e.commit();
                preloader.cancel(getResources().getString(R.string.error), getResources().getString(R.string.message_network_error));
                emptyButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void parseResponseJson(JSONObject jsonObject) {
        if(GlobalHelper.successStatusFromJson(jsonObject, true, this)) {
            userProfiles.clear();
            JSONObject data = jsonObject.optJSONObject("data");
            JSONArray profiles = data.optJSONArray("profiles");
            listView = (ListView) findViewById(R.id.profiles_list_);
            for(int i = 0; i < profiles.length(); i++) {
                JSONObject profile = profiles.optJSONObject(i);
                UserProfile p = new UserProfile();
                p.parseFromJson(profile);
                userProfiles.add(p);
            }

            //TODO AWW SHIT !!1
            //fake user as "ADD PROFILE" field
            UserProfile fakeLastProfile = new UserProfile();
            fakeLastProfile.name = "NULLPOINTEREXCEPTION_";
            userProfiles.add(fakeLastProfile);
            adapter = new ProfilesLoaderAdapter(this, userProfiles);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
            listView.setOnItemLongClickListener(this);
            preloader.done();
        }
    }

    @Override
    public void onDestroy()
    {
        if(listView != null) {
            listView.setAdapter(null);
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        gcmHelper.sendRegistrationId();
        if(i == userProfiles.size()-1) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://look2meet.com/user/profile/#addProfile"));
            startActivity(browserIntent);
        } else {
            preloader.launch();
            setActiveProfile(userProfiles.get(i));
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }
    private void setActiveProfile(final UserProfile userProfile) {
        Look2meetApi.getInstance().setActiveProfile(userProfile.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                UserProfile.getInstance().reload(new IUpdateStatus() {
                    @Override
                    public void onUpdateSuccess(JSONObject response) {
                        UserProfile.getInstance().unreadMessages = userProfile.unreadMessages;
                        loadNext(UserProfile.getInstance().id);
                        preloader.done();
                    }
                    @Override
                    public void onUpdateError() {
                        preloader.cancel(getString(R.string.error), getString(R.string.message_data_recieving_error));
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    GlobalHelper.loggSend("NETWORK_ERROR");
                    preloader.cancel(getString(R.string.error), getString(R.string.message_network_error));
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                GlobalHelper.loggSend("NETWORK_ERROR");
                preloader.cancel(getString(R.string.error), getString(R.string.message_network_error));
            }
        });

    };

    void loadNext(final int id) {
        boolean firstRunProfile = preferences.getBoolean("first_run_" + String.valueOf(id), true);
        String token = Look2meetApi.getInstance().getApiToken();
        SocketWorker.getInstance().connectToSocket(id, token);
        if (firstRunProfile) {
            loadLookingForActivity();
        } else {
            loadMapActivity();
        }
    }

    @Override
    public void loadMapActivity() {
        Intent mapIntent = new Intent(getBaseContext(), MapActivity.class);
        startActivity(mapIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(isTaskRoot()) {
            final Scheduler.Worker worker;
            worker = AndroidSchedulers.mainThread().createWorker();
            if (doubleBackToExitPressedOnce) {
                moveTaskToBack(false);
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.double_tap_to_exit), Toast.LENGTH_SHORT).show();
            worker.schedule(new Action0() {
                @Override
                public void call() {
                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                            worker.unsubscribe();
                        }
                    }, 3000);
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        YandexMetrica.onPauseActivity(this);
    }

    public void updateProfiles(View view) {
        loadProfiles();
        Button emptyButton = (Button) findViewById(R.id.profiles_empty);
        emptyButton.setVisibility(View.GONE);
    }
}
