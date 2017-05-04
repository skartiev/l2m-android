package com.zuzex.look2meet.OrgList;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.Organization;
import com.zuzex.look2meet.Look2meet;
import com.zuzex.look2meet.MapActivity;
import com.zuzex.look2meet.MenuActivity;
import com.zuzex.look2meet.OrganisationActivity;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.announces.AnnouncesFilter;
import com.zuzex.look2meet.announces.AnnouncesFilterActivity;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrgListActivity extends MenuActivity implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

    private static int REQUEST_SEARCH_ANNOUNCE = 1;
    private static int REQUEST_ORGANISATION_INFO = 2;

    private static int LIST_ITEMS_PER_PAGE = 20;

    private boolean isLoadingData;
    private boolean isNeedMore;
    private boolean isReloading;
    private int loadedPage;

    protected List<Organization> orgList;
    private OrgListAdapter adapter;
    private AnnouncesFilter announceFilter;
    private Boolean isFiltered;

    private ImageButton onMapButton;

    private double curLat;
    private double curLon;

    private ListView lvOrganizations;

    private String listType;

    private String lastSort = "";

    public String getListType() {
        return listType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        listType = intent.getStringExtra("list_type");
        LoadLayout(listType);
        getActionBar().hide();
        DisableButtons();
        onMapButton = (ImageButton) findViewById(R.id.organization_list_show_on_map_button);
        if(listType.equals("temp_object")) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) findViewById(R.id.search_org);
            searchView.setVisibility(View.VISIBLE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
            ImageButton onButton = (ImageButton) findViewById(R.id.organization_list_sort_button);
            onButton.setVisibility(View.INVISIBLE);
        }
        orgList = new ArrayList<Organization>();
        adapter = new OrgListAdapter(this, orgList);
        curLat = curLon = 0.0;
        loadedPage = 1;
        isNeedMore = false;
        isFiltered = false;
        isReloading = false;

        lvOrganizations = (ListView)findViewById(R.id.organization_list);
        lvOrganizations.setOnItemClickListener(this);
        lvOrganizations.setAdapter(adapter);
        lvOrganizations.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem == 0) {
                    isReloading = false;
                    return;
                }
                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0 && !isReloading) {
                    if(!isLoadingData && isNeedMore &&!isFiltered) {
                        isLoadingData = true;
                        loadedPage++;
                        loadData(loadedPage);
                    }
                }
            }
        });
        isLoadingData = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(1);
        YandexMetrica.onResumeActivity(this);
    }

    private void loadData(Integer page) {
        updateGeoCoordinates();
        preloader.launch();
        if(listType.equals("search_results")) {
            doSearch("name", page);
        } else if(listType.equals("top")) {
            loadTop();
        } else if(listType.equals("favorites")) {
            loadFavorites("", page);
        } else if(listType.equals("announces")) {
            loadAnnounces();
        } else if(listType.equals("checkins")) {
            loadCheckins(page);
        } else if(listType.equals("temp_object")) {
            LoadTempObjects();
        }
    }

    private void LoadLayout(String listType) {
        if(listType.equals("search_results")) {
            setContentView(R.layout.activity_org_list);
        } else if(listType.equals("top")) {
            setContentView(R.layout.activity_top);
        } else if(listType.equals("favorites")) {
            setContentView(R.layout.activity_favorites_);
        } else if(listType.equals("announces")) {
            setContentView(R.layout.activity_announces);
        } else if(listType.equals("checkins")) {
            setContentView(R.layout.activity_checkin);
        } else if(listType.equals("temp_object")) {
            setContentView(R.layout.activity_org_list);
        }
    }

    private void CancelActivity() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        onBackPressed();
    }

    public void BackClicked(View v) {
        onBackPressed();
    }

    public void showAllOnMapClicked(View v) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putParcelableArrayListExtra("organization_list", (ArrayList<Organization>) orgList);
        startActivity(intent);
        onBackPressed();
    }

    public void SearchAnnounceClicked(View v) {
        Intent intent = new Intent(this, AnnouncesFilterActivity.class);
        intent.putExtra("announce_filter", announceFilter);
        startActivityForResult(intent, REQUEST_SEARCH_ANNOUNCE);
    }

    public void SortClicked(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_filters, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                    loadedPage = 1;
                    isReloading = true;
                    switch (menuItem.getItemId()) {
                        case R.id.filter_alphavet:              startSearch("name"); break;
                        case R.id.filter_remote:                startSearch("distance"); break;
                        case R.id.filter_menCount:              startSearch("male_cnt"); break;
                        case R.id.filter_womenCount:            startSearch("female_cnt"); break;
                        case R.id.filter_menWomen:              startSearch("checkins"); break;
                        case R.id.filter_like:                  startSearch("likes_cnt"); break;
                        case R.id.filter_countHereMens:         startSearch("male_cnt_now"); break;
                        case R.id.filter_countHereWomens:       startSearch("womens_cnt_now"); break;
                        case R.id.filter_countHereMensWomens:   startSearch("checkins_now"); break;
                    }
                return true;
            }
        });
        popup.show();
    }

    public void favoritesSortClicked(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_filters, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.filter_alphavet:              loadFavorites("name", 1); break;
                        case R.id.filter_remote:                loadFavorites("distance", 1); break;
                        case R.id.filter_menCount:              loadFavorites("male_cnt", 1); break;
                        case R.id.filter_womenCount:            loadFavorites("female_cnt", 1); break;
                        case R.id.filter_menWomen:              loadFavorites("checkins", 1); break;
                        case R.id.filter_like:                  loadFavorites("likes_cnt", 1); break;
                        case R.id.filter_countHereMens:         loadFavorites("male_cnt_now", 1); break;
                        case R.id.filter_countHereWomens:       loadFavorites("womens_cnt_now", 1); break;
                        case R.id.filter_countHereMensWomens:   loadFavorites("checkins_now", 1); break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_SEARCH_ANNOUNCE) {
                announceFilter = data.getParcelableExtra("announce_filter");
                loadAnnounces();
            }
            if(requestCode == REQUEST_ORGANISATION_INFO) {
                if (data.hasExtra("organization_list")) {
                    ArrayList<Organization> organizations = data.getParcelableArrayListExtra("organization_list");
                    Intent intent = getIntent();
                    intent.putParcelableArrayListExtra("organization_list", organizations);
                    setResult(Activity.RESULT_OK, intent);
                    onBackPressed();
                }
            }
        }
    }

    private RequestParams GenerateFilterMap() {
        RequestParams filterMap = new RequestParams();
        Intent intent = getIntent();
        tryGetFilter("filter[country_id]", filterMap, intent);
        tryGetFilter("filter[city_id]", filterMap, intent);
        tryGetFilter("filter[address]", filterMap, intent);
        tryGetFilter("filter[name]", filterMap, intent);
        tryGetFilter("filter[category_id]", filterMap, intent);
        tryGetFilter("filter[favorite]", filterMap, intent);
        tryGetFilter("filter[neighbor]", filterMap, intent);
        return filterMap;
    }

    private void tryGetFilter(String name, RequestParams p, Intent intent) {
        if(intent.hasExtra(name))
            p.put(name, intent.getStringExtra(name));
    }

    private void doSearch(String sort, Integer page) {
        preloader.launch();
	    Look2meetApi.getInstance().searchObjects(page, LIST_ITEMS_PER_PAGE, GenerateFilterMap(), sort, getOrder(sort), false, curLat, curLon, responseHandler());
    }

    private void loadTop() {
	    Look2meetApi.getInstance().getTop(curLat, curLon, responseHandler());
    }

    private void loadFavorites(String sort, Integer page) {
	    Look2meetApi.getInstance().getFavoritesObject(page, LIST_ITEMS_PER_PAGE, sort, "", responseHandler());
    }

    private void loadAnnounces() {
	    Look2meetApi.getInstance().getAnnounces((announceFilter = new AnnouncesFilter(curLat,  curLon)).getData(), responseHandler());
    }

    private void loadCheckins(Integer page) {
        Look2meetApi.getInstance().getCheckinsList(page, LIST_ITEMS_PER_PAGE, true, responseHandler());
    }
    private void LoadTempObjects() {
        Look2meetApi.getInstance().getTempObjects(responseHandler());
    }

    private void updateGeoCoordinates() {
        Location loc = Look2meet.locationService.getLastLocation();
        if(loc != null) {
            curLat = loc.getLatitude();
            curLon = loc.getLongitude();
        }
    }

    private JsonHttpResponseHandler responseHandler() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                onResponse(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                onErrorResponse();
            }
        };
    }

    public void onResponse(JSONObject response) {
        try {
            if (GlobalHelper.successStatusFromJson(response, false, getApplicationContext())) {
                if (response.has("data")) {
                    JSONObject data = response.getJSONObject("data");
                    JSONArray objects = data.getJSONArray(listType.equals("checkins") ? "checkins" : "objects");
                    if (objects != null) {
                        if (loadedPage == 1) {
                            orgList.clear();
                        }
                        for (int i = 0; i < objects.length(); ++i) {
                            JSONObject obj = objects.getJSONObject(i);
                            Organization org = Organization.ParseFromJson(obj);
                            orgList.add(org);
                        }
                        if (orgList.size() > 0 && onMapButton != null) {
                            onMapButton.setVisibility(View.VISIBLE);
                        }
                        lvOrganizations.setTextFilterEnabled(true);
                        adapter.notifyDataSetChanged();
                        if(loadedPage == 1) {
                            lvOrganizations.setSelectionAfterHeaderView();
                        }

                        isNeedMore = objects.length() == LIST_ITEMS_PER_PAGE;
                        isLoadingData = false;
                        preloader.done();
                    }
                } else { // empty list
                    isLoadingData = false;
                    String message = getString(R.string.nothing_found);
                    if (listType.equals("search_results") ) {
                        message = getString(R.string.nothing_found);

                    } else if(listType.equals("favorites")) {
                        message = getString(R.string.favorites_empty);
                    }
                    preloader.cancel(getString(R.string.search), message, new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            CancelActivity();
                        }
                    });
                }
            } else {
                isLoadingData = false;
                String message = response.getString("message");
                GlobalHelper.loggSend(message);

//                    Log.d("ZZZZZZZZZZZZZZZ", "error: " + message);
                preloader.cancel(getString(R.string.error), message);
            }
        } catch (JSONException e) {
            isLoadingData = false;
            GlobalHelper.loggSend("JSON_EXCEPTION");
//                Log.d("ZZZZZZZZZZZZZZZ", "JSON exception: " + e.getMessage());
            preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
        }
    }

    public void onErrorResponse() {
        GlobalHelper.loggSend("ERROR,TRY AGAIN");
        isLoadingData = false;
        preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Organization org = adapter.filteredList.get(i);
        int id = org.id;
        Intent intent = new Intent(this, OrganisationActivity.class);
        intent.putExtra("mapObjectID", id);
        intent.putExtra("org_object", org);
        startActivityForResult(intent, REQUEST_ORGANISATION_INFO);
    }

    private void DisableButtons() {
        if(listType.equals("checkins")) {
            LinearLayout topLayout = (LinearLayout)findViewById(R.id.bottom_layout);
            LinearLayout announceButton = (LinearLayout)topLayout.findViewById(R.id.menu_button_checkin);
            announceButton.setEnabled(false);
        }
    }

    private void startSearch(String searchParamName) {
        lastSort = searchParamName;
        doSearch(searchParamName, 1);
        loadedPage = 1;
    }

    private String getOrder(String searchParamName) {
        if(lastSort.equals(searchParamName)) {
            return "desc";
        } else {
            return "asc";
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(s.isEmpty()) {
            isFiltered = false;
            lvOrganizations.clearTextFilter();
        } else {
            isFiltered = true;
            lvOrganizations.setFilterText(s);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
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
    public void onPause() {
        super.onPause();
        YandexMetrica.onPauseActivity(this);
    }
}