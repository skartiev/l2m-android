package com.zuzex.look2meet;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.DataModel.Gift;

import java.util.ArrayList;

/**
 * Created by sanchirkartiev on 18.07.14.
 */
public class GiftLoaderAdapter extends BaseAdapter{
    private Activity activity;
    ArrayList<Gift> gifts;
    protected static LayoutInflater inflater=null;

    public GiftLoaderAdapter(Activity a, ArrayList<Gift> list)
    {
        this.activity = a;
        gifts = list;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return gifts.size();
    }

    @Override
    public Object getItem(int i) {
        return gifts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;

        if(view==null)
            vi = inflater.inflate(R.layout.justimage, null);
        Gift gift = gifts.get(i);
        ImageView imageView = (ImageView) vi.findViewById(R.id.imageViewJustImageGiftsOK);
        String url = gift.url;
        ImageLoader.getInstance().displayImage(url,imageView);
        return vi;
    }
}
