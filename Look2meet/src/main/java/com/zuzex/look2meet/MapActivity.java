package com.zuzex.look2meet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Layout;
import android.transition.Visibility;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.Organization;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.announces.AnnouncePopup;
import com.zuzex.look2meet.announces.AnnouncesFilter;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.maps.DrawableMarker;
import com.zuzex.look2meet.maps.IMapObjectLoader;
import com.zuzex.look2meet.maps.LocationService;
import com.zuzex.look2meet.maps.MapObjectLoader;
import com.zuzex.look2meet.maps.MapObjectModel;

import org.apache.commons.lang.ObjectUtils;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;


public class MapActivity extends MenuActivity
        implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener, PopupMenu.OnMenuItemClickListener, GoogleMap.OnMapLoadedCallback, Action1, IMapObjectLoader {

    private final static String TAG = "MapActivity";
    private static final int MIN_ZOOM_LEVEL_TO_LOAD = 15;
    private FragmentManager fragmentManager;
    private GoogleMap mMap = null;
    private Marker myPositionMarker;
    private Marker selectedMarker;
    private ImageView map_popup_cursor;
    private View popupView;
    private PopupWindow popupWindow;
    private View map_view;
    private MapObjectLoader mapObjectLoader;
    private ArrayList<MapObjectModel> mapObjectsList;
    private ArrayList<Organization> announces;
    private Integer curAnnounceIndex = 0;
    private AnnouncePopup announcePopup;
    private Timer announceTimer;
    ArrayList<Integer> loadedObjectsID;
    Location location;
    private View topLayout;
    private View topLayoutFilter;
    private LinearLayout mainLayout;
    private Subscription subscription;
    private float zoom;
    Location loc;
    boolean isNeedLoad = false;
    LayoutInflater layoutInflater;
    int launch;
    protected ArrayList<Organization> orgList;
    private boolean isSorted;


    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        isSorted = false;
        map_view = this.getWindow().getDecorView();
        layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.map_object_info, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        map_popup_cursor = (ImageView) popupView.findViewById(R.id.map_popup_cursor);
        setUpMapIfNeeded();
        mapObjectLoader = new MapObjectLoader(this);
        mapObjectsList = new ArrayList<MapObjectModel>();
        loadedObjectsID = new ArrayList<Integer>();
        announces = new ArrayList<Organization>();
        topLayout = (View) findViewById(R.id.top_layout_map);
        mainLayout = (LinearLayout) findViewById(R.id.linearMap);
        topLayoutFilter = (View) findViewById(R.id.top_menu_filter);
        topLayoutFilter.setVisibility(View.GONE);
        orgList = new ArrayList<Organization>();


        launch = 1;
        zoom = 0;
        loc = new Location("");
        loc.setLatitude(0);
        loc.setLongitude(0);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(announceReceiver, new IntentFilter("sendObjectAnons"));
        announcePopup = new AnnouncePopup(this);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(Look2meet.checkGps && LocationService.gpsAvailable && !lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.map_gps_off_header));
            builder.setMessage(R.string.map_gps_off_warning);
            builder.setPositiveButton(getString(R.string.global_positive), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(getString(R.string.global_remind_later), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Look2meet.checkGps = false;
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
        moveCameraToMyPosition(location);
        fragmentManager = getFragmentManager();

    }

    private void checkIntent() {
        isNeedLoad = false;
        Intent intent = getIntent();
        if (intent.hasExtra("organization_list") && launch==1) {
            topLayout.setVisibility(View.GONE);
            topLayoutFilter.setVisibility(View.VISIBLE);
            /*topLayoutFilter = layoutInflater.inflate(R.layout.back_and_filter_button, null);

            mainLayout.addView(topLayoutFilter,mainLayout.indexOfChild(topLayout));
            mainLayout.removeView(topLayout);*/
            ArrayList<Organization> organizations;
            if(!isSorted)
            {
                organizations= intent.getParcelableArrayListExtra("organization_list");
            }
            else
            {
               organizations = orgList;
            }
            //ArrayList<Organization> organizations= intent.getParcelableArrayListExtra("organization_list");
            if(organizations.size() > 1) {
                loadMapObjectsFromOrganization(organizations);
            } else if(organizations.size() == 1) {
                checkIdinIntent(organizations.get(0).id);
            }
            launch = 2;
        } else if(intent.hasExtra("object_id")) {
            int id = intent.getIntExtra("object_id", -1);
            checkIdinIntent(id);
        } else {
            if(location != null) {
                shoMyPositionMarker(location);
                moveCameraToMyPosition(location);
            }
            isNeedLoad = true;
            preloader.done();
        }
    }
    public void FilterClicked(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_filters, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                //loadedPage = 1;
                //isReloading = true;
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
    public void startSearch(String str)
    {
        doSearch(str, 1);
        isSorted = true;
        launch = 1;
        //Look2meetApi.getInstance().searchObjects(page, LIST_ITEMS_PER_PAGE, GenerateFilterMap(), sort, getOrder(sort), false, curLat, curLon, responseHandler());
    }
    private void doSearch(String sort, Integer page) {
        preloader.launch();
        Look2meetApi.getInstance().searchObjects(page, 15, GenerateFilterMap(), sort, "asc", false, loc.getLatitude(), loc.getLongitude(), responseHandler());
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

    private JsonHttpResponseHandler responseHandler() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                onResponse(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //onErrorResponse();
            }
        };
    }
    public void onResponse(JSONObject response) {
        try {
            if (GlobalHelper.successStatusFromJson(response, false, getApplicationContext())) {
                if (response.has("data")) {
                    JSONObject data = response.getJSONObject("data");
                    JSONArray objects = data.getJSONArray("objects");
                    if (objects != null) {
                        orgList.clear();
                        for (int i = 0; i < objects.length(); ++i) {
                            JSONObject obj = objects.getJSONObject(i);
                            Organization org = Organization.ParseFromJson(obj);
                            orgList.add(org);
                        }

                        preloader.done();
                        checkIntent();
                    }
                } else { // empty list
                    String message = getString(R.string.nothing_found);

                        message = getString(R.string.nothing_found);


                    preloader.cancel(getString(R.string.search), message, new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {

                        }
                    });
                }
            } else {
                String message = response.getString("message");
                GlobalHelper.loggSend(message);
                preloader.cancel(getString(R.string.error), message);
            }
        } catch (JSONException e) {
            GlobalHelper.loggSend("JSON_EXCEPTION");
            preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
        }
    }

    private void checkIdinIntent(int objectId) {
        if(objectId != -1) {
            if(loadedObjectsID.contains(objectId)) {
                mMap.clear();
                for(int i = 0; i < mapObjectsList.size(); i++) {
                    MapObjectModel m = mapObjectsList.get(i);
                    int id = m.organization.id;
                    if(objectId == id) {
                        mMap.addMarker(mapObjectsList.get(i).getMarkerOptions());
                        mapObjectsList.clear();
                        mapObjectsList.add(m);
                        moveCameraToMyPosition(m.getLocation());
                    }
                }
            } else {
                preloader.launch();
                mapObjectLoader.loadFromId(objectId, new IMapObjectLoader() {
                    @Override
                    public void objectLoaded(MapObjectModel mapObject) {
                        loadedObjectsID.clear();
                        mapObjectsList.clear();
                        mMap.clear();
                        loadObject(mapObject);
                        preloader.done();
                        focusOnLastObject();
                    }
                    @Override
                    public void objectsLoaded(List<MapObjectModel> mapObject) {

                    }
                });
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setUpMapIfNeeded();
        setIntent(intent);
        checkIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        announcePopup.Hide();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(announceReceiver);
    }

    private void loadMapObjectsFromOrganization(final ArrayList<Organization> organizations) {
        loadedObjectsID.clear();
        mapObjectsList.clear();
        mMap.clear();
        shoMyPositionMarker(location);
        for (Organization organization : organizations) {
            MapObjectModel mapObject = new MapObjectModel(organization);
            MapObjectModel prev = null;
            if(mapObjectsList.size() > 0) {
                prev = mapObjectsList.get(mapObjectsList.size() - 1);
            }
            if (prev != null) {
                prev.neighborNext = mapObject.organization.id;
                mapObject.neighborsPrev = prev.organization.id;
            }
            if (mapObjectsList.size() == organizations.size() - 1) {
                mapObject.neighborNext = mapObjectsList.get(0).organization.id;
                mapObjectsList.get(0).neighborsPrev = mapObject.organization.id;
            }
            loadObject(mapObject);
            if(mapObjectsList.size() != 0 && mapObjectsList.size() == organizations.size()) {
                preloader.done();
                focusOnFirstObject();
            }
        }
    }
    private void focusOnFirstObject() {
        int size = mapObjectsList.size();
        if(size > 0) {
            MapObjectModel lastObject = mapObjectsList.get(0);
            moveCameraToMyPosition(lastObject.getLocation());
            showMarkerInfoWindow(lastObject);
        }
    }
    private void focusOnLastObject() {
        int size = mapObjectsList.size();
        if(size > 0) {
            MapObjectModel lastObject = mapObjectsList.get(size -1);
            moveCameraToMyPosition(lastObject.getLocation());
            showMarkerInfoWindow(lastObject);
        }
    }

    private void loadObject(MapObjectModel mapObject) {
        MarkerOptions opt = new MarkerOptions();
        opt.position(mapObject.getLocation());
        opt.icon(getIcon(mapObject));
        mapObject.setMarkerOptions(opt);
        Marker m = mMap.addMarker(opt);
        mapObject.setMarker(m);
        mapObjectsList.add(mapObject);
    }

    @Override
    public void onStart() {
        super.onStart();
        Look2meet.locationService.start();
        subscription = Look2meet.locationService.subscribeToLocation().subscribe(this);
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(announceTimer != null) {
            announceTimer.cancel();
            announceTimer.purge();
        }
        subscription.unsubscribe();
        Look2meet.locationService.stop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_main: {
                return true;
            }
        }
        return super.onMenuItemClick(menuItem);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        // TODO: refresh UserProfile when checkin
        if(UserProfile.getInstance().checkinsCount == 0) {
            loadAnnounces();
        }
        YandexMetrica.onResumeActivity(this);
    }



    @Override
    public void onPause() {
        super.onPause();
        if(popupWindow != null) {
            popupWindow.dismiss();
        }
        YandexMetrica.onPauseActivity(this);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_map)).getMap();
        }
        mMap.setInfoWindowAdapter(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapLoadedCallback(this);
    }

    public void showMyLocation(View view) throws InvocationTargetException, IllegalAccessException {
        if(location != null ) {
            shoMyPositionMarker(location);
            moveCameraToMyPosition(location);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onDisconnected() {
        showToast("Internet connection error");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        showToast("Internet connection error");
      }

    private void shoMyPositionMarker(Location location) {
        if (myPositionMarker != null) {
            myPositionMarker.remove();
        }
            if(location != null) {
                LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                myPositionMarker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.l2m2)));
            }
        }

    void moveCameraToMyPosition(Location location) {
        if(location != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            moveCameraToMyPosition(position);
        }
    }

    void moveCameraToMyPosition(LatLng position) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)      // Sets the center of the map to my position
                .zoom(MIN_ZOOM_LEVEL_TO_LOAD)                   // Sets the zoom
