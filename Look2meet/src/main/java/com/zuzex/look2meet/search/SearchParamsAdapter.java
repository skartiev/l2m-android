package com.zuzex.look2meet.search;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zuzex.look2meet.R;

import java.util.List;

public class SearchParamsAdapter extends BaseAdapter {
    protected Activity activity;
    protected static LayoutInflater inflater=null;
    List<SearchParameter> params;

    public SearchParamsAdapter(Activity a, List<SearchParameter> params) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.params = params;
    }

    public int getCount() {
        return  params.size();
    }

    public Object getItem(int position) {
        return params.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        SearchParameter param = params.get(position);

        if(convertView==null) {
            vi = inflater.inflate(R.layout.search_params_item, null);
        }

        TextView tvName = (TextView)vi.findViewById(R.id.item_name);
        TextView tvValue = (TextView)vi.findViewById(R.id.item_value);

        tvName.setText(param.name);
        tvValue.setText(param.value.value);

        return vi;
    }

}
