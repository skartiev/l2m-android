package com.zuzex.look2meet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.zuzex.look2meet.DataModel.UserProfile;

import java.util.List;

/**
 * Created by sanchirkartiev on 19.06.14.
 */
public class MyProfileLoaderAdapter extends BaseAdapter implements  View.OnClickListener {

    private static final int ROW_TYPE_TEXT = 0;
    private static final int ROW_TYPE_SWITCH = 1;
    private static final int ROW_TYPES_COUNT = 2;

    private List<UserProfile.Field> fields;
    private Context context;
    private LayoutInflater inflater = null;
    private boolean isEditable;

    public MyProfileLoaderAdapter(Context context, List<UserProfile.Field> fields, Boolean editable) {
        this.context = context;
        this.fields = fields;
        this.isEditable = editable;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public MyProfileLoaderAdapter(Context context, Boolean editable) {
        this.context = context;
        this.fields = fields;
        this.isEditable = editable;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setItems(List<UserProfile.Field> fields) {
        if(fields != null)
            this.fields = fields;
        return;
    }

    @Override
    public int getItemViewType(int position) {
        return fields.get(position).dataType == UserProfile.FieldDataType.ENUM ? ROW_TYPE_SWITCH : ROW_TYPE_TEXT;
    }

    @Override
    public int getViewTypeCount() {
        return ROW_TYPES_COUNT;
    }

    @Override
    public int getCount() {
        return fields.size();
    }

    @Override
    public UserProfile.Field getItem(int i) {return fields.get(i);}

    @Override
    public long getItemId(int i) {
        return i;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View v;
        final UserProfile.Field field = fields.get(position);
        if(convertView == null) {
            if(field.isEditable) {
                v = inflater.inflate(R.layout.listitemeditable, parent, false);
            } else {
                v = inflater.inflate(R.layout.listitem, parent, false);
            }
        } else{
            v = convertView;
        }

        if(field.isEditable) { // Boolean in fact
            TextView firstItem = (TextView) v.findViewById(R.id.textViewEditable);
            Switch secondItem = (Switch) v.findViewById(R.id.switchEditable);
            firstItem.setText(field.title);
            secondItem.setChecked(Integer.parseInt(field.value) != 0);

            if(isEditable) {
                secondItem.setClickable(true);
                secondItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            field.value = "1";
                        } else {
                            field.value = "0";
                        }
                    }
                });
            } else {
                secondItem.setClickable(false);
            }
        } else {
            TextView firstItem = (TextView) v.findViewById(R.id.textViewFirstItem);
            firstItem.setText(field.title);
            TextView secondItem = (TextView) v.findViewById(R.id.textViewSecondItem);
            secondItem.setText(field.value);
        }
        return v;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public void onClick(View view) {

    }
}
