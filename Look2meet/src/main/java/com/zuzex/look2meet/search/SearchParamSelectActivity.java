package com.zuzex.look2meet.search;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.utils.AnimationPreloader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class SearchParamSelectActivity extends Activity implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener, View.OnClickListener {

    private SharedPreferences preferences;
    private ArrayList<SearchParamValue> items;
    private ArrayAdapter<SearchParamValue> itemsAdapter;
    private int curParamId;

    private TextView tvCaption;
    private ListView lvItems;
    private SearchView svFilter;
    private AnimationPreloader preloader;
    private boolean needUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_param_select);
        getActionBar().hide();

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        items = new ArrayList<SearchParamValue>();

        tvCaption = (TextView)findViewById(R.id.search_param_selector_caption);
        lvItems = (ListView)findViewById(R.id.search_param_selector_list);
        svFilter = (SearchView)findViewById(R.id.search_param_selector_filter);

        svFilter.setOnQueryTextListener(this);

        Intent intent = getIntent();
        SearchParameter param = intent.getParcelableExtra("param");
        curParamId = param.id;
        tvCaption.setText(param.name);

        needUpdate = true;
        if(param.id == 0 && intent.hasExtra("countries")) {
            items = intent.getParcelableArrayListExtra("countries");
            needUpdate = false;
        } else if(param.id == 2) {
            svFilter.setOnQueryTextListener(AdressQueryTextListener);
        }
          else if(param.id == 3) {
            svFilter.setOnQueryTextListener(AdressQueryTextListener);
        } else if(param.id == 1 && intent.hasExtra("cities")) {
            items = intent.getParcelableArrayListExtra("cities");
            needUpdate = false;
        } else if(param.id == 4 && intent.hasExtra("categories")) {
            items = intent.getParcelableArrayListExtra("categories");
            needUpdate = false;
        }

        itemsAdapter = new ArrayAdapter<SearchParamValue>(this, android.R.layout.simple_list_item_1, android.R.id.text1, items);

        lvItems.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvItems.setOnItemClickListener(this);
        lvItems.setAdapter(itemsAdapter);

        if(needUpdate) {
            UpdateData("");
        }
    }

    private static Comparator<SearchParamValue> ALPHABETICAL_ORDER = new Comparator<SearchParamValue>() {
        @Override
        public int compare(SearchParamValue searchParamValue, SearchParamValue searchParamValue2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(searchParamValue.value, searchParamValue2.value);
            if (res == 0) {
                res = searchParamValue.value.compareTo(searchParamValue2.value);
            }
            return res;
        }
    };

    public void BackClicked(View v) {
        cancelActivity();
    }

    public void SetParamClicked(View v) {
        String queryString = svFilter.getQuery().toString();

        if(queryString.isEmpty()) {
            cancelActivity();
        } else {
            SearchParamValue item = new SearchParamValue(queryString, "");
            finishActivityAndSendParam(item);
        }
    }

    private ProgressDialog.OnCancelListener onCancelListener() {
        return new ProgressDialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
	           //cancel
            }
        };
    }

    // AdapterView.OnItemClickListener implementation

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        SearchParamValue item = (SearchParamValue)lvItems.getItemAtPosition(position);
        finishActivityAndSendParam(item);
    }

    // SearchView.OnQueryTextListener implementation

    public boolean onQueryTextChange(String newText) {
        itemsAdapter.getFilter().filter(newText);
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    // Loading data

    private void LoadCountries(String filter) {
        Look2meetApi.getInstance().searchCountries(filter, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                parseItems(response, "countries");
            }
        });
    }

    private void loadCities(String countryId, String filter) {
        Look2meetApi.getInstance().searchCities(countryId, filter, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                parseItems(response, "cities");
            }
        });
    }


    private void loadCategories() {
        Look2meetApi.getInstance().getCategoriesList(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                parseItems(response, "categories");
            }
        });
    }

    private void parseItems(JSONObject response, String itemsKey) {
        try {
            if(GlobalHelper.successStatusFromJson(response)) {
                JSONObject data = response.getJSONObject("data");
                if(itemsKey.equals("categories")) {
                    JSONArray a = data.optJSONArray(itemsKey);
                    for(int i = 0; i < a.length(); ++i) {
                        JSONObject item = a.getJSONObject(i);
                        String key = String.valueOf(item.optInt("id"));
                        String value = item.optString("title");
                        SearchParamValue param = new SearchParamValue(value, key);
                        items.add(param);
                    }
                    items.add(new SearchParamValue(" "+getString(R.string.search_param_type_all), ""));

                } else {
                    JSONObject countries = data.getJSONObject(itemsKey);
                    Iterator<String> keys = countries.keys();
                    ClearItems();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = countries.getString(key);
                        SearchParamValue param = new SearchParamValue(value, key);
                        items.add(param);
                    }
                }
                Collections.sort(items, ALPHABETICAL_ORDER);
                itemsAdapter.notifyDataSetChanged();
                preloader.done();
            } else {
                String message = response.getString("message");
                preloader.cancel(getString(R.string.error), message);
            }

        } catch (JSONException e) {
            preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
        }
    }



    private void UpdateData(String filter) {
        switch (curParamId) {
            case 0:
                preloader = new AnimationPreloader(this, onCancelListener());
                preloader.launch();
                LoadCountries(filter);
                break;
            case 1:
                preloader = new AnimationPreloader(this, onCancelListener());
                preloader.launch();
                loadCities(getIntent().getStringExtra("country_id"), filter);
                break;
            case 4:
                preloader = new AnimationPreloader(this, onCancelListener());
                preloader.launch();
                loadCategories();
                break;
        }
    }

    private void ClearItems() {
        items.clear();
        items.add(new SearchParamValue(getString(R.string.search_param_type_all), ""));
    }

    private void finishActivityAndSendParam(SearchParamValue value) {
        Intent intent = new Intent();
        intent.putExtra("value", value);
        StoreItems(intent);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void cancelActivity() {
        Intent intent = new Intent();
        StoreItems(intent);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void StoreItems(Intent intent) {
        Integer paramId = ((SearchParameter)getIntent().getParcelableExtra("param")).id;
        if(items.size() > 0) {
            if (paramId == 0 && needUpdate) {
                intent.putParcelableArrayListExtra("countries", items);
            } else if (paramId == 1 && needUpdate) {
                intent.putParcelableArrayListExtra("cities", items);
            } else if (paramId == 4 && needUpdate) {
                intent.putParcelableArrayListExtra("categories", items);
            }
        }
    }

    public void activeSearch(View view) {
        svFilter.onActionViewExpanded();
    }

    @Override
    public void onClick(View view) {
        if(curParamId == 2) {
            String queryString = svFilter.getQuery().toString();
            if(queryString.isEmpty()) {
                cancelActivity();
            } else {
                SearchParamValue item = new SearchParamValue(queryString, "");
                finishActivityAndSendParam(item);
            }
        }
    }


    final SearchView.OnQueryTextListener AdressQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            // Do something
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            // Do something
            if(curParamId == 2 || curParamId == 3) {
                if(query.isEmpty()) {
                    cancelActivity();
                } else {
                    SearchParamValue item = new SearchParamValue(query, "");
                    finishActivityAndSendParam(item);
                }
            }
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelActivity();
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
