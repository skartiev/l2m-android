package com.zuzex.look2meet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.analytics.tracking.android.EasyTracker;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.api.GlobalHelper;


public class LookingForActivity extends MenuActivity {

    public static final String LOOKING_FOR_SEX = "LOOKING_FOR_SEX";
    public static final String START_AGE = "START_AGE";
    public static final String END_AGE = "END_AGE";

    public static final int DEFAULT_START_AGE = 18;
    public static final int DEFULT_END_AGE = 30;

    Button skipButton;
    Button thanksButton;
    Spinner age1Spinner;
    Spinner age2Spinner;
    Spinner sexSpinner;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.who_are_you_looking_for);
        age1Spinner = (Spinner) findViewById(R.id.spinner_age1);
        age2Spinner = (Spinner) findViewById(R.id.spinner_age2);
        sexSpinner = (Spinner) findViewById(R.id.spinner_sex);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int max = 130;
        int offset = 18;
        String[] numbers = new String[max-offset+1];
        for (int i = offset; i <= max; i++) {
            numbers[i - offset] = String.valueOf(i);
        }
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, numbers);
        age1Spinner.setAdapter(aa);
        age2Spinner.setAdapter(aa);

        final String currentProfileId = String.valueOf(UserProfile.getInstance().id);

        String genderName = preferences.getString(currentProfileId + LOOKING_FOR_SEX, "all");
        int minAgePos = aa.getPosition(String.valueOf(preferences.getInt(currentProfileId + START_AGE, DEFAULT_START_AGE)));
        int maxAgePos = aa.getPosition(String.valueOf(preferences.getInt(currentProfileId + END_AGE, DEFULT_END_AGE)));

        sexSpinner.setSelection(GlobalHelper.getGenderIndex(genderName));
        age1Spinner.setSelection(minAgePos);
        age2Spinner.setSelection(maxAgePos);

        skipButton = (Button) findViewById(R.id.btnskip);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMapActivity();
                finish();
            }
        });

        thanksButton = (Button) findViewById(R.id.thx);
        thanksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor ed = preferences.edit();
                ed.putBoolean("first_run_" + currentProfileId, false);
                String sex = GlobalHelper.getGender(sexSpinner.getSelectedItem().toString());
                ed.putString(currentProfileId+LOOKING_FOR_SEX, sex);
                Integer age1 = Integer.parseInt( (String) age1Spinner.getSelectedItem());
                Integer age2 = Integer.parseInt( (String) age2Spinner.getSelectedItem());
                Integer swapVar;
                if (age1 > age2) {
                    swapVar = age1;
                    age1 = age2;
                    age2 = swapVar;
                }

                ed.putInt(currentProfileId+START_AGE, age1);
                ed.putInt(currentProfileId+END_AGE, age2);
                ed.commit();
                loadMapActivity();
                finish();
            }
        });
    }

    public void BackClicked(View v) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
    public void ButtonClicked(View v) {
        Intent lookingIntent = new Intent(getBaseContext(), MyProfileActivity.class);
        startActivity(lookingIntent);
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
}
