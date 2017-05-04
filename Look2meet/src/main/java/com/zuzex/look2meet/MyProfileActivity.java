package com.zuzex.look2meet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.DataModel.UserProfileMedia;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.media.MediaActivity;
import com.zuzex.look2meet.socket.SocketWorker;
import com.zuzex.look2meet.utils.AnimationPreloader;
import com.zuzex.look2meet.utils.StorageHelper;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends MenuActivity implements AdapterView.OnItemClickListener {
    private static String TAG = "MyProfileActivity";
    private ListView view;
    private TextView textView;
    boolean isMy;
    int id;
    int isChecked;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GET_FROM_GALLERY = 2;
    File mCurrentPhotoPath;
    private UserProfile profile;
    private List<UserProfile.Field> fieldList;
    private MyProfileLoaderAdapter adapter;
    private ImageView avatarImage;
    ImageView viewIsOnline;
    PopupMenu popup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isChecked = 0;
        isMy = true;
        fieldList = new ArrayList<UserProfile.Field>();
        getActionBar().hide();
        setContentView(R.layout.activity_my_profile);
        avatarImage = (ImageView) findViewById(R.id.imageViewProfileOther);
        viewIsOnline = (ImageView) findViewById(R.id.myProfileImageViewCurrent);
        Intent intent = getIntent();
        isMy = intent.getBooleanExtra("isMe", true);
        id = intent.getIntExtra("id", 0);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        view = (ListView) findViewById(R.id.gridView);
        view.setClickable(true);
        profile = new UserProfile();
        adapter = new MyProfileLoaderAdapter(this, false);
        if(isMy)
        {
            loadMyUserProfile();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

            if (!isMy) {
                preloader = new AnimationPreloader(this);
                preloader.launch();
                Look2meetApi.getInstance().getUsersProfileByID(id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        responseOtherUser(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                    }
                });
            } else {
                ImageView viewIsFriend = (ImageView) findViewById(R.id.myProfileImageViewIsFriend);
                viewIsFriend.setVisibility(View.INVISIBLE);

            }
        YandexMetrica.onResumeActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        YandexMetrica.onPauseActivity(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_myProfile: {
                loadMyUserProfile();
                return true;
            }
            case R.id.add_from_camera_item: {
                getImageFromCamera();
                break;
            }
            case R.id.add_from_gallery_item: {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GET_FROM_GALLERY);
                break;
            }
        }
        return super.onMenuItemClick(menuItem);
    }

    private void loadMyUserProfile() {
        profile = UserProfile.getInstance();
        isMy = true;
        showUserProfile();
        loadUserProfile();
    }

    private void showUserProfile() {
        fieldList.clear();
        fieldList.add(new UserProfile.Field(getString(R.string.search_param_country), profile.country_name));
        fieldList.add(new UserProfile.Field(getString(R.string.search_param_city), profile.city_name));

        for (UserProfile.Field field : profile.fields) {
            if (!field.value.equals("null")) {
                fieldList.add(field);
            }
        }
        textView = (TextView) findViewById(R.id.textViewNameProfile);

        String avatarUrl = profile.avatarUrl;
        String sex = profile.sex;
        String name = profile.name;
        Integer age = profile.age;

        avatarImage.setImageResource(R.drawable.no_image);
        ImageLoader.getInstance().displayImage(avatarUrl, avatarImage);


        TextView textViewAge = (TextView) findViewById(R.id.textViewAge);
        textViewAge.setText(age.toString());
        textView.setText(name);
        if (sex.equalsIgnoreCase("male")) {
            sex = getResources().getString(R.string.registration_sex_male);
        } else if (sex.equalsIgnoreCase("female")) {
            sex = getResources().getString(R.string.registration_sex_female);
        }
        TextView textViewSex = (TextView) findViewById(R.id.textViewSex);
        textViewSex.setText("Пол: " + sex);

        ImageView vip = (ImageView) findViewById(R.id.vipIcon);
        if(profile.isVip) {
            vip.setVisibility(View.VISIBLE);
        } else {
            vip.setVisibility(View.INVISIBLE);
        }

        if(isMy) {
            if(SocketWorker.getInstance().isConnected) {
                viewIsOnline.setImageResource(R.drawable.mark_online_green);
            } else {
                viewIsOnline.setImageResource(R.drawable.mark_online_red);
            }
        } else {
            if(profile.isOnline) {
                viewIsOnline.setImageResource(R.drawable.mark_online_green);
            } else {
                viewIsOnline.setImageResource(R.drawable.mark_online_red);
            }
        }


        Button buttonEditMyProfile = (Button) findViewById(R.id.profile_edit);

        if(isMy) {
            buttonEditMyProfile.setVisibility(View.VISIBLE);
        } else {
            buttonEditMyProfile.setVisibility(View.INVISIBLE);
        }

        adapter.setItems(fieldList);
        view.setAdapter(adapter);
        view.setOnItemClickListener(this);
        adapter.notifyDataSetChanged();
    }

    private void loadUserProfile() {
        Look2meetApi.getInstance().getUserProfile(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if(GlobalHelper.successStatusFromJson(response, true, getBaseContext())) {
                    JSONObject data = response.optJSONObject("data");
                    if (data != null) {
                        profile.parseFromJson(data);
                        showUserProfile();
                        preloader.done();
                    } else {
                        preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
        });
    }

    public void responseOtherUser(JSONObject response) {
        try {
            if(GlobalHelper.successStatusFromJson(response, true, getBaseContext())) {
                JSONObject data = response.optJSONObject("data");
                if(data != null) {
                    JSONObject user = data.getJSONObject("user");
                    if(user != null) {
                        profile.parseFromJson(user);
                        showUserProfile();
                        preloader.done();
                    } else {
                        preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                    }
                } else {
                    preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void FavoritesClicked(View v) {
        Intent intent = new Intent(this, MediaActivity.class);
        intent.putParcelableArrayListExtra(MediaActivity.EXTRA_PHOTO, profile.photos);
        if (isMy) {
            intent.putExtra(MediaActivity.EXTRA_MY_PROFILE, true);
        } else {
            intent.putExtra(MediaActivity.EXTRA_MY_PROFILE, false);
        }
        startActivityForResult(intent, 1);
    }

    public void openVideoActivity(View v) {
        Intent intent = new Intent(this, MediaActivity.class);
        intent.putParcelableArrayListExtra(MediaActivity.EXTRA_VIDEO, profile.video);
        intent.putExtra(MediaActivity.EXTRA_IS_VIP, profile.isVip);
        if (isMy) {
            intent.putExtra(MediaActivity.EXTRA_MY_PROFILE, true);
        } else {
            intent.putExtra(MediaActivity.EXTRA_MY_PROFILE, false);
        }
        startActivityForResult(intent, 1);
    }
    public void popup(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.add_photo_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void onAvatarClick(View v)
    {
        popup = new PopupMenu(this, v);
        if(adapter.isEditable()) {

            new AlertDialog.Builder(this).setTitle(getString(R.string.warning)).setMessage(R.string.alert_avatar).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton) {
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.add_photo_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.add_from_camera_item: {
                                    getImageFromCamera();
                                    break;
                                }
                                case R.id.add_from_gallery_item: {
                                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(i, GET_FROM_GALLERY);
                                    break;
                                }
                            }
                            return true;
                        }

                    });


                popup.show();
            }
        }).setNegativeButton(this.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).create().show();
            //alert.show();

        }
    }

    void getImageFromCamera() {
        mCurrentPhotoPath = StorageHelper.createMediaFile(getApplicationContext(), "Look2meet", ".jpg");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoPath));
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            updateAvatar(mCurrentPhotoPath);
        } else if(requestCode == GET_FROM_GALLERY) {
            if(data == null) {
                return;
            }
            Uri selectedImage = data.getData();
            File finalFile = new File(getRealPathFromURI(selectedImage));
            updateAvatar(finalFile);
        }
    }

    private void updateAvatar(final File file) {
        preloader = new AnimationPreloader(this, ProgressDialog.STYLE_HORIZONTAL);
        preloader.launch();
        final String oldImageUrl = ImageLoader.getInstance().getLoadingUriForView(avatarImage);
        profile.avatarUrl = Uri.fromFile(file).toString();
        //showUserProfile();
        avatarImage.setImageURI(Uri.fromFile(file));
        avatarImage.invalidate();
        //ImageLoader.getInstance().displayImage(file.getAbsolutePath(),avatarImage);
        Look2meetApi.getInstance().upload2(this, file,  new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                UserProfileMedia m = new UserProfileMedia(response);
                profile.addAvatarId(m.id);
                preloader.done();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                ImageLoader.getInstance().displayImage(oldImageUrl, avatarImage);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                int progress = (int)((bytesWritten * 1.0 / totalSize) * 100);
                preloader.setProgress(progress);
            }
        });
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
        if(adapter.isEditable()) {
            final UserProfile.Field field = fieldList.get(i);
            String oldValue = field.value;
            if (field.dataType == UserProfile.FieldDataType.ENUM) {
                final NumberPicker picker = new NumberPicker(this);
                picker.setMinValue(0);
                picker.setMaxValue(field.options.size() - 1);
                picker.setDisplayedValues(field.options.toArray(new String[field.options.size()]));
                picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                picker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {

                    }
                });
                picker.setValue(field.options.indexOf(field.value));
                new AlertDialog.Builder(this).setTitle(field.title).setView(picker).setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        field.value = field.options.get(picker.getValue());
                        adapter.notifyDataSetChanged();
                    }
                }).setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.change_text);
                final EditText input = new EditText(this);
                input.setText(oldValue);
                alert.setView(input);
                alert.setPositiveButton(this.getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String enteredText = input.getText().toString();
                        field.value = enteredText;
                        fieldList.set(i, field);
                    }
                });
                alert.setNegativeButton(this.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alert.show();
            }
        }
    }

    public void profileEdit(View view) {
        preloader = new AnimationPreloader(this);
        adapter.setEditable(true);
        Button editButton = (Button) findViewById(R.id.profile_edit);
        LinearLayout editableLayout = (LinearLayout) findViewById(R.id.profile_edit_buttons);
        editButton.setVisibility(View.GONE);
        editableLayout.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void disableEdit() {
        Button editButton = (Button) findViewById(R.id.profile_edit);
        LinearLayout editableLayout = (LinearLayout) findViewById(R.id.profile_edit_buttons);
        editButton.setVisibility(View.VISIBLE);
        editableLayout.setVisibility(View.GONE);
        adapter.setEditable(false);
        adapter.notifyDataSetChanged();
    }

    public void profileSave(View view) {
        disableEdit();
        preloader = new AnimationPreloader(this);
        preloader.launch();
        profile.fields = fieldList.subList(2, fieldList.size() - 1);
        Look2meetApi.getInstance().updateProfile(profile, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                loadUserProfile();
                preloader.done();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
        });
    }

    public void profileCancel(View view) {
        disableEdit();
        loadUserProfile();
    }

    @Override
    public void showMyProfile() {
        profile = UserProfile.getInstance();
        isMy = true;
        showUserProfile();
    }

}
