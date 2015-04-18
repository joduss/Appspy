package com.epfl.appspy.monitoring;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.LogA;
import com.epfl.appspy.ToastDebug;
import com.epfl.appspy.database.Database;
import com.epfl.appspy.database.GPSRecord;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Jonathan Duss on 30.03.15.
 */

public class GPSTracker extends BroadcastReceiver implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static Context context;
    private static GoogleApiClient googleApiClient;
    //private static LocationRequest locationRequest;



    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Appspy-GPS","GPS!!! + ");


        if (this.context == null) {
            this.context = context;
            Log.i("Appspy", "Setup GPS tracking");
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            //Connect. When connected successfully, onConnected() will be called
            googleApiClient.connect();

            //setup periodic check
            Intent gpsTracker = new Intent(context, GPSTracker.class);
            gpsTracker.setAction(Intent.ACTION_SEND);
            gpsTracker.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.AUTOMATIC);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTracker, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30000, pendingIntent);

        }

        if (intent.getAction().equals(Intent.ACTION_SEND) &&
            ( intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
            GlobalConstant.EXTRA_ACTION.MANUAL
            || intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
               GlobalConstant.EXTRA_ACTION.FIRST_LAUNCH
            || intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
               GlobalConstant.EXTRA_ACTION.AUTOMATIC )
            || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                || intent.getAction().equals(Intent.ACTION_PROVIDER_CHANGED)) {

            if(intent.getAction().equals(Intent.ACTION_PROVIDER_CHANGED)){
                //gps has been enabled or disabled
                LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                //lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }

            if(googleApiClient.isConnected()) {
                LogA.d("Appspy","Request update");
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setNumUpdates(1);
                locationRequest.setInterval(1);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

            }
            else {
                LogA.i("Appspy-GPS","googleApiClient is not connected");
            }

        }
    }


    @Override
    public void onLocationChanged(Location location) {
        LogA.i("Appspy-GPS","onLocationChanged " +location.getLatitude() + "  -  " + location.getLongitude() +
                           "  accuracy:" + location.getAccuracy() + "  altitude:" + location.getAltitude());

        ToastDebug.makeText(this.context,
                            "loca changed to: " + location.getLatitude() + "  -  " + location.getLongitude(),
                            Toast.LENGTH_LONG).show();
        //location.acc

        boolean enabled = true; //TODO find if gps is enabled


        GPSRecord record = new GPSRecord(System.currentTimeMillis(), enabled,location.getLongitude(),
                                         location.getLatitude(), location.getAltitude(), location.getAccuracy());

        Database db = Database.getDatabaseInstance(context);
        db.insertGPSRecord(record);
    }




    @Override
    public void onConnected(Bundle bundle) {
        LogA.i("Appspy-GPS","GoogleAPIClient is now onConnected");

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(25000);
        locationRequest.setExpirationDuration(35000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        LogA.i("Appspy-GPS", "GoogleAPIClient is now suspended");

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LogA.i("Appspy-GPS", "GoogleAPIClient failed");

    }
}
