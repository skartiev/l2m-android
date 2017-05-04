package com.zuzex.look2meet.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.Organization;
import com.zuzex.look2meet.MenuActivity;
import com.zuzex.look2meet.OrgList.OrgListActivity;
import com.zuzex.look2meet.R;

import java.util.ArrayList;

public class SearchParamsActivity extends MenuActivity implements AdapterView.OnItemClickListener {

    private ArrayList<SearchParameter> searchParams;
    private ListView lvParams;
    private SearchParamsAdapter searchParamsAdapter;
    private String curCountryId;
    private String curAdress;
    private String curName;

    private ArrayList<SearchParamValue> savedCountries;
    private ArrayList<SearchParamValue> savedCities;
    private ArrayList<SearchParamValue> savedCategories;


    private SharedPreferences preferences;

    public static int REQUEST_SHOW_INFO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_params);

        getActionBar().hide();

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        savedCountries = null;
        savedCities = null;
        savedCategories = null;
        curAdress = "";
        curName = "";

        searchParams = new ArrayList<SearchParameter>();
        searchParams.add(new SearchParameter(getString(R.string.search_param_country), new SearchParamValue(getString(R.string.search_param_country_russia), "1"), 0));
        searchParams.add(new SearchParameter(getString(R.string.search_param_city), new SearchParamValue(getString(R.string.search_param_type_all), ""), 1));
        searchParams.add(new SearchParameter(getString(R.string.search_param_address), new SearchParamValue("", ""), 2));
        searchParams.add(new SearchParameter(getString(R.string.search_param_name), new SearchParamValue("", ""), 3));
        searchParams.add(new SearchParameter(getString(R.string.search_param_type), new SearchParamValue(getString(R.string.search_param_type_all), ""), 4));
        searchParams.add(new SearchParameter(getString(R.string.search_param_favorite), new SearchParamValue(getString(R.string.search_param_no), ""), 5));
        searchParams.add(new SearchParameter(getString(R.string.search_param_near_me), new SearchParamValue(getString(R.string.search_param_no), ""), 6));

        curCountryId = "";  // No country selected

        lvParams = (ListView)findViewById(R.id.search_params_list);
        searchParamsAdapter = new SearchParamsAdapter(this, searchParams);
        lvParams.setAdapter(searchParamsAdapter);
        lvParams.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, SearchParamSelectActivity.class);
        intent.putExtra("param", searchParams.get(position));

        switch (position) {
            case 0: { // country
                if(savedCountries != null) {
                    intent.putParcelableArrayListExtra("countries", savedCountries);
                }
                break;
            }
            case 1: { // city
                intent.putExtra("country_id", curCountryId);
                if(savedCities != null) {
                    intent.putParcelableArrayListExtra("cities", savedCities);
                }
                break;
            }
            case 2:
                AlertDialog.Builder adressDialog = new AlertDialog.Builder(this);
                adressDialog.setTitle(R.string.address);
                final EditText input = new EditText(this);
                input.setText(curAdress);
                adressDialog.setView(input);
                adressDialog.setPositiveButton(this.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String enteredText = input.getText().toString();
                        curAdress = enteredText;
                        searchParams.get(2).value = new SearchParamValue(curAdress, "");
                    }
                });
                adressDialog.setNegativeButton(this.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                adressDialog.show();
                return;
            case 3:
                AlertDialog.Builder nameDialog = new AlertDialog.Builder(this);
                nameDialog.setTitle(R.string.org_name);
                final EditText editText = new EditText(this);
                editText.setText(curName);
                nameDialog.setView(editText);
                nameDialog.setPositiveButton(this.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String enteredText = editText.getText().toString();
                        curName = enteredText;
                        searchParams.get(3).value = new SearchParamValue(curName, "");
                    }
                });
                nameDialog.setNegativeButton(this.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                nameDialog.show();
                return;
            case 4: {
                if(savedCategories != null) {
                    intent.putParcelableArrayListExtra("categories", savedCategories);
                }
                break;
            }
            case 5:
            case 6: {
                SearchParamValue param = searchParams.get(position).value;
                if(param.value.equals(getString(R.string.search_param_yes))) {
                    param.value = getString(R.string.search_param_no);
                } else {
                    param.value = getString(R.string.search_param_yes);
                }
                searchParamsAdapter.notifyDataSetChanged();
                return;
            }
        }
        startActivityForResult(intent, position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if (data.hasExtra("organization_list")) {
                ArrayList<Organization> organizations = data.getParcelableArrayListExtra("organization_list");
                Intent intent = getIntent();
                intent.putParcelableArrayListExtra("organization_list", organizations);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                SearchParamValue val = data.getParcelableExtra("value");
                if (requestCode == REQUEST_SHOW_INFO) {
                    curCountryId = val.id;
                }
                searchParams.get(requestCode).value = val;
                searchParamsAdapter.notifyDataSetChanged();
            }

            if (data.hasExtra("cities")) {
                savedCities = data.getParcelableArrayListExtra("cities");
            }
            if (data.hasExtra("countries")) {
                savedCountries = data.getParcelableArrayListExtra("countries");
            }
            if (data.hasExtra("categories")) {
                savedCategories = data.getParcelableArrayListExtra("categories");
            }
        }
    }

    public void BackClicked(View v) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void SearchClicked(View v) {
        savedCities = null;
        savedCountries = null;
        Intent intent = new Intent(this, OrgListActivity.class);
        intent.putExtra("list_type", "search_results");
        StoreFilterParams(intent);
        startActivityForResult(intent, REQUEST_SHOW_INFO);
    }

    private void StoreFilterParams(Intent intent) {
        intent.putExtra("filter[country_id]", searchParams.get(0).value.value);

        SearchParamValue city = searchParams.get(1).value;
        if(!city.id.isEmpty()) {
            intent.putExtra("filter[city_id]", city.value);
        }

        if(!curAdress.isEmpty()) {
            intent.putExtra("filter[address]", curAdress);
        }

        if(!curName.isEmpty()) {
            intent.putExtra("filter[name]", curName);
        }

        SearchParamValue category = searchParams.get(4).value;
        if(!category.id.isEmpty()) {
            intent.putExtra("filter[category_id]", category.id);
        }
        intent.putExtra("filter[favorite]", searchParams.get(5).name.equals(getString(R.string.search_param_yes)) ? "1" : "0");
        intent.putExtra("filter[neighbor]", searchParams.get(6).name.equals(getString(R.string.search_param_yes)) ? "5" : "0");
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
