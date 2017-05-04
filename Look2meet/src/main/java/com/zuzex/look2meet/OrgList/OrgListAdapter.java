package com.zuzex.look2meet.OrgList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.DataModel.Organization;
import com.zuzex.look2meet.MapActivity;
import com.zuzex.look2meet.R;
import com.zuzex.look2meet.api.GlobalHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrgListAdapter extends BaseAdapter implements Filterable {

    private List<Organization> orgList;
    private OrgListActivity activity;
    private LayoutInflater inflater;
    public List<Organization> filteredList;

    public OrgListAdapter(OrgListActivity a, List<Organization> organizations) {
        filteredList = organizations;
        orgList = organizations;
        activity = a;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return orgList.size();
    }

    @Override
    public Object getItem(int i) {
        return orgList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return orgList.get(i).id;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View vi = view;
        if (view == null) {
            if (activity.getListType().equals("checkins")) {
                vi = inflater.inflate(R.layout.activity_checkin_cell, null);
            } else {
                vi = inflater.inflate(R.layout.activity_organization_cell, null);
            }
        }

        Organization org = orgList.get(i);

        TextView tvName = (TextView) vi.findViewById(R.id.textViewName);
        ImageView image = (ImageView) vi.findViewById(R.id.imageViewOrgAvatar);
        ImageView topMark = (ImageView) vi.findViewById(R.id.organization_cell_top_mark);
        TextView tvType = (TextView) vi.findViewById(R.id.textViewType);
        TextView allPeopleCount = (TextView) vi.findViewById(R.id.organization_cell_all_count);
        allPeopleCount.setText(String.valueOf(org.maleCountNow+org.femaleCountNow));
        if (activity.getListType().equals("checkins")) {
            TextView tvCheckinType = (TextView) vi.findViewById(R.id.checkin_type);
            TextView tvCheckinTime = (TextView) vi.findViewById(R.id.checkin_time);
            long curTimestamp = System.currentTimeMillis() / 1000;
            Date remainTime = new Date((org.checkinExpireLeft) * 1000);
            DateFormat df = new SimpleDateFormat("HH:mm:ss");

            tvCheckinType.setText(GlobalHelper.GetLocalizedCheckinType(org.checkinType));
            tvCheckinTime.setText(activity.getString(R.string.checkin_remain)+" "+df.format(remainTime));
        } else {

            TextView tvWorkTime = (TextView) vi.findViewById(R.id.textViewWorkTime);
            TextView tvAddress = (TextView) vi.findViewById(R.id.textViewAddress);
            TextView tvAnnounce = (TextView) vi.findViewById(R.id.textViewAnnounce);
            TextView tvLikesCount = (TextView) vi.findViewById(R.id.organization_cell_likes_count);
            TextView tvMenCount = (TextView) vi.findViewById(R.id.organization_cell_men_count);
            TextView tvWomenCount = (TextView) vi.findViewById(R.id.organization_cell_women_count);
            if (activity.getListType().equals("temp_object")) {
                ImageView onMap = (ImageView) vi.findViewById(R.id.button3);
                onMap.setVisibility(View.GONE);
                TextView onMapText = (TextView) vi.findViewById(R.id.on_map_text);
                onMapText.setText("Изменить");
            }

            tvType.setText(org.categoryName);
            tvWorkTime.setText(Html.fromHtml("<b>" + activity.getString(R.string.orglist_cell_work_time) + "</b>"+"&nbsp;"+ org.workTime));
            tvAddress.setText(Html.fromHtml("<b>" + activity.getString(R.string.orglist_cell_address) + "</b>" +"&nbsp;"+org.address));

            if(!org.announceShort.isEmpty())
                tvAnnounce.setText(Html.fromHtml("<b>" + activity.getString(R.string.orglist_cell_announce) + "</b>" +"&nbsp;"+org.announceShort));
            tvLikesCount.setText(String.valueOf(org.likesCount));
            tvMenCount.setText(String.valueOf(org.maleCountAll));
            tvWomenCount.setText(String.valueOf(org.femaleCountAll));
        }
        String name = org.name.toUpperCase();
        tvName.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvName.setText(name);
        ImageLoader.getInstance().displayImage(org.avatarUrl, image);

        if (org.isTop) {
            topMark.setVisibility(View.VISIBLE);
        } else {
            topMark.setVisibility(View.INVISIBLE);
        }

        LinearLayout showOnMap = (LinearLayout) vi.findViewById(R.id.show_on_map_layout);

        showOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Organization org = orgList.get(i);
                if (activity.getListType().equals("temp_object")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://look2meet.com/object/edit?id=" +  String.valueOf(org.id)));
                    activity.startActivity(browserIntent);
                } else {
                    Intent intent = new Intent(activity, MapActivity.class);
                    intent.putExtra("object_id", org.id);
                    activity.startActivity(intent);
                }
            }
        });
        return vi;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults oReturn = new FilterResults();
                filteredList = new ArrayList<Organization>();
                if (activity.orgList != null) {
                    if (charSequence != null) {
                        for (int i = 0; i < activity.orgList.size(); i++) {
                            Organization organization = activity.orgList.get(i);
                            String name = organization.name;
                            if (name.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                                filteredList.add(organization);
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
                orgList = (ArrayList<Organization>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}

