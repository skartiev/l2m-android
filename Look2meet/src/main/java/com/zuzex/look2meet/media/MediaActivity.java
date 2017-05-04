package com.zuzex.look2meet.media;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.IUpdateStatus;
import com.zuzex.look2meet.DataModel.UserProfileMedia;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.media.adapter.GalleryAdapter;
import com.zuzex.look2meet.utils.AnimationPreloader;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class MediaActivity extends CaptureMedia implements PopupMenu.OnMenuItemClickListener, IUpdateStatus{
    private final static String TAG = "MediaActivity";
    private AnimationPreloader preloader;
    private GridView gridView;
    private ArrayList<UserProfileMedia> media;
    private GalleryAdapter galleryAdapter;
    public static String EXTRA_PHOTO = "extra_photo";
    public static String EXTRA_VIDEO = "extra_video";
    public static String EXTRA_MY_PROFILE = "my_profile";
    public static String EXTRA_IS_VIP = "extra_isVip";
    private boolean isMyProfile = false;
    private boolean isVip = false;
    private TextView textTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        gridView = (GridView)findViewById(R.id.photo_gridview);
        getActionBar().hide();
        textTopBar = (TextView) findViewById(R.id.textViewMediaName);
        Intent intent = getIntent();
        isMyProfile = intent.getBooleanExtra(EXTRA_MY_PROFILE, false);
        if(intent.hasExtra(EXTRA_PHOTO)) {
            media = intent.getParcelableArrayListExtra(EXTRA_PHOTO);
            textTopBar.setText(R.string.photo);
            init(CONTENT_TYPE.PHOTO);
            galleryAdapter = new GalleryAdapter(this, media, GalleryAdapter.VIEW_TYPE_PHOTO, isMyProfile);
        } else if (intent.hasExtra(EXTRA_VIDEO)) {
            media = intent.getParcelableArrayListExtra(EXTRA_VIDEO);
            textTopBar.setText(R.string.video);
            init(CONTENT_TYPE.VIDEO);
            galleryAdapter = new GalleryAdapter(this, media, GalleryAdapter.VIEW_TYPE_VIDEO, isMyProfile);
        } else {
            media = new ArrayList<UserProfileMedia>();
        }
        isVip = intent.getBooleanExtra(EXTRA_IS_VIP, false);
        checkEditable();
        preloader = new AnimationPreloader(this, ProgressDialog.STYLE_HORIZONTAL);
        gridView.setAdapter(galleryAdapter);
        mFile = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        YandexMetrica.onResumeActivity(this);
    }

    void checkEditable() {
        LinearLayout topLayout = (LinearLayout)findViewById(R.id.top_bar);
        ImageButton deleteMediaButton = (ImageButton)topLayout.findViewById(R.id.deleteMedia);
        ImageButton captureMediaButton = (ImageButton)topLayout.findViewById(R.id.captureMedia);
        if(isMyProfile) {
            captureMediaButton.setVisibility(View.VISIBLE);
	        deleteMediaButton.setVisibility((galleryAdapter.getCount() > 0 ? View.VISIBLE : View.INVISIBLE));
            checkIsVip(captureMediaButton);
        }
        else {
            captureMediaButton.setVisibility(View.INVISIBLE);
	        deleteMediaButton.setVisibility(View.INVISIBLE);
        }
    }

    void checkIsVip (ImageButton captureMediaButton)
    {
	    if(getIntent().hasExtra(EXTRA_VIDEO) && isVip == false) {
			captureMediaButton.setVisibility(View.GONE);
	    }
    }

    public void captureMedia(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.add_photo_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.add_from_camera_item: {
                captureFromCamera();
                break;
            }
            case R.id.add_from_gallery_item: {
                captureFromGallery();
                break;
            }
            default:
                return false;
        }
        return true;
    }

    void startUploadFile(final File file) {
        if(file == null)
            return;
        switch (content_type) {
            case VIDEO:
                Look2meetApi.getInstance().addVideoToGallery(this, file, preloader, this);
                break;
            case PHOTO:
                Look2meetApi.getInstance().addPhotoToGallery(this, file, preloader, this);
        }
    }

    public void deleteMedia(View view) {
        galleryAdapter.toggleDelete();
        galleryAdapter.notifyDataSetChanged();
    }

    public void backButtonClick(View view) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_IMAGE_FROM_CAMERA):
                    startUploadFile(mFile);
                    break;
                case (REQUEST_IMAGE_FROM_GALLERY):
                    startUploadFile(new File(getRealPathFromURI(data.getData())));
                    break;
                case (REQUEST_VIDEO_FROM_CAMERA):
                    startUploadFile(mFile);
                    break;
                case (REQUEST_VIDEO_FROM_GALLERY):
                    startUploadFile(new File(getRealPathFromURI(data.getData())));
                    break;
                default:
                    return;
            }
        } else if(resultCode == RESULT_CANCELED) {
            galleryAdapter.notifyDataSetChanged();
        }
    }

    protected String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onUpdateSuccess(JSONObject response) {
        galleryAdapter.notifyDataSetChanged();
	    checkEditable();
    }

    @Override
    public void onUpdateError() {
        preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
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


