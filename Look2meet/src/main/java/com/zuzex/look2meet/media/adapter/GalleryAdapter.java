package com.zuzex.look2meet.media.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.DataModel.IUpdateStatus;
import com.zuzex.look2meet.DataModel.UserProfile;
import com.zuzex.look2meet.DataModel.UserProfileMedia;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.Look2meetApi;
import com.zuzex.look2meet.media.FullScreenViewActivity;
import com.zuzex.look2meet.utils.AnimationPreloader;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dgureev on 7/2/14.
 */
public class GalleryAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    private Context context;
    private AnimationPreloader preloader;
    private int DEFAULT_VIEW_TYPE = 0;
    public static int VIEW_TYPE_PHOTO = 0;
    public static int VIEW_TYPE_VIDEO = 1;
    private boolean isDeleteEnable = false;
    private boolean isMyProfile = false;
    private ArrayList<UserProfileMedia> media;

    public void toggleDelete() {
        isDeleteEnable = !isDeleteEnable;
    }

    public GalleryAdapter(Context context, ArrayList<UserProfileMedia> media, int viewType, boolean isMyProfile) {
        preloader = new AnimationPreloader(context);
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.DEFAULT_VIEW_TYPE = viewType;
        this.media = media;
        this.isMyProfile = isMyProfile;
    }

    @Override
    public int getCount() {
            return media.size();
    }

    @Override
    public Object getItem(int i)
    {
        return media.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return media.get(i).id;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View v = view;
        ImageView image;
        ImageView deleteButton;
        final UserProfileMedia item = (UserProfileMedia)getItem(i);
        if(v == null) {
            v = inflater.inflate(R.layout.image_gallery_gridview_item, viewGroup, false);
            v.setTag(R.id.image_gallery_image_view, v.findViewById(R.id.image_gallery_image_view));
            v.setTag(R.id.image_gallery_delete_button, v.findViewById(R.id.image_gallery_delete_button));
        }

        deleteButton = (ImageView) v.findViewById(R.id.image_gallery_delete_button);
        if(isDeleteEnable) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preloader.launch();
                UserProfile.getInstance().deleteFile(item.id);
                Look2meetApi.getInstance().updateProfile(UserProfile.getInstance(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        UserProfile.getInstance().reload(new IUpdateStatus() {
                            @Override
                            public void onUpdateSuccess(JSONObject response) {
                                if(DEFAULT_VIEW_TYPE == VIEW_TYPE_PHOTO) {
                                    media = UserProfile.getInstance().photos;
                                } else if(DEFAULT_VIEW_TYPE == VIEW_TYPE_VIDEO) {
                                    media = UserProfile.getInstance().video;
                                }
                                preloader.done();
                                notifyDataSetChanged();
                            }
                            @Override
                            public void onUpdateError() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        notifyDataSetChanged();
                    }
                });
            }
        });

        image = (ImageView)v.findViewById(R.id.image_gallery_image_view);
        image.setImageResource(R.drawable.no_image);
        ImageLoader.getInstance().displayImage(item.preview, image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DEFAULT_VIEW_TYPE == VIEW_TYPE_PHOTO) {
                    Intent fullScreenActivity = new Intent(context, FullScreenViewActivity.class);
                    fullScreenActivity.putExtra("position", i);
                    fullScreenActivity.putParcelableArrayListExtra("media", media);
                    context.startActivity(fullScreenActivity);
                } else {
                    if(item.url != null && !item.url.equals("")) {
                        Uri videoUri = Uri.parse(item.url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
                        context.startActivity(intent);
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void notifyDataSetChanged () {
        super.notifyDataSetChanged();
        if(isMyProfile) {
            if (DEFAULT_VIEW_TYPE == VIEW_TYPE_PHOTO) {
                media = UserProfile.getInstance().photos;
            } else if (DEFAULT_VIEW_TYPE == VIEW_TYPE_VIDEO) {
                media = UserProfile.getInstance().video;
            }
        }
    }
}