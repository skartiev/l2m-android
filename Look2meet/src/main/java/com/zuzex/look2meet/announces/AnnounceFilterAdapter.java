package com.zuzex.look2meet.announces;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.GlobalHelper;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class AnnounceFilterAdapter extends BaseAdapter {

    private static final int ROW_TYPE_TEXT = 0;
    private static final int ROW_TYPE_EDITABLE_TEXT = 1;
    private static final int ROW_TYPES_COUNT = 2;

    private Activity activity;
    private AnnouncesFilter filter;
    private LayoutInflater inflater;

    private int lastFocussedPosition = -1;
    private Handler handler = new Handler();

    public AnnounceFilterAdapter(Activity a, AnnouncesFilter announceFilter) {
        activity = a;
        filter = announceFilter;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? ROW_TYPE_TEXT : ROW_TYPE_EDITABLE_TEXT;
    }

    @Override
    public int getViewTypeCount() {
        return ROW_TYPES_COUNT;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int i) {
        return "";
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            switch (i) {
                case 0: view = inflater.inflate(R.layout.announces_filter_cell, null); break;
                default: view = inflater.inflate(R.layout.search_params_item, null); break;
            }
        }

        TextView tvName = (TextView)view.findViewById(R.id.item_name);

        switch (i) {
            case 0: {
                tvName.setText(activity.getString(R.string.announes_filter_title));
                final EditText tvValue = (EditText)view.findViewById(R.id.item_value);
                tvValue.setText(filter.title);
//                tvValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//
//                    @Override
//                    public void onFocusChange(View v, boolean hasFocus) {
//                        if (hasFocus) {
//                            handler.postDelayed(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    if (lastFocussedPosition == -1 || lastFocussedPosition == i) {
//                                        lastFocussedPosition = i;
//                                        tvValue.requestFocus();
//                                    }
//                                }
//                            }, 200);
//                        } else {
//                            lastFocussedPosition = -1;
//                        }
//                    }
//                });

                tvValue.setOnEditorActionListener(new TextView.OnEditorActionListener(){
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if(actionId == EditorInfo.IME_ACTION_DONE) {
                            filter.title = tvValue.getText().toString();
                            tvValue.clearFocus();

                            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        }

                        return false;
                    }
                });

                break;
            }
            case 1: {
                TextView tvValue = (TextView)view.findViewById(R.id.item_value);
                tvName.setText(activity.getString(R.string.announes_filter_date_from));
                tvValue.setText(DateToString(filter.dateFrom));
                break;
            }
            case 2: {
                TextView tvValue = (TextView)view.findViewById(R.id.item_value);
                tvName.setText(activity.getString(R.string.announes_filter_date_to));
                tvValue.setText(DateToString(filter.dateTo));
                break;
            }
            case 3: {
                TextView tvValue = (TextView)view.findViewById(R.id.item_value);
                tvName.setText(activity.getString(R.string.announes_filter_checkin_type));
                tvValue.setText(GlobalHelper.GetLocalizedCheckinType(filter.checkinType));
                break;
            }
        }

        return view;
    }

    private String DateToString(long timestamp) {
        Date date = new Date(timestamp*1000);
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        return df.format(date);
    }
}
