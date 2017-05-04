package com.zuzex.look2meet.search;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

public class SearchParamSelectAdapter extends ArrayAdapter<SearchParamValue> implements Filterable {

    public ArrayList<SearchParamValue> objects;
    private SearchParamsFilter filter;

    public SearchParamSelectAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new SearchParamsFilter(objects, this);
        }
        return filter;
    }
}
