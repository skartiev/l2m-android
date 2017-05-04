package com.zuzex.look2meet.maps;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLngBounds;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.zuzex.look2meet.api.GlobalHelper;
import com.zuzex.look2meet.api.Look2meetApi;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dgureev on 6/27/14.
 */
public class MapObjectLoader {
    private ArrayList<MapObjectModel> mapObjectList;
    DrawableMarker drawableMarker;
    MapBufferLoader mapBufferLoader;
    IMapObjectLoader loaderInterface;
    private Context context;

    public MapObjectLoader (Context context) {
        mapObjectList = new ArrayList<MapObjectModel>();
        mapBufferLoader = new MapBufferLoader();
        drawableMarker = new DrawableMarker(context);
        this.context = context;
    }

    public void loadFromId(int id, final IMapObjectLoader mapObjectLoader) {
        Look2meetApi.getInstance().getObjectView(id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if(GlobalHelper.successStatusFromJson(response, false, context)) {
                    if(response.has("data")) {
                        JSONObject data = response.optJSONObject("data");
                        JSONObject object = data.optJSONObject("object");
                        MapObjectModel m = new MapObjectModel(object);
                        mapObjectLoader.objectLoaded(m);
                    }
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });
    }

    public void loadObjects(LatLngBounds screenBounds, Location myLocation, IMapObjectLoader mapObjectLoader) {
        this.loaderInterface = mapObjectLoader;
        RequestParams params = new RequestParams();
        params.add("userCoords[lon]", Double.toString(myLocation.getLongitude()));
        params.add("userCoords[lat]", Double.toString(myLocation.getLatitude()));
        params.add("lat[min]", Double.toString(screenBounds.southwest.latitude));
        params.add("lat[max]", Double.toString(screenBounds.northeast.latitude));
        params.add("lon[min]", Double.toString(screenBounds.southwest.longitude));
        params.add("lon[max]", Double.toString(screenBounds.northeast.longitude));
        params.add("filter[neighbor]", "any");
	    Look2meetApi.getInstance().getObjects(params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if(GlobalHelper.successStatusFromJson(response)) {
                    parseResponseJson(response);
                }
            }
        });
    }

    private void parseResponseJson(JSONObject response) {
        JSONObject data = response.optJSONObject("data");
        if(data != null) {
            JSONArray objects = data.optJSONArray("objects");
            if (objects != null && objects.length() > 0) {
                mapObjectList = new ArrayList<MapObjectModel>();
                for (int i = 0; i < objects.length(); i++) {
                    MapObjectModel m = new MapObjectModel(objects.optJSONObject(i));
                    mapObjectList.add(m);
                }
                this.loaderInterface.objectsLoaded(mapObjectList);
            }
        }
    }
}
