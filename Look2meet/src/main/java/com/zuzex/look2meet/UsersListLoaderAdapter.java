package com.zuzex.look2meet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.DataModel.DialogObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by sanchirkartiev on 27.06.14.
 */
public class UsersListLoaderAdapter extends BaseAdapter implements Filterable {
    private UsersList activity;
    private ArrayList<DialogObject> profiles = new ArrayList<DialogObject>();
    private LayoutInflater inflater;

    public ArrayList<DialogObject> filteredList;

    public UsersListLoaderAdapter(UsersList userList, ArrayList<DialogObject> profiles)
    {
        filteredList = new ArrayList<DialogObject>(profiles);
        this.activity = userList;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.profiles = profiles;
    }

    @Override
    public int getCount() {
        return profiles.size();
    }

    @Override
    public Object getItem(int i) {
        return profiles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return profiles.get(i).id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;
        DialogObject dialogObject = profiles.get(i);
        String name = dialogObject.name;
        String avatarUrl = dialogObject.avatarUrl;
        String cityName  = dialogObject.city_name;
        int age = dialogObject.age;
        boolean isFriend = dialogObject.isFavorite;
        boolean isOnline = dialogObject.isOnline;
        boolean isVip = dialogObject.isVip;

        if(view==null)
            vi = inflater.inflate(R.layout.activity_users_list_cell, null);
        TextView textViewStatus = (TextView) vi.findViewById(R.id.textViewLabelStatus);
        ImageView checkin = (ImageView) vi.findViewById(R.id.imageViewCheckin);
        textViewStatus.setText(dialogObject.getLocaleCheckinType(activity));
        if(activity.getCurr_list().equals("FriendsList")) {
            textViewStatus.setVisibility(View.INVISIBLE);
            checkin.setVisibility(View.INVISIBLE);
        }
        else if(activity.getCurr_list().equals("BlackList")) {
            textViewStatus.setVisibility(View.INVISIBLE);
            checkin.setVisibility(View.INVISIBLE);
        }

        TextView textViewUsersList = (TextView)  vi.findViewById(R.id.textViewUsersList);
        TextView textViewCityNameUsersList = (TextView) vi.findViewById(R.id.textViewCityName);
        textViewCityNameUsersList.setText(cityName);
        textViewUsersList.setText(name);
        TextView ageText = (TextView) vi.findViewById(R.id.textViewMarker);
        ageText.setText(String.valueOf(age));
        ImageView imageIsFriend = (ImageView) vi.findViewById(R.id.imageViewIsFriend);
        if(!isFriend) {
            imageIsFriend.setVisibility(View.INVISIBLE);
        }
        ImageView imageCurrent = (ImageView) vi.findViewById(R.id.imageViewCurrent);

        if(isOnline) {
            imageCurrent.setImageResource(R.drawable.mark_online_green);
        } else {
            imageCurrent.setImageResource(R.drawable.mark_online_red);
        }

        ImageView unreadMessage = (ImageView)vi.findViewById(R.id.userlist_unread_message_image);
        TextView unreadMessageText = (TextView)vi.findViewById(R.id.userlist_unread_messages_text);
        if(dialogObject.unreadMessages>0) {
            unreadMessage.setVisibility(View.VISIBLE);
            unreadMessageText.setText(String.valueOf(dialogObject.unreadMessages));
        } else {
            unreadMessage.setVisibility(View.INVISIBLE);
            unreadMessageText.setText("");
        }

        ImageView image=(ImageView)vi.findViewById(R.id.imageViewUsersList);
        ImageLoader.getInstance().displayImage(avatarUrl, image);
        TextView textViewTitle = (TextView) vi.findViewById(R.id.textViewTitleForDialogs);
        if(activity.getCurr_list().equals("ChatList")) {
            textViewStatus.setVisibility(View.INVISIBLE);
            checkin.setVisibility(View.INVISIBLE);
            textViewTitle.setVisibility(View.INVISIBLE);
        }

        ImageView vip = (ImageView) vi.findViewById(R.id.vipIcon);
            if(isVip) {
                vip.setVisibility(View.VISIBLE);
            } else {
                vip.setVisibility(View.INVISIBLE);
            }
        return vi;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults oReturn = new FilterResults();
                filteredList = new ArrayList<DialogObject>();
                if (profiles!= null)
                {
                    if (charSequence != null) {
                        for (int i = 0; i < activity.users.size(); i++) {
                            DialogObject profile = activity.users.get(i);
                            String name = profile.name;
                            if (name.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                                filteredList.add(profile);
                            }
                        }
                        oReturn.values = filteredList;
                    }
                }
                return oReturn;
            }
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                profiles = (ArrayList<DialogObject>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    private void sortByMessages() {
        Comparator<DialogObject> myComparator = new Comparator<DialogObject>() {
            public int compare(DialogObject obj1, DialogObject obj2) {
//                return obj1.date.compareTo(obj2.date);
                return obj2.unreadMessages - obj1.unreadMessages;
            }
        };
        Collections.sort(profiles, myComparator);
    }

    @Override
    public void notifyDataSetChanged() {
        sortByMessages();
        filteredList = new ArrayList<DialogObject>(profiles);
        super.notifyDataSetChanged();
    }
}

