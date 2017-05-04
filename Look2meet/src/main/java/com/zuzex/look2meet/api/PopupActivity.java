package com.zuzex.look2meet.api;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zuzex.look2meet.R;

/**
 * Created by romanabashin on 16.07.14.
 */
public class PopupActivity extends Activity {

	private Button okButton;
	private Button leftButton;
	private Button rightButton;
	private TextView textLabel;
	private LinearLayout btnsLayout;

	private PopupActivity self;

	public boolean isOkMode = true;
	public String text = "Error";

	@Override
	public void onBackPressed() {
		self = null;
		super.onBackPressed();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_popup);
		self = this;

		Intent intent = getIntent();
		if (intent.hasExtra("text"))
			text = intent.getStringExtra("text");
		if (intent.hasExtra("isOkMode"))
			isOkMode = intent.getBooleanExtra("isOkMode", true);


		initViews();
	}

	private void initViews() {
		okButton = (Button) findViewById(R.id.okButton);
		leftButton = (Button) findViewById(R.id.leftButton);
		rightButton = (Button) findViewById(R.id.rightButton);
		textLabel = (TextView) findViewById(R.id.textLabel);
		btnsLayout = (LinearLayout) findViewById(R.id.btnsLayout);

		leftButton.setText(R.string.api_mobile_data);
		rightButton.setText(R.string.api_wifi_data);

		textLabel.setText(text);

		if (isOkMode) {
			leftButton.setHeight(0);
			rightButton.setHeight(0);
			btnsLayout.setVisibility(View.GONE);
		} else {
			okButton.setHeight(0);
			okButton.setVisibility(View.GONE);
		}

		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.onBackPressed();
			}
		});


		leftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				self.onBackPressed();
//				Context context = Look2meet.getContext();
				self.startActivity(new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS));
			}
		});
		rightButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				self.onBackPressed();
//				Context context = Look2meet.getContext();
				self.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
			}
		});


	}
}
