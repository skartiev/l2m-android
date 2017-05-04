package com.zuzex.look2meet;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zuzex.look2meet.DataModel.UserProfile;

import java.util.List;

/**
 * Created by dgureev on 9/29/14.
 */
public class ProfileOrganizationAdapter extends ArrayAdapter<UserProfile.Checkin> {
    private final LayoutInflater inflater;

    public ProfileOrganizationAdapter(Context context, int resource, List<UserProfile.Checkin> objects) {
        super(context, resource, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi;
            if(convertView == null) {
                vi = inflater.inflate(R.layout.userprofile_checkins, parent, false);
            } else {
                vi = convertView;
            }
        TextView checkin_organisation = (TextView) vi.findViewById(R.id.profile_organisation);
        checkin_organisation.setText(getItem(position).object_name);
        checkin_organisation.setPaintFlags(checkin_organisation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        TextView checkin_type_text = (TextView) vi.findViewById(R.id.profile_checkin_type);
        String checkin_type = getItem(position).type;
        String translated_checkin_type = "";
        if(checkin_type.equals(UserProfile.CHECKIN_TYPE_NOW)) {
            translated_checkin_type = getContext().getString(R.string.checkin_type_now);
        } else if(checkin_type.equals(UserProfile.CHECKIN_TYPE_PLAN)) {
            translated_checkin_type = getContext().getString(R.string.checkin_type_plan);
        }else if(checkin_type.equals(UserProfile.CHECKIN_TYPE_SOON)) {
            translated_checkin_type = getContext().getString(R.string.checkin_type_soon);
        }
        checkin_type_text.setText(translated_checkin_type);
        return vi;
    }

}
