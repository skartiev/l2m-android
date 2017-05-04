package com.zuzex.look2meet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zuzex.look2meet.DataModel.Organization;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.OrgList.OrgListActivity;
import com.zuzex.look2meet.PushNotifications.GcmHelper;
import com.zuzex.look2meet.chat.ChatMessage;
import com.zuzex.look2meet.search.SearchParamsActivity;
import com.zuzex.look2meet.socket.SocketWorker;
import com.zuzex.look2meet.utils.AnimationPreloader;

import java.util.ArrayList;


public class MenuActivity extends FragmentActivity implements PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_ORGANISATION_INFO = 0;
    private BroadcastReceiver unreadMessageReceiver;
    public AnimationPreloader preloader;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preloader = new AnimationPreloader(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        unreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra("message")) {
                    ChatMessage message_received;
                    message_received = (ChatMessage) intent.getSerializableExtra("message");
                    int incomingId = new Integer(message_received.idUser);
                    if(incomingId != UserProfile.getInstance().id) {
                        UserProfile.getInstance().unreadMessages+=1;
                    }
                }
                else if(intent.hasExtra("unread_messages_count")) {
                    int unreadMessages = intent.getIntExtra("unread_messages_count", 0);
                    UserProfile.getInstance().unreadMessages = unreadMessages;
                }
                showUnreadMessages(UserProfile.getInstance().unreadMessages);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(unreadMessageReceiver, new IntentFilter("reciveChatMessage"));
    }

    @Override
    public void onResume() {
        super.onResume();
        showUnreadMessages(UserProfile.getInstance().unreadMessages);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(unreadMessageReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_ORGANISATION_INFO) {
                if (data.hasExtra("organization_list")) {
                    ArrayList<Organization> organizations = data.getParcelableArrayListExtra("organization_list");
                    Intent intent = getIntent();
                    intent.putParcelableArrayListExtra("organization_list", organizations);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    protected void loadMapActivity() {
        Intent mapIntent = new Intent(getBaseContext(), MapActivity.class);
        startActivity(mapIntent);
    }
    protected void loadLookingForActivity() {
        Intent lookingIntent = new Intent(getBaseContext(), LookingForActivity.class);
        startActivityForResult(lookingIntent, 0);
    }

    public void openMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void searchClicked(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.search_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.search_menu_item_top: {
                        Intent intent = new Intent(getBaseContext(), OrgListActivity.class);
                        intent.putExtra("list_type", "top");
                        startActivityForResult(intent, 0);
                        break;
                    }
                    case R.id.search_menu_item_search: {
                        Intent lookingIntent = new Intent(getBaseContext(), SearchParamsActivity.class);
                        startActivityForResult(lookingIntent, 0);
                        break;
                    }
                    case R.id.search_menu_item_create_temp_object: {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://look2meet.com/object/add"));
                        startActivity(browserIntent);
                    }
                }
                return true;
            }
        });
        popup.show();
    }
    public void friendsClicked(View v)
    {
        /*Intent intent = new Intent(getBaseContext(), FriendsActivity.class);
        startActivity(intent);*/
        Intent intent = new Intent(getBaseContext(),UsersList.class);
        intent.putExtra("curr_list", "FriendsList");
        startActivity(intent);
    }

    public void announcesClicked(View v)
    {
        Intent intent = new Intent(getBaseContext(), OrgListActivity.class);
        intent.putExtra("list_type", "announces");
        startActivityForResult(intent, 0);
    }

    public void profilesClicked(View v)
    {
        Intent intent = new Intent(getBaseContext(), ProfilesActivity.class);
        SocketWorker.getInstance().disconnect();
        intent.putExtra("ACTIVE", 1);
        startActivity(intent);
    }

    public void FavoritesClicked(View v)
    {
        final Context context = this;
        Intent intent = new Intent(context, OrgListActivity.class);
        intent.putExtra("list_type", "favorites");
        startActivityForResult(intent, REQUEST_ORGANISATION_INFO);
    }
    
    public void checkInCliked(View v) {
        final Context context = this;
        Intent intent = new Intent(context, OrgListActivity.class);
        intent.putExtra("list_type", "checkins");
        startActivity(intent);
    }
    public void chatClicked(View v) {
        final Context context = this;
        Intent intent = new Intent(context, UsersList.class);
        intent.putExtra("curr_list", "ChatList");
        startActivity(intent);
    }

    public void BackClicked(View v) {
        onBackPressed();
    }

    private void showMap(){
        Intent mapIntent = new Intent(MenuActivity.this, MapActivity.class);
        startActivity(mapIntent);
    }

    public void showMyProfile() {
        Intent profileIntent = new Intent(getBaseContext(), MyProfileActivity.class);
        startActivity(profileIntent);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_main:
                showMap();
                return true;
            case R.id.menu_myProfile: {
                showMyProfile();
                return true;
            }
            case R.id.menu_settings: {
                Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
            case R.id.menu_myTemp: {
                Intent tempIntent = new Intent(getBaseContext(),OrgListActivity.class);
                tempIntent.putExtra("list_type", "temp_object");
                startActivity(tempIntent);
                return true;
            }
            case R.id.menu_history: {
                final Context context = this;
                Intent intent = new Intent(context, UsersList.class);
                intent.putExtra("curr_list", "ChatList");
                startActivity(intent);
                return true;
            }
            case R.id.menu_search: {
                Intent RegisterIntent = new Intent(getBaseContext(), LookingForActivity.class);
                startActivityForResult(RegisterIntent, 0);
                return true;
            }
            case R.id.menu_exit: {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                SharedPreferences.Editor e = preferences.edit();
                e.putString("apiToken", "");
                e.putString("apiTokenBackup", "");
                e.commit();
                GcmHelper gcmHelper = new GcmHelper(getApplicationContext());
                gcmHelper.removeRegistrationId();
                startActivity(intent);
                finish();
                return true;
            }
            case R.id.menu_balans: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://look2meet.com/user/profile"));
                startActivity(browserIntent);
                return true;
            }
            case R.id.menu_admin: {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{UserProfile.getInstance().adminEmail});
                email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_theme));

	            String about = new String();
	            about += getString(R.string.message_template1) + " " + UserProfile.getInstance().name;
	            about += "\n\n(" + getString(R.string.message_template2) + ")\n\n\n\n\n";
	            about += getString(R.string.message_template3) + " " + Build.VERSION.RELEASE;
                PackageManager manager = this.getPackageManager();
                try {
                    PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                    about +="\n" + getString(R.string.build);
                    about += info.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                about += "\n" + getDeviceName() + "\n";
                String emails = preferences.getString("email","");
                about += "\n"+"Email: "+emails+"\n";
                email.putExtra(Intent.EXTRA_TEXT, about);
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, getString(R.string.choose_email_client)));
                return true;
            }
        }
        return true;
    }

    void showUnreadMessages(int messages) {
        if(messages >= 0) {
            try {
                RelativeLayout bottomLayout = (RelativeLayout) findViewById(R.id.menu_button_chat);
                TextView unreadMessage = (TextView) bottomLayout.findViewById(R.id.menu_unread_messages_my_profile);
                if(messages > 1000)
                    unreadMessage.setText("999");
                else if(messages == 0) {
                    unreadMessage.setText("");
                } else {
                    unreadMessage.setText(String.valueOf(messages));
                }
            } catch (Exception e) {
//                Log.w("MainMenu", e.toString());
            }
        }
    }

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}
	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void startActivityForResult(Intent intent, int code) {
       super.startActivityForResult(intent, code);
       overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void onPause() {
        super.onPause();
        preloader.close();
    }
}
