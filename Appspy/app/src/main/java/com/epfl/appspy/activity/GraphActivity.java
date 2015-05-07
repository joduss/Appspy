package com.epfl.appspy.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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

import java.lang.reflect.Array;
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

    private ApplicationInstallationRecord record;

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


        long appId = this.getIntent().getLongExtra("AppId", -1);
        LogA.d("Appspy-Graph","Extra: " + appId);
        record = Database.getDatabaseInstance(this).getAppInstallRecordForId(appId);

        if(record != null) {
            this.setTitle(record.getApplicationName());
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No data for that application in the given time interval")
                   .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           // FIRE ZE MISSILES!
                       }
                   });
            builder.create().show();
            // Create the AlertDialog object and return it
        }


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

        long subIntervalsLength = intervalLength / nbPoints;


        ArrayList<ApplicationActivityRecord> activityAggregatedForeground = new ArrayList<>();
        ArrayList<ApplicationActivityRecord> activityAggregatedBackground = new ArrayList<>();

        ArrayList<Long> beginInterval = new ArrayList<>();
        ArrayList<Long> endInterval = new ArrayList<>();


        //BEGIN NEW
        //Create "bins"
        for(i = 0; i < nbPoints; i++){
            activityAggregatedForeground.add(new ApplicationActivityRecord(this.record.getPackageName(),0,0,0,0,0,false, false));
            activityAggregatedBackground.add(new ApplicationActivityRecord(this.record.getPackageName(),0,0,0,0,0,false, false));
            beginInterval.add(start + i * subIntervalsLength);
            endInterval.add(start + (i+1) * subIntervalsLength);
        }

        //sorted, from oldest to newest
        List<ApplicationActivityRecord> recordsForegroundInInterval = db.getAppActivityInTimeRange(start,
                                                                                                   end,
                                                                                                   record.getPackageName(),
                                                                                                   true);

        //
        long lastFGTime = -1;
        long lastRecordTime = -1;
        for(ApplicationActivityRecord activityRecord : recordsForegroundInInterval){
            int interval = whatInterval(activityRecord.getRecordTime(), beginInterval, endInterval);

            long fgTime = activityRecord.getForegroundTime() - lastFGTime;
            long down = activityRecord.getDownloadedData();
            long up = activityRecord.getUploadedData();

            if(lastFGTime == -1){
                //first record. We don't know what there was before
                fgTime = 0;
            }
            else if(rebootOnMidnight(lastRecordTime, activityRecord.getRecordTime()) == true){
                //we just reboot after a night. No app were open, stats are 0 for all apps, so if not 0, means it
                //was open that amount of time
                fgTime = activityRecord.getForegroundTime();
            }

            ApplicationActivityRecord currentRec = activityAggregatedForeground.get(interval);
            currentRec.setDownloadedData( currentRec.getDownloadedData() + down);
            currentRec.setUploadedData(currentRec.getUploadedData() + up);
            currentRec.setForegroundTime(currentRec.getForegroundTime() + fgTime);


            lastFGTime = activityRecord.getForegroundTime();
            lastRecordTime = activityRecord.getRecordTime();

        }

        //END NEW


        LogA.d("Appspy-Graph", "NB foreground aggr. record: " + activityAggregatedForeground.size());


        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> foregroundSeries = new LineGraphSeries<>();

        //Create data for the line
        i = 0;
        //foregroundSeries.appendData(new DataPoint(-5, -5), false, nbPoints+1);

        double max = 0;
        for (ApplicationActivityRecord r : activityAggregatedForeground) {
            max = (r.getForegroundTime()/1000) > max ? r.getForegroundTime()/1000 : max;
            DataPoint p = new DataPoint(i, r.getForegroundTime()/1000);
            foregroundSeries.appendData(p, false, nbPoints);
            LogA.d("Appspy-Graph","i:"+ i +  " r.getForegroundTime()/1000: " + r.getForegroundTime());
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




        //Set the graph data
        foregroundSeries.setTitle("Foreground time");
        graph.removeAllSeries();
        graph.addSeries(foregroundSeries);

        //customize the graph
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(50);
        graph.getViewport().setMinX(0);

        graph.getViewport().setYAxisBoundsManual(true);
        max = max > 0 ? max*1.1 : 10;
        graph.getViewport().setMaxY(max);
        graph.getViewport().setMinY(0);

        graph.getViewport().setScalable(true);


        //graph.addSeries(series2);



        //compute data over whole interval
        //************

        long foregroundDataDownloaded = 0;
        long foregroundDataUploaded = 0;

        for(ApplicationActivityRecord foregroundRecord : activityAggregatedForeground){
            foregroundDataDownloaded += foregroundRecord.getDownloadedData();
            foregroundDataUploaded += foregroundRecord.getUploadedData();
        }

        long backDataDownloaded = 0;
        long backDataUploaded = 0;

        for(ApplicationActivityRecord backgroundRecord : activityAggregatedBackground){
            backDataDownloaded += backgroundRecord.getDownloadedData();
            foregroundDataUploaded += backgroundRecord.getUploadedData();
        }

        long totalFGTime = 0;
        for(ApplicationActivityRecord r : activityAggregatedForeground){
            totalFGTime += r.getForegroundTime();
        }

        long totalDown = foregroundDataDownloaded + backDataDownloaded;
        long totalUp = foregroundDataUploaded + backDataUploaded;

        TextView intervalLengthTV = (TextView) findViewById(R.id.interval_length_tv);
        TextView fgDataDownTV = (TextView) findViewById(R.id.data_down_fore_tv);
        TextView fgDataUpTV = (TextView) findViewById(R.id.data_up_fore_tv);
        TextView bgDataDownTV = (TextView) findViewById(R.id.data_down_back_tv);
        TextView bgDataUpTV = (TextView) findViewById(R.id.data_up_back_tv);
        TextView totDataDownTV = (TextView) findViewById(R.id.data_down_tv);
        TextView totDataUpTV = (TextView) findViewById(R.id.data_up_tv);
        TextView totFGTimeTV = (TextView) findViewById(R.id.tot_foregroundtime_tv);



        intervalLengthTV.setText("" + (intervalLength / 1000 / 60 / 60) + " hours");
        fgDataDownTV.setText("" + (foregroundDataDownloaded) + " Bytes");
        fgDataUpTV.setText("" + (foregroundDataUploaded ) + " Bytes");
        bgDataDownTV.setText("" + backDataDownloaded + " Bytes");
        bgDataUpTV.setText("" + backDataUploaded + " Bytes");
        totDataDownTV.setText("" + totalDown + " Bytes");
        totDataUpTV.setText("" + totalUp + " Bytes");
        totFGTimeTV.setText("" + (totalFGTime / 1000) + "seconds");



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


    private int whatInterval(long current, List<Long> begin, List<Long> end){
        for(int i = 0; i < begin.size(); i++){
            if(current > begin.get(i) && current < end.get(i)){
                return i;
            }
        }
        return -1;
    }

    private boolean rebootOnMidnight(long lastRecordTime, long currentRecordTime){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(lastRecordTime);
        int dayLast = c.get(Calendar.DAY_OF_YEAR);
        c.setTimeInMillis(currentRecordTime);
        int dayCurrent = c.get(Calendar.DAY_OF_YEAR);

        if(dayCurrent > dayLast){
            return true;
        }
        return false;
    }

}
