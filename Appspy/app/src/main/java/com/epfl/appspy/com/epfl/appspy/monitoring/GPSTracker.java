package com.epfl.appspy.com.epfl.appspy.monitoring;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.LogA;
import com.epfl.appspy.ToastDebug;
import com.epfl.appspy.com.epfl.appspy.database.Database;
import com.epfl.appspy.com.epfl.appspy.database.GPSRecord;
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

    private Context context;
    private GoogleApiClient googleApiClient;



    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Appspy-GPS","GPS!!!");

        if (this.context == null) {
            Log.i("Appspy", "Setup GPS tracking");
            this.context = context;
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

            //TODO: setup at boot/00/30/00/30
        }

        if (intent.getAction().equals(Intent.ACTION_SEND) &&
            ( intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
            GlobalConstant.EXTRA_ACTION.MANUAL
            || intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
               GlobalConstant.EXTRA_ACTION.FIRST_LAUNCH
            || intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) ==
               GlobalConstant.EXTRA_ACTION.AUTOMATIC )
            || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {






        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("Appspy-GPS","onLocationChanged " +location.getLatitude() + "  -  " + location.getLongitude() +
                           "  acc:" + location.getAccuracy() + "  altitude:" + location.getAltitude());

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
        Log.d("Appspy-GPS","onConnected");

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(25000);
        mLocationRequest.setExpirationDuration(35000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Appspy-GPS","onConnectionSuspended");
        LogA.d("Appspy-log", "GPS connection suspended");

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Appspy-GPS","onConnectionFailed");
        LogA.d("Appspy-log","GPS connection failed");

    }
}
