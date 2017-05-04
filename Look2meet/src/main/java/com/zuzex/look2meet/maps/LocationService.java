package com.zuzex.look2meet.maps;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class LocationService {
    protected BehaviorSubject<Location> behaviorSubject;
    protected LocationClient locationClient;
    private static Location location;
    private Context context;
    public static boolean gpsAvailable = false;
    public LocationService(final Context context) {
        this.context = context;
        final LocationRequest locationRequest = LocationRequest.create()
                .setInterval(30000)
                .setSmallestDisplacement(50f)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        behaviorSubject = BehaviorSubject.create(selectProvider());
        behaviorSubject.subscribeOn(Schedulers.io());
        locationClient = new LocationClient(context, new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                behaviorSubject.onNext(locationClient.getLastLocation());
                locationClient.requestLocationUpdates(locationRequest, new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        behaviorSubject.onNext(location);
                        LocationService.location = location;
                    }

                });
            }
            @Override
            public void onDisconnected() {
                behaviorSubject.onCompleted();
            }

        }, new GooglePlayServicesClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                // propagate errors
            }
        }
        );
    }

    public Location selectProvider() {
        LocationManager lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        if(lm.getProviders(false).toString().contains("gps")) {
            gpsAvailable = true;
            new Location(LocationManager.GPS_PROVIDER);
        } else if(lm.getProviders(false).toString().contains("network")) {
            new Location(LocationManager.NETWORK_PROVIDER);
        }
        return new Location(LocationManager.PASSIVE_PROVIDER);
    }

    public Location getLastLocation() {
        return location;
    }

    public void start() {
        if (!locationClient.isConnected() || !locationClient.isConnecting())
            locationClient.connect();
    }

    public void stop() {
        if (locationClient.isConnected())
            locationClient.disconnect();
    }

    public Observable<Location> subscribeToLocation() {
        return behaviorSubject;
    }
}
