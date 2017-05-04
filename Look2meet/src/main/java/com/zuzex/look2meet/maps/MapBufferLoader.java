package com.zuzex.look2meet.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by dgureev on 6/26/14.
 */
public class MapBufferLoader {
    boolean firstLoad = true;
    LatLngBounds currentBuffSize;

    double maxLat;
    double maxLon;
    double minLat;
    double minLon;
    double maxLatBuff;
    double maxLonBuff;
    double minLatBuff;
    double minLonBuff;

    public MapBufferLoader() {
        currentBuffSize = new LatLngBounds(new LatLng(0,0), new LatLng(0,0));
    }

   public void calcBuffSize(LatLngBounds mapViewPosition) {
       maxLat = mapViewPosition.northeast.latitude;
       minLat = mapViewPosition.southwest.latitude;
       maxLon = mapViewPosition.northeast.longitude;
       minLon = mapViewPosition.southwest.longitude;

       maxLatBuff = maxLat + (maxLat - minLat)/4;
       maxLonBuff = maxLon + (maxLon - minLon)/4;
       minLatBuff = minLat - (maxLat - minLat)/4;
       minLonBuff = minLon - (maxLon - minLon)/4;
       currentBuffSize = new LatLngBounds(new LatLng(minLatBuff, minLonBuff), new LatLng(maxLatBuff, maxLonBuff));
    }

    public LatLngBounds getCurrentBuffSize() {
        return this.currentBuffSize;
    }

    public boolean isNeedUpdate(LatLngBounds currentScreen) {
        if(firstLoad) {
            firstLoad = false;
            return true;
        }
        if (currentBuffSize.contains(currentScreen.northeast) && currentBuffSize.contains(currentScreen.southwest)) {
            return false;
        } else {
            calcBuffSize(currentScreen);
        }
        return true;
    }

}

