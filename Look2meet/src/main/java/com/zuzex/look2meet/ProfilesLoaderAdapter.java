package com.zuzex.look2meet;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.DataModel.UserProfile;

import java.util.ArrayList;

public class ProfilesLoaderAdapter extends BaseAdapter {
    
    protected Activity activity;
    protected ArrayList<UserProfile> userProfiles;
    protected static LayoutInflater inflater=null;

    public ProfilesLoaderAdapter(Activity a, ArrayList<UserProfile> userProfiles) {
        activity = a;
        this.userProfiles = userProfiles;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
       return userProfiles.size();
    }
    @Override
    public Object getItem(int position) {
        return userProfiles.get(position);
    }

    public long getItemId(int position) {
        return userProfiles.get(position).id;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        UserProfile profile = userProfiles.get(position);
        String avatarUrl;
        String name;
        if(convertView==null) {
            vi = inflater.inflate(R.layout.profiles_list_item, null);
        }
        TextView profileName = (TextView)vi.findViewById(R.id.item_text_);
        ImageView avatarImage = (ImageView)vi.findViewById(R.id.image);
        ImageView unreadMessageImage = (ImageView)vi.findViewById(R.id.profile_unread_message_image);
        TextView unreadMessageCount = (TextView)vi.findViewById(R.id.profile_unread_message_text);

        if(profile.unreadMessages > 0) {
            unreadMessageCount.setText(String.valueOf(profile.unreadMessages));
            unreadMessageImage.setVisibility(View.VISIBLE);
        }

        if(profile.name.equals("NULLPOINTEREXCEPTION_")) {
            name = activity.getString(R.string.create_new_profile);
            avatarImage.setImageResource(R.drawable.plus);
        } else {
            avatarUrl = profile.avatarUrl;
            name = profile.name;
            ImageLoader.getInstance().displayImage(avatarUrl, avatarImage );
        }
        profileName.setText(name);
        return vi;
    }
}