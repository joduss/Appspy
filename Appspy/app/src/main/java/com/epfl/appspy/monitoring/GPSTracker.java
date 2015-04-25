package com.epfl.appspy.monitoring;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.epfl.appspy.GlobalConstant;
import com.epfl.appspy.GlobalConstant.EXTRA_ACTION;
import com.epfl.appspy.LocationType;
import com.epfl.appspy.LogA;
import com.epfl.appspy.R;
import com.epfl.appspy.Settings;
import com.epfl.appspy.database.GPSRecord;

import com.epfl.appspy.ToastDebug;
import com.epfl.appspy.database.Database;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Jonathan Duss on 30.03.15.
 */


/**
 * Track the location of the user
 */
public class GPSTracker extends BroadcastReceiver implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static Context context;
    private static GoogleApiClient googleApiClient;
    private static double intervalPrecision = 0.1;
    private static LocationRequest locationRequest;


    //Location disabled -> GoogleApiClient = disconnected
    //location: lost -> suspended


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Appspy-GPS","GPS!!! ");


        //INIT phase
        if (this.context == null) {
            this.context = context;
            Log.i("Appspy-GPS", "Setup GPS tracking");
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            //When connected successfully, onConnected() will be called
            googleApiClient.connect();
        }
        //END INIT phase


        if(intent.getAction().equals(Intent.ACTION_SEND) &&
             intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) == EXTRA_ACTION.FIRST_LAUNCH){
            LogA.i("Appspy-GPS", "GPS Tracker called for first launch"); //manually calling won't be possible. only debug
            //nothing to do. Will be done in the init phase
            addRecordIfNoLocation();
        }
        else if( intent.getAction().equals(Intent.ACTION_SEND) &&
                intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) == EXTRA_ACTION.AUTOMATIC) {
            LogA.i("Appspy-GPS", "GPS Tracker called automatically. Periodic task because location services are disabled");
            addRecordIfNoLocation();
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            LogA.i("Appspy-GPS", "Starting GPS Tracker after boot");
            //addRecordIfNoLocation();
        }
        else if(intent.getAction().equals(LocationManager.MODE_CHANGED_ACTION)){
            if(getLocationType() == LocationType.NONE){
                LogA.i("Appspy-GPS", "Location services have been disabled");
                addRecordIfNoLocation();
                setPeriodicCheck();
            }
            else {
                LogA.i("Appspy-GPS", "Location services have been enabled");
                //Was disabled. So the client was disconnected. We reconnect it.
                //onConnected will be called once it will be connected
                cancelPeriodicCheck();
                googleApiClient.reconnect();
            }
        }
        else if(intent.getAction().equals(Intent.ACTION_SEND) &&
                intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) == EXTRA_ACTION.MANUAL){
            googleApiClient.reconnect();
        }
        else if(intent.getAction().equals(Intent.ACTION_SEND) &&
                intent.getSerializableExtra(GlobalConstant.EXTRA_TAG) == EXTRA_ACTION.UPDATE){
            LogA.i("Appspy-GPS", "settings for Location refresh interval has been updated. Reconfigure GPS Tracker..." );
            
            //GPS frequency has changed
            cancelPeriodicCheck();
            if(getLocationType() == LocationType.NONE){
                //reset the periodicCheck if no location enabled
                setPeriodicCheck();
            }
            else {
                //reconnect so it will take into account the new settings
                googleApiClient.reconnect();
            }
        }

    }

    private void addRecordIfNoLocation(){
        if(getLocationType() == LocationType.NONE){
            GPSRecord record = new GPSRecord(System.currentTimeMillis(), LocationType.NONE, LocationType.NO_VALUE,
                                             LocationType.NO_VALUE, LocationType.NO_VALUE, (float) LocationType.NO_VALUE);
            Database.getDatabaseInstance(context).insertGPSRecord(record);
        }
    }


    @Override
    public void
    onLocationChanged(Location location) {
        LogA.i("Appspy-GPS","onLocationChanged " +location.getLatitude() + "  -  " + location.getLongitude() +
                           "  accuracy:" + location.getAccuracy() + "  altitude:" + location.getAltitude());

        ToastDebug.makeText(this.context,
                            "loca changed to: " + location.getLatitude() + "  -  " + location.getLongitude(),
                            Toast.LENGTH_LONG).show();
        //location.acc


        LocationType locationType= getLocationType();


        GPSRecord record = new GPSRecord(System.currentTimeMillis(), locationType,location.getLongitude(),
                                         location.getLatitude(), location.getAltitude(), location.getAccuracy());

        Database.getDatabaseInstance(context).insertGPSRecord(record);

    }




    @Override
    public void onConnected(Bundle bundle) {
        long interval = Settings.getSettings(context).getGPSIntervalMillis();
        LogA.i("Appspy-GPS","GoogleAPIClient is now onConnected. Will be updated every " + interval / 1000 + " seconds");


        locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval((long) (interval*(1-intervalPrecision)));
        locationRequest.setExpirationDuration((long) (interval*(1+intervalPrecision)));
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


        //Check if Location is available. If disabled, setup checking every X minutes
        //setup periodic check
        if(getLocationType() == LocationType.NONE) {
            LogA.i("Appspy-GPS", "Google API Client is now onConnected, but Location is disabled" );
            setPeriodicCheck();
        } else {
            //location services are working fine. Cancel the periodic check
            cancelPeriodicCheck();
        }
    }

    private void setPeriodicCheck(){
        long interval = Settings.getSettings(context).getGPSIntervalMillis();
        if(getLocationType() == LocationType.NONE) {
            LogA.i("Appspy-GPS", "Set up periodic check every " + interval / 1000 + " seconds");

            cancelPeriodicCheck(); //first cancel

            Intent gpsTracker = new Intent(context, GPSTracker.class);
            gpsTracker.setAction(Intent.ACTION_SEND);
            gpsTracker.putExtra(GlobalConstant.EXTRA_TAG, GlobalConstant.EXTRA_ACTION.AUTOMATIC);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTracker, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        }
        else
        {
            //there was an error. cancel and continue with location
            LogA.d("Appspy-GPS","bidoum");
            cancelPeriodicCheck();
            googleApiClient.reconnect();
        }

    }

    private void cancelPeriodicCheck(){
        Intent gpsTracker = new Intent(context, GPSTracker.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTracker, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        LogA.i("Appspy-GPS", "GoogleAPIClient is now suspended. Force re");
        googleApiClient.reconnect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LogA.i("Appspy-GPS", "GoogleAPIClient failed. Try again...");
        //try again
        googleApiClient.connect();
    }


    /**
     * Give what type of location provider is in use.
     * @return used location provider
     */
    private LocationType getLocationType(){
        if(context != null) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                return LocationType.GPS;
            }
            else if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                return LocationType.NETWORK;
            }
            else{
                return LocationType.NONE;
            }
        }
        return null;
    }

}
