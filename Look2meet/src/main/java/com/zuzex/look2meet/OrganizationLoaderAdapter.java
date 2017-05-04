package com.zuzex.look2meet;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sanchirkartiev on 24.06.14.
 */
public class OrganizationLoaderAdapter extends BaseAdapter {
    private ArrayList<Field> object;
    protected Activity activity;
    protected static LayoutInflater inflater=null;

    public OrganizationLoaderAdapter() {
        activity = new Activity();
        this.object = new ArrayList<Field>();
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public OrganizationLoaderAdapter(Activity a,ArrayList<Field> object) {
        activity = a;
        this.object = object;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return object.size();
    }

    public Object getItem(int i) {
        return i;
    }


    public long getItemId(int i) {
        return i;
    }


    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi=view;
            if(view==null)
                vi = inflater.inflate(R.layout.listitem, null);
        Field field = object.get(i);
        TextView firstField = (TextView) vi.findViewById(R.id.textViewFirstItem);
        TextView secondField = (TextView) vi.findViewById(R.id.textViewSecondItem);
        firstField.setText(field.firstField);
        secondField.setText(field.secondField);
        return vi;

    }
}

