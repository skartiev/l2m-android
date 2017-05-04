package com.zuzex.look2meet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.DialogObject;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.chat.ChatActivity;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends MenuActivity {
    private ListView fieldsListView;

    private TextView textViewFullName;
    private TextView textViewSex;
    private TextView textViewDeleteFriend;
    private TextView textViewAgeProfile;
    private TextView textViewTopCaption;
    TextView textViewLike;
    int id;
    int isLiked;
    int likes_cnt;
    Button btnLike;
    boolean isBlackList;
    ProfileOrganizationAdapter checkinsAdapter;

    GridView organizationsGridView;

    private List<UserProfile.Field> listOfFields;
    private UserProfile userProfile;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getActionBar().hide();
        textViewAgeProfile = (TextView) findViewById(R.id.textViewAge);
        textViewFullName = (TextView) findViewById(R.id.textViewNameProfile);
        textViewSex = (TextView) findViewById(R.id.textViewSex);
        textViewDeleteFriend = (TextView) findViewById(R.id.textViewDelete);
        textViewTopCaption = (TextView) findViewById(R.id.activity_top_caption);
        textViewLike = (TextView) findViewById(R.id.likesCount);
        btnLike = (Button) findViewById(R.id.buttonLike);
        listOfFields = new ArrayList<UserProfile.Field>();

    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        if(id == -1) {
            id = UserProfile.getInstance().id;
        }
        loadUserProfile();
        YandexMetrica.onResumeActivity(this);
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

    private void setUser(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

     private void showUser() {
         String name = userProfile.name;
         textViewTopCaption.setText(name);
         String sex = userProfile.sex;
         String avatarUrl = userProfile.avatarUrl;
         int age = userProfile.age;
         textViewAgeProfile.setText(String.valueOf(age));
         textViewDeleteFriend.setText(userProfile.isFavorite ? getString(R.string.delete_from_friends) : getString(R.string.add_to_friends));
         textViewFullName.setText(name);
         likes_cnt = userProfile.likes;
         isLiked = userProfile.isLike;
         isBlackList = userProfile.isInBlackList;
         TextView textViewToBlack = (TextView) findViewById(R.id.textViewAddBlackList);
         if(isBlackList){
             textViewToBlack.setText(R.string.remove_from_blacklist);
         }
         else{
             textViewToBlack.setText(R.string.add_to_blacklist);
         }
         ImageView onlineImage = (ImageView) findViewById(R.id.myProfileImageViewCurrent);

         if(userProfile.isOnline){
             onlineImage.setImageResource(R.drawable.mark_online_green);
         } else {
             onlineImage.setImageResource(R.drawable.mark_online_red);
         }
         onlineImage.setVisibility(View.VISIBLE);
         textViewLike.setText(String.valueOf(likes_cnt));
         checkIsLike();
        if(sex.equalsIgnoreCase("male")){
            sex = getResources().getString(R.string.sex) +" "+ getResources().getString(R.string.registration_sex_male);
        }
        else if(sex.equalsIgnoreCase("female")){
            sex = getResources().getString(R.string.sex) +" "+getResources().getString(R.string.registration_sex_female);
        }
        userProfile.sex = sex;
        textViewSex.setText(sex);
        ImageView image = (ImageView) findViewById(R.id.imageViewProfileOther);
        ImageLoader.getInstance().displayImage(avatarUrl, image);
         if(userProfile.checkinsList.size() > 0) {
            showCheckins();
         } else {
             showFields();
         }

         ImageView vip = (ImageView) findViewById(R.id.vipIcon);
         if(userProfile.isVip) {
             vip.setVisibility(View.VISIBLE);
         } else {
             vip.setVisibility(View.INVISIBLE);
         }
         ImageView favoriteImage = (ImageView) findViewById(R.id.favoriteImage);
         if (userProfile.isFavorite) {
            favoriteImage.setVisibility(View.VISIBLE);
         } else {
            favoriteImage.setVisibility(View.GONE);
         }
    }

    private void showCheckins() {
        organizationsGridView = (GridView) findViewById(R.id.profile_organisation_gridview);
        organizationsGridView.setVisibility(View.VISIBLE);
        checkinsAdapter = new ProfileOrganizationAdapter(this, R.id.profile_organisation_gridview, userProfile.checkinsList);
        organizationsGridView.setAdapter(checkinsAdapter);
        organizationsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent organization = new Intent(getApplicationContext(), OrganisationActivity.class);
                final int object_id = checkinsAdapter.getItem(i).object_id;
                organization.putExtra("mapObjectID", object_id);
                startActivity(organization);
            }
        });
        checkinsAdapter.notifyDataSetChanged();
        listOfFields.clear();
        for (UserProfile.Field field : userProfile.fields) {
            if (!field.value.equals("null")) {
                listOfFields.add(field);
            }
        }
    }

    private void showFields() {
        fieldsListView = (ListView) findViewById(R.id.gridViewNameProfile);
        fieldsListView.setVisibility(View.VISIBLE);
        MyProfileLoaderAdapter FieldListadapter = new MyProfileLoaderAdapter(this, listOfFields, false);

        fieldsListView.setAdapter(FieldListadapter);
        FieldListadapter.notifyDataSetChanged();
    }

    public void deleteUserFriend(View v)
    {
        preloader.launch();
        if(userProfile.isFavorite) {
            Look2meetApi.getInstance().deleteFriend(id, friendChangeHandler());
        } else {
            Look2meetApi.getInstance().addFriend(id, friendChangeHandler());
        }
    }


    private JsonHttpResponseHandler friendChangeHandler() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                onResponseDelete(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                errorResponse();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                errorResponse();
            }
        };
    }

    private void loadUserProfile() {
        preloader.launch();
	    Look2meetApi.getInstance().getUsersProfileByID(id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                responseProfiles(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                errorResponse();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                errorResponse();
            }
        });
    }
    public void responseProfiles(JSONObject response) {
        if (GlobalHelper.successStatusFromJson(response,true,null)) {
            JSONObject mainInfo = response.optJSONObject("data");
            JSONObject user = mainInfo.optJSONObject("user");
            UserProfile up = new UserProfile();
            up.parseFromJson(user);
            setUser(up);
            showUser();
            preloader.done();
        }
        else {
            GlobalHelper.loggSend("USER_PROFILE");
            preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            finish();
        }
    }

    private void errorResponse() {
        preloader.cancel("", "Error loading");
        finish();
    }

    private void onResponseDelete(JSONObject response) {
        Boolean success = response.optBoolean("success");
        ImageView favoriteImage = (ImageView) findViewById(R.id.favoriteImage);
        if(success) {
            if (userProfile.isFavorite) {
                textViewDeleteFriend.setText(getString(R.string.add_to_friends));
                favoriteImage.setVisibility(View.GONE);
                userProfile.isFavorite = false;
            } else {
                textViewDeleteFriend.setText(getString(R.string.delete_from_friends));
                userProfile.isFavorite = true;
                favoriteImage.setVisibility(View.VISIBLE);
            }
            preloader.done();
        } else {
            String message = response.optString("message");
            preloader.cancel(getString(R.string.error), message);
        }
    }

	public void onSendMessagePress(View v) {
        Look2meetApi.getInstance().addDialogsWithUser(userProfile.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (GlobalHelper.successStatusFromJson(response, true, null)) {
                    JSONObject data = response.optJSONObject("data");
                    DialogObject dialogObject = new DialogObject(data);

                    Intent chatIntent = new Intent(getBaseContext(), ChatActivity.class);
                    chatIntent.putExtra("currDialogObject", dialogObject);
                    startActivity(chatIntent);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                preloader.cancel("", "Error loading");
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel("", "Error loading");
                finish();
            }
        });
	}
	public void onSendGiftPress(View v) {
        Intent giftIntent = new Intent(getBaseContext(),GiftsActivity.class);
        giftIntent.putExtra("id",userProfile.id);
        startActivity(giftIntent);
	}
    public void onAddBlackListFriend(View v)
    {
        preloader.launch();
	    Look2meetApi.getInstance().addToBlackList(userProfile.id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                onResponse(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                errorResponse();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                errorResponse();
            }
        });
    }

    public void onResponse(JSONObject response) {
        Boolean success = response.optBoolean("success");
        if(success) {
            TextView textViewToBlack = (TextView) findViewById(R.id.textViewAddBlackList);
            if (isBlackList) {
                textViewToBlack.setText(getString(R.string.add_to_blacklist));
                isBlackList = false;
            } else {
                textViewToBlack.setText(getString(R.string.remove_from_blacklist));
                isBlackList = true;
            }
            preloader.done();
        } else {
            String message = response.optString("message");
            preloader.cancel(getString(R.string.error), message);
        }
    }

    public void profile_like_btn_press(View v) {
        preloader.launch();
	    Look2meetApi.getInstance().setLike(userProfile.guid, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                btnParseResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                errorResponse();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                errorResponse();
            }
        });
    }

    private void btnParseResponse(JSONObject jsonObject) {
        Boolean success = jsonObject.optBoolean("success");
        if(success) {
            if (isLiked == 1) {
                textViewLike.setTextColor(Color.GRAY);
                btnLike.setBackgroundResource(R.drawable.like_gray2x);
                likes_cnt = likes_cnt - 1;
                textViewLike.setText(String.valueOf(likes_cnt));
                isLiked = 0;
                userProfile.isLike = isLiked;

            } else {
                likes_cnt = likes_cnt + 1;
                textViewLike.setTextColor(Color.RED);
                textViewLike.setText(String.valueOf(likes_cnt));
                btnLike.setBackgroundResource(R.drawable.like2x);
                isLiked = 1;
                userProfile.isLike = isLiked;
            }

            preloader.done();
        } else {
            String message = jsonObject.optString("message");
            GlobalHelper.loggSend(message);
            preloader.cancel(getString(R.string.error), message);
        }
    }
    private void checkIsLike() {

        if(isLiked == 1)
        {
            textViewLike.setTextColor(Color.RED);
           btnLike.setBackgroundResource(R.drawable.like2x);
        }
        else
        {
            textViewLike.setTextColor(Color.GRAY);
           btnLike.setBackgroundResource(R.drawable.like_gray2x);
        }
    }
    public void infoClicked(View v)
    {
        Intent myProfileIntent = new Intent(getBaseContext(), MyProfileActivity.class);
        myProfileIntent.putExtra("id",id);
        myProfileIntent.putExtra("isMe",false);
        startActivityForResult(myProfileIntent, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        YandexMetrica.onPauseActivity(this);
    }
}
