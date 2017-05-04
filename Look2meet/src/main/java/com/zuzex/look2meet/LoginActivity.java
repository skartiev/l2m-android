package com.zuzex.look2meet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.api.apiData.LoginRequest;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.http.Header;
import org.json.JSONObject;

public class LoginActivity extends MenuActivity {
    private final static String TAG = "LoginActivity";
    private static int MIN_PASSWORD_LENGTH = 6;

    Button login_enter_button;
    EditText login_email;
    EditText login_password;
    TextView text;
    Switch login_switch_remeber;

    SharedPreferences preferences;

	private Context savedContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        login_enter_button = (Button) findViewById(R.id.login_enter_button);
        login_email = (EditText) findViewById(R.id.login_email);
        login_password = (EditText) findViewById(R.id.login_password);
        login_switch_remeber = (Switch) findViewById(R.id.login_switch_remeber);
        text = (TextView) findViewById(R.id.login_forget_password);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        login_switch_remeber.setChecked(true);
        getActionBar().hide();

        text.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://look2meet.com/#forgotPassword"));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(browserIntent);
            }
        });
	    savedContext = this;
        login_enter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Look2meetApi.getInstance().isConnected(savedContext)) {
                    preloader.launch();
                    LoginRequest loginData = new LoginRequest();
                    String email = login_email.getText().toString();
                    SharedPreferences.Editor e = preferences.edit();
                    e.putString("email",email);
                    e.commit();
                    String password = login_password.getText().toString();

                    if(!EmailValidator.getInstance().isValid(email)) {
                        preloader.cancel(getResources().getString(R.string.message_wrong_data), getString(R.string.message_wrong_email));
                        return;
                    } else if(password.length()<MIN_PASSWORD_LENGTH) {
                        preloader.cancel(getResources().getString(R.string.message_wrong_data), getString(R.string.message_password_short));
                        return;
                    }
                    loginData.setEmail(login_email.getText().toString());
                    loginData.setPassword(login_password.getText().toString());
                    Look2meetApi.getInstance().login(loginData, responseHandler());
                }
            }
        });


        login_enter_button.setLongClickable(true);
        login_enter_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                LoginRequest loginData = new LoginRequest();
                loginData.setEmail("meow@mail.com");
                loginData.setPassword("123123");
                Look2meetApi.getInstance().login(loginData, responseHandler());
                return false;
            }
        });
        checkIntent();
    }


    private JsonHttpResponseHandler responseHandler() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                parseJsonResponse(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                Log.w("LoginActivity", "statusCode"+statusCode+" "+throwable.getMessage());
            }

        };
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if(intent.hasExtra("reset")) {
            removeToken();
        } else {
            checkRemember();
        }
    }

    private void save(String apiToken) {
        if (login_switch_remeber.isChecked()) {
            SharedPreferences.Editor e = preferences.edit();
            e.putBoolean("remember_token", login_switch_remeber.isChecked());
            e.putString("apiToken", apiToken);
            e.commit();
        } else {
            SharedPreferences.Editor e = preferences.edit();
            e.putBoolean("remember_token", false);
            e.commit();
        }
    }

    private void load() {
        String apiToken = preferences.getString("apiToken", "");
        if(!apiToken.equals("")) {
            Look2meetApi.getInstance().setApiToken(apiToken);
            startProfiles();
            Intent profilesIntent = new Intent(this, ProfilesActivity.class);
            startActivity(profilesIntent);
            finish();
        }
    }

    private void removeToken() {
        Look2meetApi.getInstance().setApiToken("");
        SharedPreferences.Editor e = preferences.edit();
        e.putString("apiToken", "");
        e.putString("apiTokenBackup", "");
        e.commit();
        checkRemember();
    }

    private void checkRemember() {
        boolean remember;
        remember = preferences.getBoolean("remember_token", false);
        if (remember) {
            login_switch_remeber.setChecked(true);
            load();
        }
    }

    private void parseJsonResponse(JSONObject jsonObject) {
    if(GlobalHelper.successStatusFromJson(jsonObject, false, this)) {
            JSONObject data = jsonObject.optJSONObject("data");
            String token = data.optString("apiToken");
            save(token);
            Look2meetApi.getInstance().setApiToken(token);
            startProfiles();
            preloader.done();
    } else {
        preloader.cancel(getString(R.string.error), jsonObject.optString("message", jsonObject.optString("message", getString(R.string.message_data_recieving_error))));
        }
    }

	public void onClickFillDefaults(View v) {
//		Log.wtf("onClickFillDefaults", "onClickFillDefaults");
//		login_email.setText("eugened.web@zuzex.com");
//		login_password.setText("123456");
	}

    public void startProfiles() {
        Intent profilesIntent = new Intent(getBaseContext(), ProfilesActivity.class);
        profilesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(profilesIntent);
        finish();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        super.startActivity(intent);
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

}




