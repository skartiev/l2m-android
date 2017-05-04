package com.zuzex.look2meet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.analytics.tracking.android.EasyTracker;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.api.GlobalHelper;

public class FilterActivity extends Activity implements AdapterView.OnItemClickListener {
    String[] data;
    String[] checkins_list;
    String[] ages;
    Spinner spinner;
    Spinner spinnerAge;
    Spinner spinnerCheckin;
    Spinner spinnerAgeTo;
    String sex;
    int age;
    int ageTo;
    String checkin;
    String userId;
    private SharedPreferences preferences;
    private static int START_FILTER_AGE = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);
        getActionBar().hide();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        data = new String[]{getString(R.string.gender_type_all), getString(R.string.gender_type_male), getString(R.string.gender_type_female)};
        checkins_list = new String[]{getString(R.string.checkin_type_all), getString(R.string.checkin_type_now), getString(R.string.checkin_type_soon), getString(R.string.checkin_type_plan)};
        Intent intent = getIntent();
        sex = intent.getStringExtra("SEX");
        checkin = intent.getStringExtra("CHECKIN");
        age = intent.getIntExtra("AGE", 0);
        ageTo = intent.getIntExtra("AGE_TO", 0);

        int sexIndex = java.util.Arrays.asList(data).indexOf(GlobalHelper.getLocalizedGender(sex));
        if(age == 0 || ageTo == 0) {
            userId = String.valueOf(UserProfile.getInstance().id);
            age = preferences.getInt(userId+LookingForActivity.START_AGE, LookingForActivity.DEFAULT_START_AGE);
            ageTo = preferences.getInt(userId+LookingForActivity.END_AGE, LookingForActivity.DEFULT_END_AGE);
            sex = preferences.getString(userId+LookingForActivity.LOOKING_FOR_SEX, "all");
            sexIndex = GlobalHelper.getGenderIndex(sex);
        }

        int checkinIndex = java.util.Arrays.asList(checkins_list).indexOf(GlobalHelper.GetLocalizedCheckinType(checkin));

        ages = new String[130-START_FILTER_AGE+1];
        for (int i=START_FILTER_AGE; i<=130; i++) {
            ages[i-START_FILTER_AGE] = String.valueOf(i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (Spinner) findViewById(R.id.spinnerSex);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Title");
        spinner.setSelection(sexIndex);

        spinnerAge = (Spinner) findViewById(R.id.spinnerAgeFilter);
        ArrayAdapter<String> adapterAge = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ages);
        adapterAge.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(adapterAge);
        spinnerAge.setSelection(age - 18);

        spinnerCheckin = (Spinner) findViewById(R.id.spinnerCheckin);
        ArrayAdapter<String> adapterCheckin = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, checkins_list);
        adapterCheckin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCheckin.setAdapter(adapterCheckin);
        spinnerCheckin.setSelection(checkinIndex);

        spinnerAgeTo = (Spinner) findViewById(R.id.spinnerTo);
        ArrayAdapter<String> adapterAgeTo = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ages);
        adapterAgeTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeTo.setAdapter(adapterAgeTo);
        spinnerAgeTo.setSelection(ageTo - 18);
    }
        /*
        ListView listViewFilter = (ListView) findViewById(R.id.listViewFilter);
        myFilter = new ArrayList<HashMap<String,Object>>();      //создаем массив списков
        HashMap<String, Object> hm;                             //список объектов
        hm = new HashMap<String, Object>();
        hm.put(FIRSTKEY, "Пол");
        hm.put(SECONDKEY, "Все");
        myFilter.add(hm);
        hm = new HashMap<String, Object>();
        hm.put(FIRSTKEY, "Возраст");
        hm.put(SECONDKEY, "20");
        myFilter.add(hm);
        hm = new HashMap<String, Object>();
        hm.put(FIRSTKEY, "Чекин");
        hm.put(SECONDKEY, "Все");
        myFilter.add(hm);
        final String[] firstColumn = new String[] {
                "Пол", "Возраст","Чекин"
        };
        SimpleAdapter adapter = new SimpleAdapter(this,
                myFilter,
                R.layout.list_component, new String[]{
                FIRSTKEY,         //верхний текст
                SECONDKEY,        //нижний теккт
        }, new int[]{
                R.id.text1, //ссылка на объект отображающий текст
                R.id.text2, //ссылка на объект отображающий текст
               }); //добавили ссылку в чем отображать картинки из list.xml
        listViewFilter.setAdapter(adapter);
        listViewFilter.setOnItemClickListener(this);
        */

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    /*@Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i)
        {
            case 0:
            {
                /*DialogFragment newFragment = new PickerFragment();
                newFragment.show(getFragmentManager(), "timePicker")
                Intent RegisterIntent = new Intent(getBaseContext(), PickerActivity.class);
                RegisterIntent.putExtra("EXTRA_NUMBER",0);
                startActivity(RegisterIntent);

            }
            break;
            case 1:
            {
                Intent RegisterIntent = new Intent(getBaseContext(), PickerActivity.class);
                RegisterIntent.putExtra("EXTRA_NUMBER",1);
                startActivity(RegisterIntent);
            }
            break;
            case 2:
            {
                Intent RegisterIntent = new Intent(getBaseContext(), PickerActivity.class);
                RegisterIntent.putExtra("EXTRA_NUMBER",2);
                startActivity(RegisterIntent);
            }
            break;
        }
    }
    public static class PickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));



        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
        */
    public void filterClick(View v)
    {
        Intent filterIntent = new Intent();
        sex = spinner.getSelectedItem().toString();
        String age = spinnerAge.getSelectedItem().toString();
        checkin = spinnerCheckin.getSelectedItem().toString();
        String ageTo = spinnerAgeTo.getSelectedItem().toString();
        filterIntent.putExtra("SEX",sex);
        filterIntent.putExtra("AGE",age);
        filterIntent.putExtra("AGE_TO", ageTo);
        filterIntent.putExtra("CHECKIN", checkin);
        //filterIntent.putExtra("STATUS","1");
        setResult(RESULT_OK,filterIntent);
        finish();
    }

    public void BackClicked(View v)
    {
        Intent filterIntent = new Intent();
        setResult(RESULT_CANCELED,filterIntent);
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
}


