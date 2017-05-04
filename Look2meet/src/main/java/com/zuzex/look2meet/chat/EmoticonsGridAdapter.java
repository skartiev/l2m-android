package com.zuzex.look2meet.chat;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.R;

import java.io.InputStream;
import java.util.ArrayList;

public class EmoticonsGridAdapter extends BaseAdapter {

    private ArrayList<Emoticon> paths;
    private int pageNumber;
    Context mContext;

    KeyClickListener mListener;

    public EmoticonsGridAdapter(Context context, ArrayList<Emoticon> paths, int pageNumber, KeyClickListener listener) {
        this.mContext = context;
        this.paths = paths;
        this.pageNumber = pageNumber;
        this.mListener = listener;
    }
    public View getView(int position, View convertView, ViewGroup parent){

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.emoticons_item, null);
        }

        final Emoticon emoticon = paths.get(position);

        final ImageView image = (ImageView) v.findViewById(R.id.item);
        ImageLoader.getInstance().displayImage(emoticon.url, image);

        image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.keyClickedIndex(emoticon);
            }
        });

        return v;
    }

    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public Emoticon getItem(int position) {
        return paths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getPageNumber () {
        return pageNumber;
    }

    private Bitmap getImage (String path) {
        AssetManager mngr = mContext.getAssets();
        InputStream in = null;

        try {
            in = mngr.open("emoticons/" + path);
        } catch (Exception e){
            e.printStackTrace();
        }

        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = chunks;

        Bitmap temp = BitmapFactory.decodeStream(in, null, null);
        return temp;
    }

    public interface KeyClickListener {

        public void keyClickedIndex(Emoticon emoticon);
    }
}