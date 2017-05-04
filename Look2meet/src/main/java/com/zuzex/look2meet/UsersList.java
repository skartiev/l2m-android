package com.zuzex.look2meet;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.DataModel.DialogObject;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.chat.ChatActivity;
import com.zuzex.look2meet.chat.ChatMessage;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UsersList extends MenuActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, SearchView.OnQueryTextListener, AdapterView.OnItemSelectedListener {
    ListView view;
    UsersListLoaderAdapter adapter;
    String type;
    String name;
    String address;
    String checkin_type;
    String curr_list;
    int id;
    private SharedPreferences preferences;
    protected ArrayList<DialogObject> users;
    static final private int CHOOSE_FILTER = 0;
    private int retryRequestError = 0;
    private static int LIST_ITEMS_PER_PAGE = 20;
    private Boolean isLoadingData;
    private Boolean isNeedMore;
    private Boolean isFiltered;
    private Integer loadedPage;
    TextView tvFilterWarn;
    private BroadcastReceiver unreadMessageReceiver;
    private SearchView searchView;
    private boolean isReloading;

    private String filterSex;
    private String filterCheckin;
    private Integer filterAgeFrom;
    private Integer filterAgeTo;
    private LinearLayout organizationTextLayout;
    public String getType() {return type;}
    public String getCurr_list() {return curr_list;}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setContentView(R.layout.activity_users_list);
        getActionBar().hide();
        view = new ListView(this);
        users = new ArrayList<DialogObject>();
        type = "";
        address = "";
        name = "";
        checkin_type = "";
        loadedPage = 1;
        isNeedMore = false;
        isFiltered = false;
        isReloading = false;
        filterSex = "";
        filterCheckin = "";
        filterAgeFrom = 0;
        filterAgeTo = 0;
        tvFilterWarn = (TextView)findViewById(R.id.filter_results_warning);
        view = (ListView) findViewById(R.id.listViewUsersList);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.searchViewTop);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        organizationTextLayout = (LinearLayout) findViewById(R.id.userlist_text_layout);
        unreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.hasExtra("message")) {
                    ChatMessage message_received;
                    message_received = (ChatMessage) intent.getSerializableExtra("message");
                    Integer incomingId = new Integer(message_received.idUser);
                    ArrayList<Integer> usersIds = new ArrayList<Integer>();
                    for(int i = 0; i<users.size(); i++) {
                        if(incomingId.equals(users.get(i).id)) {
                            users.get(i).unreadMessages+=1;
                            users.get(i).date = message_received.date;
                            incrUnreadMessages(users.get(i).id);
                            usersIds.add(users.get(i).id);
                        }
                    }
                    if(!usersIds.contains(incomingId)) {
                        loadData(1);
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIntent(getIntent());
        LocalBroadcastManager.getInstance(this).registerReceiver(unreadMessageReceiver, new IntentFilter("reciveChatMessage"));
        users.clear();
        loadData(1);
        YandexMetrica.onResumeActivity(this);
    }

    public void checkIntent(Intent intent) {
        LinearLayout topBarOld = (LinearLayout) findViewById(R.id.top_menu);
        LinearLayout topMenuParent = (LinearLayout) findViewById(R.id.top_menu_parent);
        topBarOld.setVisibility(View.VISIBLE);
        topMenuParent.setVisibility(View.GONE);
        curr_list = intent.getStringExtra("curr_list");
        TextView textViewTop = (TextView) findViewById(R.id.textViewTop);
        adapter = new UsersListLoaderAdapter(this, users);
        view.setAdapter(adapter);
        view.setOnItemClickListener(this);
        view.setOnItemLongClickListener(this);
        view.setOnItemSelectedListener(this);

        try {
            if (curr_list.equals("OrgList")) {
                organizationTextLayout.setVisibility(View.VISIBLE);
                type = intent.getStringExtra("category_name");
                id = intent.getIntExtra("id", 0);
                TextView typeView = (TextView) findViewById(R.id.textViewHeaderType);
                typeView.setText(type.toUpperCase());
                address = intent.getStringExtra("address");
                TextView addressView = (TextView) findViewById(R.id.textViewHeaderAddress);
                addressView.setText(address);
                name = intent.getStringExtra("name");
                checkin_type = intent.getStringExtra("type");
                TextView nameView = (TextView) findViewById(R.id.textViewHeaderName);
                textViewTop.setText(name);
                if(name.equals("false")) {
                    nameView.setText("Не указан");
                }
                else {
                    nameView.setText(name);
                }
            } else if (curr_list.equals("BlackList")) {
                textViewTop.setText(R.string.user_list_black);
                removeFromListView(searchView, intent);
            } else if (curr_list.equals("WhiteList")) {
                textViewTop.setText(R.string.user_list_white);
                removeFromListView(searchView, intent);
            } else if (curr_list.equals("FriendsList")) {
                textViewTop.setText(R.string.user_list_friends);
                removeFromListView(searchView, intent);
            } else if (curr_list.equals("MaskList")) {
                textViewTop.setText(R.string.user_list_mask);
                removeFromListView(searchView, intent);
            } else if (curr_list.equals("ChatList")) {
                textViewTop.setText(R.string.dialogs);
                removeFromListView(searchView, intent);
                topBarOld.setVisibility(View.GONE);
                topMenuParent.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        view.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem == 0) {
                    isReloading = false;
                    return;
                }

                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                {
                    if(!isLoadingData && isNeedMore && !isFiltered)
                    {
                        isLoadingData = true;
                        loadedPage++;
                        loadData(loadedPage);
                    }
                }
            }
        });
    }

    private void checkListType(String listType) {

    }


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(unreadMessageReceiver);
        EasyTracker.getInstance(this).activityStop(this);
    }

    private void loadData(int page) {
        preloader.launch();
        isLoadingData = true;
        if(curr_list.equals("OrgList")) {
            if (checkin_type.equals("male_cnt_now")) {
                if(filterSex.isEmpty()) filterSex = "male";
                if(filterCheckin.isEmpty()) filterCheckin = "now";
            } else if (checkin_type.equals("female_cnt_now")) {
                if(filterSex.isEmpty()) filterSex = "female";
                if(filterCheckin.isEmpty()) filterCheckin = "now";
            } else if (checkin_type.equals("male_cnt_soon")) {
                if(filterSex.isEmpty()) filterSex = "male";
                if(filterCheckin.isEmpty()) filterCheckin = "soon";
            } else if (checkin_type.equals("female_cnt_soon")) {
                if(filterSex.isEmpty()) filterSex = "female";
                if(filterCheckin.isEmpty()) filterCheckin = "soon";
            } else if (checkin_type.equals("male_cnt_plan")) {
                if(filterSex.isEmpty()) filterSex = "male";
                if(filterCheckin.isEmpty()) filterCheckin = "plan";
            } else if (checkin_type.equals("female_cnt_plan")) {
                if(filterSex.isEmpty()) filterSex = "female";
                if(filterCheckin.isEmpty()) filterCheckin = "plan";
            } else if (checkin_type.equals("male_cnt_visitors")) {
                if(filterSex.isEmpty()) filterSex = "male";
                if(filterCheckin.isEmpty()) filterCheckin = "";
            } else if (checkin_type.equals("female_cnt_visitors")) {
                if(filterSex.isEmpty()) filterSex = "female";
                if(filterCheckin.isEmpty()) filterCheckin = "";
            } else if (checkin_type.equals("cnt_now")) {
                if(filterSex.isEmpty()) filterSex = "";
                if(filterCheckin.isEmpty()) filterCheckin = "now";
            } else if (checkin_type.equals("cnt_soon")) {
                if(filterSex.isEmpty()) filterSex = "";
                if(filterCheckin.isEmpty()) filterCheckin = "soon";
            } else if (checkin_type.equals("cnt_plan")) {
                if(filterSex.isEmpty()) filterSex = "";
                if(filterCheckin.isEmpty()) filterCheckin = "plan";
            } else if (checkin_type.equals("cnt_visitors")) {
                if(filterSex.isEmpty()) filterSex = "";
                if(filterCheckin.isEmpty()) filterCheckin = "";
            }
            Look2meetApi.getInstance().getUsersList(page, LIST_ITEMS_PER_PAGE, filterAgeFrom, filterAgeTo, filterSex, filterCheckin, id, responseHandler());
        } else if(curr_list.equals("BlackList")) {
            Look2meetApi.getInstance().getBlackList(page, LIST_ITEMS_PER_PAGE, responseHandler()) ;
        } else if(curr_list.equals("WhiteList")) {
            Look2meetApi.getInstance().getWhiteList(page, LIST_ITEMS_PER_PAGE, responseHandler());
        } else if(curr_list.equals("FriendsList")) {
            Look2meetApi.getInstance().getFriendsList(page, LIST_ITEMS_PER_PAGE, "", responseHandler());
        } else if(curr_list.equals("MaskList")) {
            Look2meetApi.getInstance().getMaskList(page, LIST_ITEMS_PER_PAGE, responseHandler());
        } else if(curr_list.equals("ChatList")) {
            Look2meetApi.getInstance().getDialogs(page, LIST_ITEMS_PER_PAGE, responseHandler());
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
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
            }
        };
    }

        public void onResponse(JSONObject response) {
            try {
                Boolean success = response.getBoolean("success");
                if (success) {
                    if (response.has("data")) {
                        JSONObject data = response.optJSONObject("data");
                        if (loadedPage == 1) {
                            users.clear();
                        }
                        JSONArray usersArray = data.optJSONArray(curr_list.equals("ChatList") ? "dialogs" : "users");
                        isNeedMore = usersArray.length() == LIST_ITEMS_PER_PAGE;
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject tempObjects = usersArray.getJSONObject(i);
                            DialogObject dialog = new DialogObject(tempObjects);
                            boolean isContains = true;
                            if (users.size() > 0) {
                                for (int u = 0; u < users.size(); u++) {
                                    if (dialog.id == users.get(u).id) {
                                        isContains = true;
                                        continue;
                                    } else {
                                        isContains = false;
                                    }
                                }
                            } else if (!dialog.isSelf) {
                                users.add(dialog);
                            }
                            if (!isContains) {
                                users.add(dialog);
                            }
                        }
                        calcUnreadMessages();
                        adapter.notifyDataSetChanged();
                        preloader.done();
                        view.setTextFilterEnabled(true);
                    } else {
                        preloader.done();
                    }
                } else {
                    String message = response.getString("message");
                    GlobalHelper.loggSend(message);
                    preloader.cancel(getString(R.string.error), message);
                }
                isLoadingData = false;
            } catch (JSONException e) {
                GlobalHelper.loggSend("JSON_EXCEPTION");
                isLoadingData = false;
                e.fillInStackTrace();
                if (retryRequestError < 10) {
                    loadData(1);
                    retryRequestError += 1;
                } else {
                    preloader.cancel(getString(R.string.error), getString(R.string.error_try_again));
                    retryRequestError = 0;
                }
            }
        }

    private void incrUnreadMessages(final int userId) {
        for(int i = 0; i<adapter.getViewTypeCount(); i++) {
            UserProfile p;
            p = (UserProfile) adapter.getItem(i);
            Integer id = new Integer(p.id);
            if(id.equals(userId)) {
                View v = view.getChildAt(i - view.getFirstVisiblePosition());
                if(v == null)
                    return;
                TextView unreadMessagesText = (TextView) v.findViewById(R.id.userlist_unread_messages_text);
                ImageView unreadMessagesImage = (ImageView) v.findViewById(R.id.userlist_unread_message_image);
                unreadMessagesImage.setVisibility(View.VISIBLE);
                unreadMessagesText.setText(String.valueOf(p.unreadMessages));
            }
        }
    }

    private JsonHttpResponseHandler jsonHttpResponseHandler() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
        };
    }

    private void removeFromListView(SearchView searchView, Intent intent) {
        ImageButton button = (ImageButton) findViewById(R.id.buttonFilterUsers);
        LinearLayout layoutTop = (LinearLayout) findViewById(R.id.top_menu);
        button.setVisibility(View.INVISIBLE);
        TextView typeView = (TextView) findViewById(R.id.textViewHeaderType);
        searchView.setGravity(Gravity.TOP);
        LinearLayout layout = (LinearLayout) findViewById(R.id.layoutFrom);
        address = intent.getStringExtra("address");
        TextView addressView = (TextView) findViewById(R.id.textViewHeaderAddress);
        name = intent.getStringExtra("name");
        checkin_type = intent.getStringExtra("type");
        TextView nameView = (TextView) findViewById(R.id.textViewHeaderName);
        layout.removeView(typeView);
        layout.removeView(addressView);
        layout.removeView(nameView);
    }

    public void onFilterUserClick(View v) {
        Intent filterIntent = new Intent(UsersList.this, FilterActivity.class);
        filterIntent.putExtra("SEX", filterSex);
        filterIntent.putExtra("CHECKIN", filterCheckin);
        filterIntent.putExtra("AGE", filterAgeFrom);
        filterIntent.putExtra("AGE_TO", filterAgeTo);
        startActivityForResult(filterIntent, CHOOSE_FILTER);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int i, long l) {
        if(curr_list.equals("ChatList")) {
            goToProfile(i);
        } else {
            PopupMenu menu = new PopupMenu(UsersList.this, view);
            menu.getMenuInflater().inflate(R.menu.delete, menu.getMenu());
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.goTo: {
                            goToProfile(i);
                            break;
                        }
                        case R.id.delete: {
                            int id_now = users.get(i).userFromId;
                            if (curr_list.equals("BlackList")) {
                                Look2meetApi.getInstance().deleteFromBlackList(id_now, jsonHttpResponseHandler());
                            } else if (curr_list.equals("WhiteList")) {
                                Look2meetApi.getInstance().deleteFromWhiteList(id_now, jsonHttpResponseHandler());
                            } else if (curr_list.equals("MaskList")) {
                                Look2meetApi.getInstance().deleteFromMaskList(id_now, jsonHttpResponseHandler());
                            }
                            users.remove(i);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    return true;
                }
            });
            menu.show();//showing popup menu\
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(curr_list.equals("ChatList"))
        {
            DialogObject dialogObject = users.get(i);
            dialogObject.unreadMessages = 0;
            Intent chatIntent = new Intent(getBaseContext(), ChatActivity.class);
            chatIntent.putExtra("currDialogObject", dialogObject);
            startActivity(chatIntent);
        } else {
            goToProfile(i);
        }
    }

    private void goToProfile(int i) {
        try {
            int id = adapter.filteredList.get(i).id;
            //int id = users.get(i).id;
            Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        } catch (Exception ex) {
//            Log.d("ZZZZZZZZZZZZZ", "goToProfile exception: " + ex.getMessage());
        }
    }

    public void backButtonClick(View v) {
        onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            filterSex = GlobalHelper.getGender(data.getStringExtra("SEX"));
            filterCheckin = GlobalHelper.GetCheckinType(data.getStringExtra("CHECKIN"));
            filterAgeFrom = Integer.parseInt(data.getStringExtra("AGE"));
            filterAgeTo = Integer.parseInt(data.getStringExtra("AGE_TO"));
            isReloading = false;
            //Look2meetApi.getInstance().getUsersList(1, LIST_ITEMS_PER_PAGE, filterAgeFrom, filterAgeTo, filterSex, filterCheckin, id, responseUsers, errorListener);
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
            view.clearTextFilter();
            tvFilterWarn.setVisibility(View.GONE);
        } else {
            isFiltered = true;
            view.setFilterText(s);

            if(isNeedMore) { // не все данные загружены
                tvFilterWarn.setVisibility(View.VISIBLE);
            }
        }

        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        PopupMenu menu = new PopupMenu(this,this.view);
        menu.getMenuInflater().inflate(R.menu.delete, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return true;
            }
        });

        menu.show();//showing popup menu
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void chatClicked(View v) {
        if(curr_list.equals("FriendsList")) {
            Intent intent = new Intent();
            intent.putExtra("curr_list", "ChatList");
            checkIntent(intent);
        }
        users.clear();
        loadData(1);
    }

    @Override
    public void onPause() {
        super.onPause();
        preloader.close();
        YandexMetrica.onPauseActivity(this);
    }

    public void activeSearch(View view) {
        searchView.onActionViewExpanded();
    }

    public void calcUnreadMessages() {
        if(curr_list.equals("ChatList")) {
        int unreadMessages = 0;
        for(int i = 0; i < users.size(); ++i) {
            unreadMessages += users.get(i).unreadMessages;
        }
            Intent intent = new Intent("reciveChatMessage");
            intent.putExtra("unread_messages_count", unreadMessages);
            LocalBroadcastManager.getInstance(Look2meet.getContext()).sendBroadcast(intent);
            showUnreadMessages(unreadMessages);
        }
    }
}

