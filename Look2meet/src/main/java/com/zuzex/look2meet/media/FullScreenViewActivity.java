package com.zuzex.look2meet.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.google.analytics.tracking.android.EasyTracker;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.UserProfileMedia;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.media.adapter.FullScreenImageAdapter;

import java.util.ArrayList;

public class FullScreenViewActivity extends Activity implements ViewPager.OnPageChangeListener{

	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;
    private int position = 0;
    private ArrayList<UserProfileMedia> media;
    Button buttonBack;
    Button buttonForward;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_full_screen_view);
		viewPager = (ViewPager) findViewById(R.id.pager);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        media = intent.getParcelableArrayListExtra("media");

		ArrayList<String> filePathArray = getIntent().getStringArrayListExtra("filePathArray");
//		Log.wtf("FullScreenViewActivity", position +"|"+ filePathArray.toString());
        buttonBack = (Button) findViewById(R.id.image_gallery_closeFullscreen_button_back);
        buttonForward = (Button) findViewById(R.id.image_gallery_closeFullscreen_button_forward);
        adapter = new FullScreenImageAdapter(this, media);
		if (filePathArray != null)
			adapter.filePathArray = filePathArray;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.setOnPageChangeListener(this);
        checkPosition(position);
	}

    public void backButtonClick(View view) {
        finish();
    }

    public void close(View view) {
        finish();
    }

    public void forward(View view) {
        Button buttonBack = (Button) findViewById(R.id.image_gallery_closeFullscreen_button_back);
        buttonBack.setTextColor(Color.WHITE);
        if(position < adapter.getCount()-1) {
            viewPager.setCurrentItem(position+=1);
        }
    }

    public void back(View view) {
        if(position > 0) {
            viewPager.setCurrentItem(position-=1);
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        checkPosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void checkPosition(int position) {
        this.position = position;
        if(position==0) {
            buttonBack.setTextColor(Color.GRAY);
            buttonBack.setBackgroundResource(R.drawable.layout_border_gray);
            buttonForward.setTextColor(Color.WHITE);
            buttonForward.setBackgroundResource(R.drawable.layout_border_white);
        }
        else if(position == media.size()-1) {
            buttonBack.setTextColor(Color.WHITE);
            buttonBack.setBackgroundResource(R.drawable.layout_border_white);
            buttonForward.setTextColor(Color.GRAY);
            buttonForward.setBackgroundResource(R.drawable.layout_border_gray);
        }
        else {
            buttonBack.setTextColor(Color.WHITE);
            buttonForward.setTextColor(Color.WHITE);
            buttonBack.setBackgroundResource(R.drawable.layout_border_white);
            buttonForward.setBackgroundResource(R.drawable.layout_border_white);
        }
    }

}
