package com.epfl.appspy.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.epfl.appspy.LogA;
import com.epfl.appspy.R;
import com.epfl.appspy.database.ApplicationActivityRecord;
import com.epfl.appspy.database.ApplicationInstallationRecord;
import com.epfl.appspy.database.Database;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class GraphActivity extends ActionBarActivity {

    TextView intervalTV;


    private int startDay = 0;
    private int startMonth = 0;
    private int startYear = 0;
    private int startHour = 0;
    private int startMinute = 0;

    private int endDay = 0;
    private int endMonth = 0;
    private int endYear = 0;
    private int endHour = 0;
    private int endMinute = 0;
    private int i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);


        intervalTV = (TextView) findViewById(R.id.tv_interval);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        startYear = year;
        endYear = year;
        startMonth = month;
        endMonth = month;
        startDay = day;
        endDay = day;

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScreenInformation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void clickChangeBeginning(View v) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, final int yearInput, final int monthOfYear, final int dayOfMonth) {
                startDay = dayOfMonth;
                startMonth = monthOfYear;
                startYear = yearInput;
                updateScreenInformation();
            }
        }, year, month, day);
        dpd.setTitle("Interval start day");
        dpd.show();
    }


    public void clickChangeEnding(View v) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, final int yearInput, final int monthOfYear, final int dayOfMonth) {
                endDay = dayOfMonth;
                endMonth = monthOfYear;
                endYear = yearInput;
                updateScreenInformation();
            }
        }, year, month, day);
        dpd.setTitle("Interval end day");
        dpd.show();
    }





    private void updateScreenInformation() {
        String title =
                startDay + "-" + (startMonth + 1) + "-" + startYear + " ---> " + endDay + "-" + (endMonth + 1) + "-" +
                endYear;
        intervalTV.setText(title);
        getInfo();
    }


