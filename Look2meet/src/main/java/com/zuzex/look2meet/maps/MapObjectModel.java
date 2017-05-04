package com.zuzex.look2meet.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zuzex.look2meet.DataModel.Organization;

import org.json.JSONObject;

/**
 * Created by dgureev on 6/5/14.
 */
public class MapObjectModel {

    public MapObjectModel(Organization organization) {
        this.organization = organization;
    }

    public MapObjectModel(JSONObject jsonObject) {
        organization = Organization.ParseFromJson(jsonObject);
    }

    public int neighborsPrev = -1;
    public int neighborNext = -1;
    public Organization organization;
    private Marker marker;

    private int multiObjectCount = 1;
    public void setMultiObjectCount(int multiObject) {
        this.multiObjectCount = multiObject;
    }
    public boolean isMultiObject() {
        if(this.multiObjectCount > 1)
            return true;
        else return false;
    }
    public int getMultiObjectCount() {
        return multiObjectCount;
    }
    public Marker getMarker() {
        return marker;
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }
    private MarkerOptions markerOptions;

    public LatLng getLocation() {
        return new LatLng(organization.latitude, organization.longitude);
    }
}
