package com.zuzex.look2meet.api.apiData;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dgureev on 6/5/14.
 */
public class MapObjectsRequest {
    private String filter;
    private LatLng userCoords;

    double maxLat;
    double minLat;

    double maxLon;
    double minLon;

    public MapObjectsRequest(Location location, LatLngBounds coords) {
        userCoords = new LatLng(location.getLatitude(), location.getLongitude());

        if(coords.northeast.latitude > coords.southwest.latitude) {
            maxLat = coords.northeast.latitude;
            minLat = coords.southwest.latitude;
        } else {
            minLat = coords.northeast.latitude;
            maxLat = coords.southwest.latitude;
        }

        if(coords.northeast.longitude > coords.southwest.longitude) {
            maxLon = coords.northeast.longitude;
            minLon = coords.southwest.longitude;
        } else {
            minLon = coords.northeast.longitude;
            maxLon = coords.southwest.longitude;
        }
    }

    public String getUserCoordsLat() {
        return Double.toString(userCoords.latitude);
    }

    public String getUserCoordsLon() {
        return Double.toString(userCoords.longitude);
    }

    public Map<String, String> getUserCoordsMap() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("userCoords[lat]", getUserCoordsLat());
        params.put("userCoords[lon]", getUserCoordsLon());
        params.put("lat[min]", Double.toString(minLat));
        params.put("lat[max]", Double.toString(maxLat));
        params.put("lon[min]", Double.toString(minLon));
        params.put("lon[max]", Double.toString(maxLon));
        params.put("filter[neighbor]", "any");
        return params;
    }
}