//              .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    void movePositionToCenterMap(LatLng position) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .zoom(mMap.getCameraPosition().zoom)
                .target(position)      // Sets the center of the map to my position
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void showToast(CharSequence text) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void showMarkerInfoWindow(final MapObjectModel model) {
        preloader.done();
        selectedMarker = model.getMarker();
        final Organization organization = model.organization;
        TextView title = (TextView) popupView.findViewById(R.id.map_object_name);
        title.setText(organization.name);
        TextView work_time = (TextView) popupView.findViewById(R.id.map_object_work_time);
        work_time.setText(organization.workTime);
        ImageView avatar = (ImageView) popupView.findViewById(R.id.map_object_image);
        ImageLoader.getInstance().displayImage(organization.avatarUrl, avatar);
        TextView femaleCountNow = (TextView) popupView.findViewById(R.id.map_object_female_cnt_now);
        femaleCountNow.setText(String.valueOf(organization.femaleCountAll));

        TextView maleCountNow = (TextView) popupView.findViewById(R.id.map_object_male_cnt_now);
        maleCountNow.setText(String.valueOf(organization.maleCountAll));

        TextView checkinsNow = (TextView) popupView.findViewById(R.id.map_object_users_count);
        checkinsNow.setText(String.valueOf(organization.maleCountAll + organization.femaleCountAll));

        ImageView isTop = (ImageView) popupView.findViewById(R.id.map_object_top);

        if(organization.isTop) {
            isTop.setVisibility(View.VISIBLE);
        } else {
            isTop.setVisibility(View.GONE);
        }

        LinearLayout nextObject = (LinearLayout) popupView.findViewById(R.id.map_info_forward);
        LinearLayout prevObject = (LinearLayout) popupView.findViewById(R.id.map_info_back);
        if(model.isMultiObject() || (model.neighborNext != -1 && model.neighborsPrev != -1)) {
            nextObject.setVisibility(View.VISIBLE);
            nextObject.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int goIndex = -1;
                    if (model.neighborNext != -1) {
                        goIndex = model.neighborNext;
                    } else if (model.neighborsPrev != 1) {
                        goIndex = model.neighborsPrev;
                    }
                    MapObjectModel m;
                    m = find(goIndex);
                    if (m != null) {
                        showMarkerInfoWindow(m);
                        movePositionToCenterMap(m.getLocation());
                    }
                }
            });
            prevObject.setVisibility(View.VISIBLE);
            prevObject.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int goIndex = -1;
                    if(model.neighborsPrev != -1) {
                        goIndex = model.neighborsPrev;
                    } else if (model.neighborNext != 1) {
                        goIndex = model.neighborNext;
                    }
                    MapObjectModel m;
                    m = find(goIndex);
                    if(m != null) {
                        showMarkerInfoWindow(m);
                        movePositionToCenterMap(m.getLocation());
                    }
                }
            });
        }
        else {
            nextObject.setVisibility(View.INVISIBLE);
            prevObject.setVisibility(View.INVISIBLE);
        }
        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OrganisationActivity.class);
                intent.putExtra("mapObjectID", organization.id);
                startActivityForResult(intent, 0);
            }
        });

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight()/2;
        popupWindow.showAtLocation(map_view, Gravity.CENTER, 0, -(popupHeight+(popupHeight/4)));
        map_popup_cursor.setVisibility(View.VISIBLE);
    }

     MapObjectModel find(int id) {
         for(MapObjectModel model: mapObjectsList) {
             if(model.organization.id == id) {
                 return model;
             }
         }
         return null;
     }

    @Override
    public void onMapClick(LatLng latLng) {
        isNeedLoad = true;
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        selectedMarker = marker;
        if (myPositionMarker != null && marker.getId().equals(myPositionMarker.getId())) {
            moveCameraToMyPosition(marker.getPosition());
        } else {
            for (MapObjectModel mapObject : mapObjectsList) {
                if (marker.getId().equals(mapObject.getMarker().getId()))
                    showMarkerInfoWindow(mapObject);
                movePositionToCenterMap(marker.getPosition());
            }
        }
        return true;
    }
    private double max4d1(double d1,double d2,double d3,double d4)
    {
        return Math.max(Math.max(d1,d2),Math.max(d3,d4));
    }
    private double min4d1(double d1,double d2,double d3, double d4)
    {
        return Math.min(Math.min(d1, d2), Math.min(d3, d4));
    }
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(location !=null && isNeedLoad) {
            VisibleRegion region = mMap.getProjection().getVisibleRegion();
            double maxLat = max4d1(region.farLeft.latitude,
                                    region.farRight.latitude,
                                    region.nearLeft.latitude,
                                    region.nearRight.latitude);
            double minLat = min4d1(region.farLeft.latitude,
                                   region.farRight.latitude,
                                    region.nearLeft.latitude,
                                    region.nearRight.latitude);
            double maxLon = max4d1(region.farLeft.longitude,
                                    region.farRight.longitude,
                                    region.nearLeft.longitude,
                                    region.nearRight.longitude);
            double minLon = min4d1(region.farLeft.longitude,
                                   region.farRight.longitude,
                                    region.nearLeft.longitude,
                                    region.nearRight.longitude);
            double latDelta = maxLat - minLat;
            double lonDelta = maxLon - minLon;
            double newCenterLat = minLat + latDelta/2;
            double newCenterLon = minLon + lonDelta/2;
            if(Math.abs(loc.getLatitude() - newCenterLat) > 2*latDelta || Math.abs(loc.getLongitude() - newCenterLon) >2*lonDelta ||
                    Math.abs(cameraPosition.zoom - zoom)>0.5)
            {

                loc.setLatitude(newCenterLat);
                loc.setLongitude(newCenterLon);
                zoom = cameraPosition.zoom;

                mapObjectLoader.loadObjects(mMap.getProjection().getVisibleRegion().latLngBounds, location, this);
            }
        }

        if (selectedMarker != null && selectedMarker != myPositionMarker) {
            Boolean positionEqual = roundLatLonIsEqual(selectedMarker.getPosition(), cameraPosition.target);
            if(positionEqual) {
                map_popup_cursor.setVisibility(View.VISIBLE);
            }
            else {
                map_popup_cursor.setVisibility(View.GONE);

            }
        }
    }


    private Boolean roundLatLonIsEqual(LatLng positionOne, LatLng positionTwo) {
        double onePosRoundLat = (double) Math.round(positionOne.latitude*10000)/10000;
        double onePosRoundLon = (double) Math.round(positionOne.longitude*10000)/10000;
        double twoPosRoundLat = (double) Math.round(positionTwo.latitude*10000)/10000;
        double twoPosRoundLon = (double) Math.round(positionTwo.longitude*10000)/10000;
        if(onePosRoundLat == twoPosRoundLat && onePosRoundLon == twoPosRoundLon) {
            return true;
        }
        else {
            return false;
        }
    }

    public void openMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void showSettingsAlert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.map_gps_off_warning)
                .setCancelable(false)
                .setPositiveButton(R.string.global_positive, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.dismiss();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.global_negative, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void objectLoaded(MapObjectModel mapObject) {

    }

    @Override
    public void objectsLoaded(List<MapObjectModel> mapObjects) {
        loadedObjectsID.clear();
        mapObjectsList.clear();
        mMap.clear();
        shoMyPositionMarker(location);
        for (MapObjectModel newMapObject : mapObjects) {
          loadObject(newMapObject);
        }
    }

    public BitmapDescriptor getIcon(MapObjectModel mapObjectModel) {
        DrawableMarker marker = new DrawableMarker(this);
        for (int i = 0; i < mapObjectsList.size(); i++) {
            MapObjectModel existMapObject = mapObjectsList.get(i);
            if(existMapObject.organization.longitude == mapObjectModel.organization.longitude && existMapObject.organization.latitude == mapObjectModel.organization.latitude) {
                existMapObject.getMarkerOptions().visible(false);
                existMapObject.neighborNext = mapObjectModel.organization.id;
                mapObjectModel.neighborsPrev = existMapObject.organization.id;
                mapObjectModel.setMultiObjectCount(existMapObject.getMultiObjectCount() + 1);
                existMapObject.setMultiObjectCount(existMapObject.getMultiObjectCount() + 1);
            }
        }
        int checkinsAll = mapObjectModel.organization.maleCountAll + mapObjectModel.organization.femaleCountAll;
        int icon = R.drawable.marker_one;
        if (checkinsAll > 0) {
            icon = R.drawable.marker_fm;
        }
        if (mapObjectModel.organization.isTop) {
            icon = R.drawable.marker_hot;
        }
        Bitmap markerBitmap = null;
        if(mapObjectModel.getMultiObjectCount() > 1) {
            String objectCount = String.valueOf(mapObjectModel.getMultiObjectCount());
            markerBitmap = marker.drawMultiObjectCount(this, R.drawable.marker_double, objectCount);
        } else if (checkinsAll > 0) {
            String maleCount = String.valueOf(mapObjectModel.organization.maleCountAll);
            String femaleCount = String.valueOf(mapObjectModel.organization.femaleCountAll);
            markerBitmap = marker.drawCheckinsCount(this, icon, maleCount, femaleCount);
        } else {
            return BitmapDescriptorFactory.fromResource(icon);
        }
        return BitmapDescriptorFactory.fromBitmap(markerBitmap);
    }

    private void loadAnnounces() {
        double lat = 0.0, lon = 0.0;
        if(location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }
        Look2meetApi.getInstance().getAnnounces(new AnnouncesFilter(lat, lon).getData(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Boolean success = response.getBoolean("success");
                    if(success) {
                        if(response.has("data")) {
                            JSONObject data = response.getJSONObject("data");
                            JSONArray objects = data.getJSONArray("objects");
                            if (objects != null) {
                                announces.clear();
                                for (int i = 0; i < objects.length(); ++i) {
                                    JSONObject obj = objects.getJSONObject(i);
                                    Organization org = Organization.ParseFromJson(obj);
                                    announces.add(org);
                                }
                                startShowPopups();
                            }
                        }
                    } else {
                        String message = response.getString("message");
                    }
                } catch (JSONException e) {
//                    Log.d("ZZZZZZZZZZZZZZ", "JSON exception: " + e.getMessage());
                } catch (Exception ex) {
//                    Log.d("ZZZZZZZZZZZZZZ", "exception: " + ex.getMessage());
                }
            }
        });
    }

    private void startShowPopups() {
        announceTimer = new Timer();
        final Handler uiHandler = new Handler();
        announceTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Organization org = announces.get(curAnnounceIndex);
                        if (curAnnounceIndex < announces.size() - 1) {
                            curAnnounceIndex++;
                        } else {
                            curAnnounceIndex = 0;
                        }
                        announcePopup.show(org);
                    }
                });
            }
        }, 0L, 10L * 1000);
    }

    private BroadcastReceiver announceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Organization org = intent.getParcelableExtra("organization");
            announcePopup.show(org);
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    announcePopup.Hide();
                }
            }, 5000);
        }
    };

    @Override
    public void onMapLoaded() {
        checkIntent();
    }

    @Override
    public void call(Object o) {
        Location  location = (Location) o;
        shoMyPositionMarker(location);
        this.location = location;
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
}
