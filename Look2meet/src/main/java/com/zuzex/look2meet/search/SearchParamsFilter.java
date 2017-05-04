package com.zuzex.look2meet.search;

import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;

public class SearchParamsFilter extends Filter {

    private ArrayAdapter adapter;
    private ArrayList<SearchParamValue> sourceObjects;

    public SearchParamsFilter(ArrayList<SearchParamValue> objects, SearchParamSelectAdapter a) {
        sourceObjects = new ArrayList<SearchParamValue>();
        adapter = a;
        synchronized (this) {
            sourceObjects.addAll(objects);
        }
    }

    @Override
    protected FilterResults performFiltering(CharSequence chars) {
        String filterSeq = chars.toString().toLowerCase();
        FilterResults result = new FilterResults();
        if (filterSeq != null && filterSeq.length() > 0) {
            ArrayList<SearchParamValue> filter = new ArrayList<SearchParamValue>();

            for (SearchParamValue object : sourceObjects) {
                // the filtering itself:
                if (object.value.toLowerCase().contains(filterSeq))
                    filter.add(object);
            }
            result.count = filter.size();
            result.values = filter;
        } else {
            // add all objects
            synchronized (this) {
                result.values = sourceObjects;
                result.count = sourceObjects.size();
            }
        }
        return result;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        // NOTE: this function is *always* called from the UI thread.
        ArrayList<SearchParamValue> filtered = (ArrayList<SearchParamValue>) filterResults.values;
        adapter.notifyDataSetChanged();


        adapter.clear();
        for (int i = 0, l = filtered.size(); i < l; i++)
            adapter.add(filtered.get(i));
        adapter.notifyDataSetInvalidated();
    }
}
