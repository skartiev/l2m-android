package com.zuzex.look2meet.media.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.DataModel.UserProfileMedia;
import com.zuzex.look2meet.R;

import java.util.ArrayList;

public class FullScreenImageAdapter extends PagerAdapter {
    private Activity _activity;
    private LayoutInflater inflater;
	public ArrayList<String> filePathArray;
    private ArrayList<UserProfileMedia> media;
    private ImageLoader imageLoader;

	public FullScreenImageAdapter(Activity activity, ArrayList<UserProfileMedia> media) {
        this._activity = activity;
        this.media = media;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
	    if (filePathArray != null)
		    return  filePathArray.size();
	    else
            return media.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView image;

        inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        image = (ImageView) viewLayout.findViewById(R.id.imgDisplay);

        image.setImageResource(R.drawable.no_image);

	    if (filePathArray != null)
		    imageLoader.displayImage(filePathArray.get(position), image);
	    else
            imageLoader.displayImage(media.get(position).url, image);
        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}