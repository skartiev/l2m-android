package com.zuzex.look2meet.announces;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zuzex.look2meet.DataModel.Organization;
import com.zuzex.look2meet.OrganisationActivity;
import com.zuzex.look2meet.R;

import org.apache.commons.lang.StringUtils;

public class AnnouncePopup {

    private Activity activity;
    private PopupWindow popup;
    private Point displaySize;
    private Organization curOrg;

    public AnnouncePopup(Activity act) {

        activity = act;

        Display display = activity.getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        LayoutInflater layoutInflater = (LayoutInflater) activity.getBaseContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.announce_popup, null);
        popupView.setOnClickListener(popupClickListener);
        popup = new PopupWindow(popupView,displaySize.x,dp2px(130));
        popup.setAnimationStyle(R.style.PopupWindowAnimation);
        popup.setClippingEnabled(false);
    }

    private View.OnClickListener popupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(activity, OrganisationActivity.class);
            intent.putExtra("mapObjectID", curOrg.id);
            intent.putExtra("org_object", curOrg);
            activity.startActivityForResult(intent, 0);
        }
    };

    public void show(Organization org) {
        if(activity != null && !popup.isShowing()) {
            popup.showAsDropDown(activity.findViewById(R.id.bottom_layout), 0, 0);
        }

        curOrg = org;

        View contentView = popup.getContentView();
        ImageView orgAvatar = (ImageView)contentView.findViewById(R.id.org_avatar);
        TextView orgName = (TextView)contentView.findViewById(R.id.org_name);
        TextView orgAnnounce = (TextView)contentView.findViewById(R.id.org_announce);

        ImageLoader.getInstance().displayImage(org.avatarUrl, orgAvatar);
        orgName.setText(org.name);
        orgAnnounce.setText(StringUtils.abbreviate(org.announceShort, 153));
    }

    public void Hide() {
        if(popup.isShowing()) {
            popup.dismiss();
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, activity.getResources().getDisplayMetrics());
    }
}
