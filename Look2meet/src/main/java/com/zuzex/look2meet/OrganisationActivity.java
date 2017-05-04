package com.zuzex.look2meet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.IUpdateStatus;
import com.zuzex.look2meet.DataModel.Organization;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.OrgList.OrgListActivity;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OrganisationActivity extends MenuActivity {

    private ListView gridViewAdditional;
    private int isLiked;
    private int isFavorite;
    private int id;
    private TextView textViewLike;
    private TextView textFavorites;
    private ImageButton btnLike;
    private TextView now;
    private Organization org;
    private LayoutInflater inflater;
    private TextView tvCaption;
    private TextView favoriteIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisation);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        gridViewAdditional = (ListView) findViewById(R.id.gridViewAdditional);
        btnLike = (ImageButton) findViewById(R.id.buttonLike);
        textFavorites = (TextView) findViewById(R.id.textViewFavorites);
        now = (TextView) findViewById(R.id.textViewNow);
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tvCaption = (TextView)findViewById(R.id.activity_top_caption);
        favoriteIcon = (TextView) findViewById(R.id.favoriteImage);
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
        checkIntent(getIntent());
        YandexMetrica.onResumeActivity(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        checkIntent(intent);
    }

    public void checkIntent(Intent intent) {
        int defaultValue = -1;
        int mapObjectId = intent.getIntExtra("mapObjectID", defaultValue);
        if(intent.hasExtra("org_object")) {
            org = intent.getParcelableExtra("org_object");
        } else {
            org = new Organization();
            org.id = mapObjectId;
        }
        if(mapObjectId != defaultValue) {
            loadMapObjectViewById(mapObjectId);
        }
    }

    void loadMapObjectViewById (Integer id) {
        if(Look2meetApi.getInstance().isConnected(this)) {
            preloader.launch();
	        Look2meetApi.getInstance().getObjectView(id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    preloader.done();
                    try {
                        parseViewById(response);
                    } catch (JSONException e) {
                        GlobalHelper.loggSend("JSON_EXCEPTION");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    GlobalHelper.loggSend("NETWORK ERROR");
                    preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                }
            });
        }
    }

    private void parseViewById(JSONObject jsonObject) throws JSONException {

        boolean success = false;
        try {
            success = jsonObject.getBoolean("success");
        } catch (JSONException e) {
            success = false;
        }
        if (success) {

            JSONObject data = null;
            JSONObject object = null;
            JSONArray addFields = null;
            JSONObject checkinsAll = null;

            try {
                data = jsonObject.getJSONObject("data");
                object = data.getJSONObject("object");
                checkinsAll = object.getJSONObject("checkinsAll");
                id = object.getInt("id");
            } catch (JSONException e) {
                GlobalHelper.loggSend("JSON_EXCEPTION");
            }

            try {
                addFields = object.getJSONArray("addFields");
            } catch (JSONException e) {
                addFields = null;
            }
            org.UpdateFromJson(object);

            TextView textViewTimeWork = (TextView) findViewById(R.id.textViewTimeWork);
            ImageView imageView = (ImageView) findViewById(R.id.imageViewOrg);
            TextView textView = (TextView) findViewById(R.id.textViewHeaderType);
            TextView textViewAddress = (TextView) findViewById(R.id.textViewAddress);
            TextView textViewDescription = (TextView) findViewById(R.id.textViewDescription);
            View descriptionDivider = findViewById(R.id.descriptionDivider);
            textViewLike = (TextView) findViewById(R.id.textViewLike);
            TextView textViewHereGirl = (TextView) findViewById(R.id.textViewHereGirl);
            TextView textViewHereBoy = (TextView) findViewById(R.id.textViewHereBoy);
            TextView textViewSoonGirl = (TextView) findViewById(R.id.textViewSoonGirl);
            TextView textViewSoonBoy = (TextView) findViewById(R.id.textViewSoonBoy);
            TextView textViewPlanGirl = (TextView) findViewById(R.id.textViewPlanGirl);
            TextView textViewPlanBoy = (TextView) findViewById(R.id.textViewPlanBoy);
            TextView textViewVisitorsGirl = (TextView) findViewById(R.id.textViewVisitorGirl);
            TextView textViewVisitorsBoy = (TextView) findViewById(R.id.textViewVisitorBoy);
            ScrollView sv = (ScrollView)findViewById(R.id.scrollView);
            ImageView topImage = (ImageView) findViewById(R.id.organization_top_icon);

            try {
                Integer male_cnt_now = checkinsAll.getInt("male_cnt_now");
                Integer female_cnt_now = checkinsAll.getInt("female_cnt_now");
                Integer human_cnt_now = male_cnt_now + female_cnt_now;
                now.setText(human_cnt_now.toString());
                Integer male_cnt_soon = checkinsAll.getInt("male_cnt_soon");
                Integer female_cnt_soon = checkinsAll.getInt("female_cnt_soon");
                Integer male_cnt_plan = checkinsAll.getInt("male_cnt_plan");
                Integer female_cnt_plan = checkinsAll.getInt("female_cnt_plan");
                Integer male_cnt_all = male_cnt_now + male_cnt_plan + male_cnt_soon;
                Integer female_cnt_all = female_cnt_now + female_cnt_plan + female_cnt_soon;
                isLiked = object.getInt("like");
                if (isLiked == 1) {
                    textViewLike.setTextColor(Color.parseColor("#e4022f"));
                    btnLike.setBackgroundResource(R.drawable.like2x);
                } else {
                    textViewLike.setTextColor(Color.parseColor("#979191"));
                    btnLike.setBackgroundResource(R.drawable.like_gray2x);
                }

                isFavorite = object.getInt("isFavorite");

                if (isFavorite == 1) {
                    textFavorites.setText("В ИЗБРАННОМ");
                    favoriteIcon.setVisibility(View.VISIBLE);
                } else {
                    textFavorites.setText("В ИЗБРАННОЕ");
                    favoriteIcon.setVisibility(View.GONE);
                }

                textViewAddress.setText(org.address);
                textViewTimeWork.setText(org.workTime);
                textView.setText(org.name);
                textViewDescription.setText(org.announceShort);

                textViewLike.setText(String.valueOf(org.likesCount));
                textViewHereGirl.setText(female_cnt_now.toString());
                textViewHereBoy.setText(male_cnt_now.toString());
                textViewSoonGirl.setText(female_cnt_soon.toString());
                textViewSoonBoy.setText(male_cnt_soon.toString());
                textViewPlanGirl.setText(female_cnt_plan.toString());
                textViewPlanBoy.setText(male_cnt_plan.toString());
                textViewVisitorsGirl.setText(female_cnt_all.toString());
                textViewVisitorsBoy.setText(male_cnt_all.toString());
                tvCaption.setText(org.name);

                gridViewAdditional.setVerticalScrollBarEnabled(false);

                if (addFields != null) {
                    ArrayList<Field> fieldses = new ArrayList<Field>();
                    for (int i=0; i<addFields.length();i++) {
                        JSONObject temp = addFields.getJSONObject(i);
                        Field tempField = new Field(temp.getString("label"),temp.getString("value"),0,"");
                        if(!temp.getBoolean("isNull") && !temp.getString("value").equals("false")) {
                            fieldses.add(tempField);
                        }
                    }
                    OrganizationLoaderAdapter adapter = new OrganizationLoaderAdapter(this,fieldses);
                    gridViewAdditional.setAdapter(adapter);
                }

                if(org.announceFull.isEmpty()) {
                    textViewDescription.setVisibility(View.GONE);
                    descriptionDivider.setVisibility(View.GONE);
                }
                sv.smoothScrollTo(0, 0);
                ImageLoader.getInstance().displayImage(org.avatarUrl, imageView);
                // TODO: допилить парсинг в объект

                if(org.isTop) {
                    topImage.setVisibility(View.VISIBLE);
                } else {
                    topImage.setVisibility(View.INVISIBLE);
                }

            } catch (JSONException e) {
                GlobalHelper.loggSend("JSON_EXCEPTION");
            }
        }
    }

    public void like_btn_press(View v)
    {
        preloader.launch();
	    Look2meetApi.getInstance().setLike(org.guid, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    btnParseResponse(response);
                } catch (JSONException e) {
                    GlobalHelper.loggSend("JSON_EXCEPTION");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                GlobalHelper.loggSend("NETWORK ERROR");
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
        });
    }

    private void btnParseResponse(JSONObject jsonObject) throws JSONException {
        Boolean success = jsonObject.optBoolean("success");
        if(success) {
            if (org.isLiked) {
                btnLike.setBackgroundResource(R.drawable.like_gray2x);
               // likes_cnt = likes_cnt - 1;
                org.likesCount = org.likesCount -1;
                textViewLike.setText(String.valueOf(org.likesCount));
                textViewLike.setTextColor(Color.parseColor("#979191"));
                org.isLiked = false;

            } else {
                org.likesCount = org.likesCount + 1;
                textViewLike.setText(String.valueOf(org.likesCount));
                textViewLike.setTextColor(Color.parseColor("#e4022f"));
                btnLike.setBackgroundResource(R.drawable.like2x);
                org.isLiked = true;
            }
            preloader.done();
        } else {
            String message = jsonObject.getString("message");
            preloader.cancel(getString(R.string.error), message);
        }
    }

    public void topBtnClicked(View v)
    {
       /* if(isFavorite == 1)
        {
            textFavorites.setText("В ИЗБРАННОЕ");
            isFavorite = 0;
        }
        else
        {
            textFavorites.setText("В ИЗБРАННОМ");
        }*/
        preloader.launch();
	    Look2meetApi.getInstance().setFavorites(id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    favoriteBtnParseResponse(response);
                } catch (JSONException e) {
                    GlobalHelper.loggSend("JSON_EXCEPTION");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                GlobalHelper.loggSend("NETWORK ERROR");
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
        });
    }

    private void favoriteBtnParseResponse(JSONObject jsonObject) throws JSONException {
        Boolean success = jsonObject.optBoolean("success");
        if(success) {
            if (isFavorite == 1) {
                textFavorites.setText("В ИЗБРАННОЕ");
                favoriteIcon.setVisibility(View.GONE);
                isFavorite = 0;
            } else {
                textFavorites.setText("В ИЗБРАННОМ");
                isFavorite = 1;
                favoriteIcon.setVisibility(View.VISIBLE);
            }
            preloader.done();
        } else {
            String message = jsonObject.getString("message");
            preloader.cancel(getString(R.string.error), message);
        }
    }
    public void onHereClickedWomen(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "female_cnt_now");
        intent.putExtra("curr_list","OrgList");
        intent.putExtra("id",org.id);
        startActivity(intent);
    }
    public void onHereClickedMen(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "male_cnt_now");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onHereClicked(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "cnt_now");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onSoonClickedWomen(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "female_cnt_soon");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onSoonClickedMen(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "male_cnt_soon");
        intent.putExtra("curr_list","OrgList");
        intent.putExtra("id",org.id);
        startActivity(intent);
    }
    public void onSoonClicked(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "cnt_soon");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onPlanClickedWomen(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "female_cnt_plan");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onPlanClickedMen(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "male_cnt_plan");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onPlanClicked(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "cnt_plan");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onVisitorsClickedWomen(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "female_cnt_visitors");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onVisitorsClickedMen(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "male_cnt_visitors");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }
    public void onVisitorsClicked(View v)
    {
        Intent intent = new Intent(this, UsersList.class);
        intent.putExtra("category_name", org.categoryName);
        intent.putExtra("address",org.address);
        intent.putExtra("name",org.name);
        intent.putExtra("type", "cnt_visitors");
        intent.putExtra("id",org.id);
        intent.putExtra("curr_list","OrgList");
        startActivity(intent);
    }

    public void ShowOnMap(View v) {
        ArrayList<Organization> orgList = new ArrayList<Organization>();
        orgList.add(org);
        Intent intent = new Intent(getBaseContext(), MapActivity.class);
        intent.putParcelableArrayListExtra("organization_list", orgList);
        startActivity(intent);
    }

    public void ShowTop(View v) {
        Intent intent = new Intent(getBaseContext(), OrgListActivity.class);
        intent.putExtra("list_type", "top");
        startActivityForResult(intent, 0);
    }

    public void BackClicked(View v) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void CheckinClicked(View v) {
        if(org.checkinType.isEmpty()) {
            final String[] checkinTypes = new String[] { getString(R.string.checkin_type_now), getString(R.string.checkin_type_soon), getString(R.string.checkin_type_plan) };
            View pickerView = inflater.inflate(R.layout.checkin_type_picker, null);
            final NumberPicker picker = (NumberPicker)pickerView.findViewById(R.id.checkin_type_numpicker);
            picker.setMinValue(0);
            picker.setMaxValue(checkinTypes.length - 1);
            picker.setDisplayedValues(checkinTypes);
            picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            picker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {

                }
            });

            new AlertDialog.Builder(this).setTitle(getString(R.string.announes_filter_checkin_type)).setView(pickerView).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final String checkinType = GlobalHelper.GetCheckinType(checkinTypes[picker.getValue()]);
                    preloader.launch();

                    Look2meetApi.getInstance().setCheckin(checkinType, org.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Boolean success = response.getBoolean("success");
                                if(success) {
                                    org.checkinType = checkinType;
                                    UserProfile.getInstance().reload(new IUpdateStatus() {
                                        @Override
                                        public void onUpdateSuccess(JSONObject response) {
                                            preloader.done();
                                        }

                                        @Override
                                        public void onUpdateError() {
                                            preloader.done();
                                        }
                                    });
                                } else {
                                    String message = response.getString("message");
                                    preloader.cancel(getString(R.string.error), message);
                                }
                            } catch (JSONException e) {
                                GlobalHelper.loggSend("JSON_EXCEPTION");
                                preloader.cancel(getString(R.string.announes_filter_checkin_type), getString(R.string.error_try_again));
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            GlobalHelper.loggSend("ERROR TRY AGAIN");
                            preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                        }
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                        }
                    });
                }
            }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create().show();
        } else {
            String message = getString(R.string.already_checked_in) + " \"" + GlobalHelper.GetLocalizedCheckinType(org.checkinType) + "\". " + getString(R.string.go_away);
            new AlertDialog.Builder(this).setTitle(getString(R.string.announes_filter_checkin_type)).setMessage(message).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Look2meetApi.getInstance().deleteCheckin(org.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                Boolean success = response.getBoolean("success");
                                if (success) {
                                    org.checkinType = "";
                                    UserProfile.getInstance().reload(new IUpdateStatus() {
                                        public void onUpdateSuccess(JSONObject response) {
                                            preloader.done();
                                        }

                                        @Override
                                        public void onUpdateError() {
                                            preloader.done();
                                        }
                                    });
                                } else {
                                    String message = response.getString("message");
                                    preloader.cancel(getString(R.string.error), message);
                                }
                            } catch (JSONException e) {
                                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                GlobalHelper.loggSend("ERROR, TRY AGAIN");
                                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                        }
                    });
            }
        }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).create().show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public void onPause() {
        super.onPause();
        YandexMetrica.onPauseActivity(this);
    }
}
