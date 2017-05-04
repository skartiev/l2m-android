package com.zuzex.look2meet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.api.Look2meetApi;

import org.apache.http.Header;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";

    private Button login_button;
    private Button register_button;
    private TextView textView;
    private Drawable mActionBarBackgroundDrawable;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login_button = (Button) findViewById(R.id.login_button);
        register_button = (Button) findViewById(R.id.register_button);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.top_bar);
        getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(false);

        checkSavedApiToken();

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent RegisterIntent = new Intent(getBaseContext(), RegistrationActivity.class);
                startActivity(RegisterIntent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                finish();
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent LoginIntent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(LoginIntent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                finish();
            }
        });
    }

    private void checkSavedApiToken() {
        String apiToken = preferences.getString("apiToken", "");
        if(!apiToken.equals("")) {
            Look2meetApi.getInstance().setApiToken(apiToken);
            Intent profilesIntent = new Intent(getBaseContext(), ProfilesActivity.class);
            startActivity(profilesIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            finish();
        }
    }

    private void parse(JSONObject response)  {
            JSONObject data = response.optJSONObject("data");
            int count = data.optInt("countUsers");
            textView = (TextView) findViewById(R.id.userCountText);
            String s = String.valueOf(count);
            s = "Всего наc уже: " + s;
            textView.setText(s);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Look2meetApi.getInstance().isConnected(this)) {
            Look2meetApi.getInstance().getCurrentCountPeoples(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    parse(response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                }

            });
        }
        EasyTracker.getInstance(this).activityStart(this);
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

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}