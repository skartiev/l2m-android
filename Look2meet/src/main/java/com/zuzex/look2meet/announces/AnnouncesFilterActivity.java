package com.zuzex.look2meet.announces;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.yandex.metrica.YandexMetrica;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.GlobalHelper;

import java.util.Calendar;

public class AnnouncesFilterActivity extends FragmentActivity implements DatePickerDialog.OnDateSetListener, AdapterView.OnItemClickListener {

    private AnnouncesFilter filter;
    private AnnounceFilterAdapter adapter;
    private LayoutInflater inflater;
    private int selectedItem = -1;
    private String[] checkinTypes;

    private ListView lvFilterItems;
    private DialogFragment datePickerFragment;
    private EditText etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announces_filter);
        getActionBar().hide();

        Intent intent = getIntent();
        if(intent.hasExtra("announce_filter")) {
            filter = intent.getParcelableExtra("announce_filter");
        } else {
            filter = new AnnouncesFilter(0,0);
        }

        adapter = new AnnounceFilterAdapter(this, filter);
        datePickerFragment = new DatePickerFragment();
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        checkinTypes = new String[] {getString(R.string.checkin_type_all), getString(R.string.checkin_type_now), getString(R.string.checkin_type_soon), getString(R.string.checkin_type_plan)};

        lvFilterItems = (ListView)findViewById(R.id.announes_filter_list);
        lvFilterItems.setOnItemClickListener(this);
        lvFilterItems.setItemsCanFocus(true);
//        lvFilterItems.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        lvFilterItems.setAdapter(adapter);
    }

    public void BackClicked(View v) {
        CancelActivity();
    }

    public void OkClicked(View v) {
        Intent intent = new Intent();
        intent.putExtra("announce_filter", filter);

        setResult(RESULT_OK, intent);
        finish();
    }

    private void CancelActivity() {
        Intent intent = new Intent();
//        StoreItems(intent);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        selectedItem = position;

        if(position == 0) {
            lvFilterItems.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            View cell = inflater.inflate(R.layout.announces_filter_cell, null);
            EditText ed = (EditText)cell.findViewById(R.id.item_value);
            ed.requestFocus();
        } else if(position == 1) {
            datePickerFragment.show(getSupportFragmentManager(), "dateFrom");
        } else if(position == 2) {
            datePickerFragment.show(getSupportFragmentManager(), "dateTo");
        } else if(position == 3) {
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
            new AlertDialog.Builder(this).setMessage(getString(R.string.announes_filter_checkin_type)).setView(pickerView).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    filter.checkinType = GlobalHelper.GetCheckinType(checkinTypes[picker.getValue()]);
                    adapter.notifyDataSetChanged();
                }
            }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create().show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if(selectedItem == 1) {
            filter.dateFrom = TimestampFromDate(year, month, day);
        } else if(selectedItem == 2) {
            filter.dateTo = TimestampFromDate(year, month, day);
        }

        adapter.notifyDataSetChanged();
    }

    private long TimestampFromDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTimeInMillis()/1000;
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
}