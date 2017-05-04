package com.zuzex.look2meet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.UserProfile;

public class SettingsActivity extends MenuActivity implements AdapterView.OnItemClickListener {
    ListView listViewSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        String[] values = new String[]{"Черный список", "Белый список", "Маска"};
        String[] valuesIsNotVip = new String[] {"Черный список"};
        listViewSettings = (ListView) findViewById(R.id.listViewSettings);
        ArrayAdapter<String> adapter;
        if(UserProfile.getInstance().isVip) {
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, values);
        }
        else {
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, valuesIsNotVip);
        }
        listViewSettings.setAdapter(adapter);
        listViewSettings.setOnItemClickListener(this);
        getActionBar().hide();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0: {
                Intent blackListIntent = new Intent(getBaseContext(), UsersList.class);
                blackListIntent.putExtra("curr_list","BlackList");
                startActivity(blackListIntent);
                break;
            }
            case 1: {
                Intent whiteListIntent = new Intent(getBaseContext(), UsersList.class);
                whiteListIntent.putExtra("curr_list","WhiteList");
                startActivity(whiteListIntent);
                break;
            }
            case 2: {
                Intent MaskListIntent = new Intent(getBaseContext(), UsersList.class);
                MaskListIntent.putExtra("curr_list","MaskList");
                startActivity(MaskListIntent);
                break;
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
    }

    @Override
    public void onPause() {
        super.onPause();
        YandexMetrica.onPauseActivity(this);
    }
}
