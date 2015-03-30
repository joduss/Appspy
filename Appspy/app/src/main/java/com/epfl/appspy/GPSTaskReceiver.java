package com.epfl.appspy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Jonathan Duss on 30.03.15.
 */

public class GPSTaskReceiver extends BroadcastReceiver implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Context context;
    private GoogleApiClient googleApiClient;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Appspy-GPS","GPS!!!");

        if (this.context == null) {
            this.context = context;
        }

        if (intent.getAction().equals(Intent.ACTION_SEND) &&
            intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
            GlobalConstant.EXTRA_ACTION.GPS
            || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("Appspy", "Setup GPS tracking");

            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            //Conect. When connected successfully, onConnected() will be called
            googleApiClient.connect();



        }
    }




    @Override
    public void onLocationChanged(Location location) {
        Log.d("Appspy-GPS","onLocationChanged");

        //location.
    }




    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Appspy-GPS","onConnected");

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(20000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Appspy-GPS","onConnectionSuspended");

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Appspy-GPS","onConnectionFailed");

    }
}
