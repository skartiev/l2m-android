package com.zuzex.look2meet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.api.apiData.AddUser;
import com.zuzex.look2meet.api.apiData.LoginRequest;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.http.Header;
import org.json.JSONObject;

public class RegistrationActivity extends MenuActivity {
    Button registration_enter_button;
    ToggleButton registration_sex_switch;
    Switch registration_agree_switch;
    EditText registration_email;
    EditText registration_name;
    EditText registration_password;
    EditText registration_confirm_password;
    AddUser loginUserData;
    Location location;
    SharedPreferences preferences;
    TextView iAgreeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        registration_enter_button = (Button) findViewById(R.id.registration_enter_button);
        registration_email = (EditText) findViewById(R.id.registration_email);
        registration_name = (EditText) findViewById(R.id.registration_name);
        registration_password = (EditText) findViewById(R.id.registration_password);
        registration_confirm_password = (EditText) findViewById(R.id.registration_confirm_password);
        registration_agree_switch = (Switch) findViewById(R.id.registration_agree_switch);
        registration_sex_switch = (ToggleButton) findViewById(R.id.toggleButton);
        iAgreeText = (TextView) findViewById(R.id.textViewAgree);
        iAgreeText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://look2meet.com/agreement.html"));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(browserIntent);
            }
        });
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        getActionBar().hide();
        loginUserData = new AddUser();
        registration_enter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEnteredData();
            }
        });

        registration_agree_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                registration_enter_button.setEnabled(b);
            }
        });
        location = Look2meet.locationService.getLastLocation();
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

    private void checkEnteredData() {

        String email = registration_email.getText().toString();
        String password = registration_password.getText().toString();
        String password_confirm = registration_confirm_password.getText().toString();
        String name = registration_name.getText().toString();

        if (!EmailValidator.getInstance().isValid(email)) {
            preloader.cancel(getResources().getString(R.string.message_wrong_data), getString(R.string.message_wrong_email));
            return;
        } else if (!password.equals(password_confirm)) {
            preloader.cancel(getString(R.string.message_wrong_data), getString(R.string.message_passwords_not_equals));
            return;
        } else if (password.isEmpty()) {
            preloader.cancel(getString(R.string.message_wrong_data), getString(R.string.message_empty_password));
            return;
        } else if (password.length() < 6) {
            preloader.cancel(getString(R.string.message_wrong_data), getString(R.string.message_password_short));
            return;
        } else if (name.isEmpty()) {
            preloader.cancel(getString(R.string.message_wrong_data), getString(R.string.message_name_empty));
            return;
        }
        loginUserData.setRole("user");
        loginUserData.setPassword(password);
        loginUserData.setEmail(email);
        loginUserData.setConfirm_password(password);
        loginUserData.setName(name);
        if(location != null)
        {
            loginUserData.setLat(location.getLatitude());
            loginUserData.setLon(location.getLongitude());
        }

        loginUserData.setSex(!registration_sex_switch.isChecked());
        preloader.launch();
        Look2meetApi.getInstance().registration(loginUserData.getParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                parseResponseJson(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                GlobalHelper.loggSend("NETWORK_ERROR");
                preloader.cancel(getResources().getString(R.string.message_network_error), getResources().getString(R.string.message_check_network));
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                GlobalHelper.loggSend("NETWORK_ERROR");
                preloader.cancel(getString(R.string.error), getString(R.string.message_network_error));
            }
        });
    }

    private void parseResponseJson(final JSONObject jsonObject) {
        final String message = jsonObject.optString("message");
        if(GlobalHelper.successStatusFromJson(jsonObject, false,this)) {
            LoginRequest loginData = new LoginRequest();
            loginData.setEmail(loginUserData.getEmail());
            loginData.setPassword(loginUserData.getPassword());
            loginData.setPhone(loginUserData.getPhone());
            Look2meetApi.getInstance().login(loginData, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    parseLoginResponse(response);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                }

                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    preloader.cancel(getString(R.string.error), getString(R.string.message_network_error));
                }
            });
        }
    }
    //preloader.cancel(getResources().getString(R.string.message_network_error), getResources().getString(R.string.message_check_network));
    //preloader.cancel(getResources().getString(R.string.message_error), jsonObject.get("message").toString());

    private void parseLoginResponse(JSONObject response) {
        if(GlobalHelper.successStatusFromJson(response, false, this)) {
            JSONObject data = null;
            data = response.optJSONObject("data");
            String apiToken = data.optString("apiToken");
            Look2meetApi.getInstance().setApiToken(apiToken);
            Intent profilesIntent = new Intent(getBaseContext(), ProfilesActivity.class);
            preloader.done();
            startActivity(profilesIntent);
        } else {
            GlobalHelper.loggSend("JSON_ERROR");
            preloader.cancel(getResources().getString(R.string.message_error), response.optString("message"));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        super.startActivity(intent);
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
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