//    private void getInfo(){
//        Database db = Database.getDatabaseInstance(getApplicationContext());
//
//        Calendar c = Calendar.getInstance();
//        c.set(startYear, startMonth, startDay,0,0,0);
//        long start = c.getTimeInMillis();
//        c.set(endYear, endMonth, endDay,0,0,0);
//        c.add(Calendar.DAY_OF_MONTH, 1);
//        long end = c.getTimeInMillis();
//
//        List<ApplicationActivityRecord> recordsInInterval = db.getAppActivityInTimeRange(start, end, "com.facebook.katana");
//
//        LogA.d("Appspy-Graph","start = " + start);
//        LogA.d("Appspy-Graph","end = " + end);
//
//        LogA.d("Appspy-Graph","end - start = " + (end-start));
//        LogA.d("Appspy-Graph", "NB record from DB: " + recordsInInterval.size());
//
//
//
//        long intervalLength = end - start;
//        int nbPoints = 48;
//
//        long subIntervalsLength = intervalLength / 48;
//
//        long startCurrentSubInterval = start;
//
//        int index = 0;
//        ArrayList<ApplicationActivityRecord> activityAggregatedForeground = new ArrayList<>();
//        ArrayList<ApplicationActivityRecord> activityAggregatedBackground = new ArrayList<>();
//
//
//        if(recordsInInterval.size() > 0){
//            startCurrentSubInterval = recordsInInterval.get(0).getRecordTime();
//        }
//
//        long foregroundTime = 0;
//        for(ApplicationActivityRecord record : recordsInInterval){
//            LogA.d("Appspy-Graph","record time:" + record.getRecordTime());
//            if(record.getRecordTime() >= startCurrentSubInterval && record.getRecordTime() < (startCurrentSubInterval + subIntervalsLength)){
//
//                if(record.isWasForeground()){
//                    ApplicationActivityRecord r = activityAggregatedForeground.get(index);
//                    if(r == null){
//                        foregroundTime = record.getForegroundTime();
//                        r = new ApplicationActivityRecord(record.getPackageName(),subIntervalsLength, 0,0,record.getUploadedData(), record.getDownloadedData(),record.isWasForeground(), record.isBoot());
//                        activityAggregatedForeground.add(r);
//                    }
//                    else{
//                        if(record.getForegroundTime() < r.getForegroundTime() && record.isBoot()){
//                            //means the stat were reseted (change of day with reboot?)
//                            r.setForegroundTime(r.getForegroundTime() + record.getForegroundTime());
//                            foregroundTime = record.getForegroundTime();
//                        }
//                        r.setForegroundTime(r.getForegroundTime() + (record.getForegroundTime() - foregroundTime));
//                        r.setUploadedData(r.getUploadedData() + record.getUploadedData());
//                        r.setDownloadedData(r.getDownloadedData() + record.getDownloadedData());
//                    }
//
//                }
//                else {
//                    //background activity
//                    ApplicationActivityRecord r = activityAggregatedBackground.get(index);
//                    if(r == null){
//                        r = new ApplicationActivityRecord(record.getPackageName(),subIntervalsLength, 0,0,record.getUploadedData(), record.getDownloadedData(),record.isWasForeground(), record.isBoot());
//                        activityAggregatedBackground.add(r);
//                    }
//                    else{
//                        r.setUploadedData(r.getUploadedData() + record.getUploadedData());
//                        r.setDownloadedData(r.getDownloadedData() + record.getDownloadedData());
//                    }
//                }
//
//            }
//            else {
//                //we did the whole interval
//                startCurrentSubInterval += subIntervalsLength;
//                index ++;
//            }
//
//        } // END FOR
//
//        LogA.d("Appspy-Graph", "NB foreground aggr. record: " + activityAggregatedForeground.size());
//
//
//        GraphView graph = (GraphView) findViewById(R.id.graph);
//        LineGraphSeries<DataPoint> foregroundSeries = new LineGraphSeries<DataPoint>();
//
//        int i = 0;
//        for(ApplicationActivityRecord r : activityAggregatedForeground){
//            DataPoint p = new DataPoint(i, r.getForegroundTime());
//            foregroundSeries.appendData(p, true, nbPoints);
//            i++;
//        }
//
//        //LineGraphSeries<DataPoint> backgroundSeries = new LineGraphSeries<DataPoint>();
//
////            i = 0;
////            for(ApplicationActivityRecord r : activityAggregatedForeground){
////                DataPoint p = new DataPoint(i, r.getForegroundTime());
////                backgroundSeries.appendData(p, true, nbPoints);
////                i++;
////            }
//
//
//
//
//        //series2.setColor(Color.RED);
//
//
//        graph.addSeries(foregroundSeries);
//        //graph.addSeries(series2);
//    }


    private void getInfo() {
        Database db = Database.getDatabaseInstance(getApplicationContext());

        Calendar c = Calendar.getInstance();
        c.set(startYear, startMonth, startDay, 0, 0, 0);
        long start = c.getTimeInMillis();
        c.set(endYear, endMonth, endDay, 0, 0, 0);
        c.add(Calendar.DAY_OF_MONTH, 1); //to include the selected end day, add 24h
        long end = c.getTimeInMillis();


        LogA.d("Appspy-Graph", "start = " + start);
        LogA.d("Appspy-Graph", "end = " + end);

        LogA.d("Appspy-Graph", "end - start = " + (end - start));


        long intervalLength = end - start;
        int nbPoints = 48;

        long subIntervalsLength = intervalLength / 48;

        long startCurrentSubInterval = start;

        int index = 0;
        ArrayList<ApplicationActivityRecord> activityAggregatedForeground = new ArrayList<>();
        ArrayList<ApplicationActivityRecord> activityAggregatedBackground = new ArrayList<>();

        long totForegroundTimeInInterval = 0;

        while (startCurrentSubInterval < end) {

            String packageName ="com.facebook.katana";

            List<ApplicationActivityRecord> recordsForegroundInInterval = db.getAppActivityInTimeRange(startCurrentSubInterval,
                                                                                             (startCurrentSubInterval +
                                                                                              subIntervalsLength - 1),
                                                                                             packageName, true);

            long foregroundTime = 0;
            for (ApplicationActivityRecord record : recordsForegroundInInterval) {
                LogA.d("Appspy-Graph", "record time:" + record.getRecordTime());

                if (record.isWasForeground()) {
                    ApplicationActivityRecord r = getIndexNoException(activityAggregatedForeground, index);
                    if (r == null) {
                        foregroundTime = record.getForegroundTime();
                        r = new ApplicationActivityRecord(record.getPackageName(), subIntervalsLength, 0, 0,
                                                          record.getUploadedData(), record.getDownloadedData(),
                                                          record.isWasForeground(), record.isBoot());
                        activityAggregatedForeground.add(r);
                    }
                    else {
                        if (record.getForegroundTime() < r.getForegroundTime() && record.isBoot()) {
                            //means the stat were reseted (change of day with reboot?)
                            r.setForegroundTime(r.getForegroundTime() + record.getForegroundTime());
                            totForegroundTimeInInterval += record.getForegroundTime();
                            foregroundTime = record.getForegroundTime();
                        }
                        r.setForegroundTime(r.getForegroundTime() + (record.getForegroundTime() - foregroundTime));
                        totForegroundTimeInInterval += (record.getForegroundTime() - foregroundTime);
                        r.setUploadedData(r.getUploadedData() + record.getUploadedData());
                        r.setDownloadedData(r.getDownloadedData() + record.getDownloadedData());
                    }

                }
//                else {
//                    //background activity
//                    ApplicationActivityRecord r = getIndexNoException(activityAggregatedBackground, index);
//                    if (r == null) {
//                        r = new ApplicationActivityRecord(record.getPackageName(), subIntervalsLength, 0, 0,
//                                                          record.getUploadedData(), record.getDownloadedData(),
//                                                          record.isWasForeground(), record.isBoot());
//                        activityAggregatedBackground.add(r);
//                    }
//                    else {
//                        r.setUploadedData(r.getUploadedData() + record.getUploadedData());
//                        r.setDownloadedData(r.getDownloadedData() + record.getDownloadedData());
//                    }
//                }



            } // END FOR

            LogA.d("Appspy-Graph","index: " + index);

            if(recordsForegroundInInterval.size() == 0){
                ApplicationActivityRecord r = new ApplicationActivityRecord(packageName, startCurrentSubInterval, 0, 0,
                                                                            0, 0,
                                                                            false, false);
                activityAggregatedForeground.add(r);
            }

            //set values on textview
            TextView totalForegroundTimeTV = (TextView) findViewById(R.id.tot_foregroundtime_tv);
            totalForegroundTimeTV.setText("" + totForegroundTimeInInterval);


            //go to next interval
            foregroundTime = 0;
            index++;
            startCurrentSubInterval += subIntervalsLength;
        } //END WHILE

        LogA.d("Appspy-Graph", "NB foreground aggr. record: " + activityAggregatedForeground.size());


        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> foregroundSeries = new LineGraphSeries<DataPoint>();

        i = 0;
        for (ApplicationActivityRecord r : activityAggregatedForeground) {
            DataPoint p = new DataPoint(i, r.getForegroundTime()/1000);
            foregroundSeries.appendData(p, true, nbPoints);
            LogA.d("Appspy-Graph","r.getForegroundTime()/1000: " + r.getForegroundTime()/1000);
            i++;
        }

        //LineGraphSeries<DataPoint> backgroundSeries = new LineGraphSeries<DataPoint>();

//            i = 0;
//            for(ApplicationActivityRecord r : activityAggregatedForeground){
//                DataPoint p = new DataPoint(i, r.getForegroundTime());
//                backgroundSeries.appendData(p, true, nbPoints);
//                i++;
//            }


        //series2.setColor(Color.RED);

        foregroundSeries.setTitle("Foreground time");
        graph.removeAllSeries();
        graph.getViewport().setMaxX(50);
        graph.addSeries(foregroundSeries);
        graph.getViewport().setMaxX(50);
        graph.getViewport().setScalable(true);
        //graph.addSeries(series2);


    }


    /**
     * Always return. If index is out of bound, return null
     * @param list
     * @param index
     * @param <K>
     * @return
     */
@Nullable
    private static <K> K getIndexNoException(List<K> list, int index){
        if(index > 0 && index < list.size()){
            return list.get(index);
        }
        else {
            return null;
        }
    }

}
