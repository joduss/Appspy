package com.epfl.appspy.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.epfl.appspy.LogA;
import com.epfl.appspy.R;
import com.epfl.appspy.database.ApplicationActivityRecord;
import com.epfl.appspy.database.ApplicationInstallationRecord;
import com.epfl.appspy.database.Database;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Activity showing graph and statistic to the user
 */
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


        //Setup default value for graph: takes the statistics of the current day
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


    /**
     * The user want to change the beginning of the displayed stats.
     * @param v
     */
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

    /**
     * The user want to change the end of the displayed stats.
     * @param v
     */
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
        setupGraph();
    }



    private void setupGraph() {

        //SETUP GRAPH COMMON
        //************
        int nbPoints = 48;

        int numLabelHorizontal = 7;
        int numLabelVertical = 6;



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

        //How long the interval of displayed stats is
        long intervalLength = end - start;

        //time between each point
        long subIntervalsLength = intervalLength / nbPoints;

        //subinterval in hours
        final double sbIntervalDurationHours = (intervalLength / nbPoints) / 1000.0 / 60.0 / 60.0;


        //Aggragate data by subinterval
        ArrayList<ApplicationActivityRecord> activityAggregatedForeground = new ArrayList<>();
        ArrayList<ApplicationActivityRecord> activityAggregatedBackground = new ArrayList<>();

        //Store when subinterval starts and ends
        ArrayList<Long> beginSubInterval = new ArrayList<>();
        ArrayList<Long> endSubInterval = new ArrayList<>();


        //BEGIN NEW
        //Create "bins" (subintervals)
        for(i = 0; i < nbPoints; i++){
            activityAggregatedForeground.add(new ApplicationActivityRecord(this.record.getPackageName(),0,0,0,0,0,false, false));
            activityAggregatedBackground.add(new ApplicationActivityRecord(this.record.getPackageName(),0,0,0,0,0,false, false));
            beginSubInterval.add(start + i * subIntervalsLength);
            endSubInterval.add(start + (i + 1) * subIntervalsLength);
        }

        //sorted, from oldest to newest
        List<ApplicationActivityRecord> recordsForegroundInInterval = db.getAppActivityInTimeRange(start,
                                                                                                   end,
                                                                                                   record.getPackageName(),
                                                                                                   true);

        LogA.d("Appspy-Graph","NB record found: " + recordsForegroundInInterval.size());

        //fill bins for foreground
        long lastFGTime = -1;
        long lastRecordTime = -1;
        for(ApplicationActivityRecord activityRecord : recordsForegroundInInterval){
            int interval = whatSubInterval(activityRecord.getRecordTime(), beginSubInterval, endSubInterval);

            long fgTime = activityRecord.getForegroundTime() - lastFGTime;
            long down = activityRecord.getDownloadedData();
            long up = activityRecord.getUploadedData();

            LogA.d("Appspy-Graph","down: " + down);

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

        //fill bins for background
        //sorted, from oldest to newest
        List<ApplicationActivityRecord> recordsBackgroundInInterval = db.getAppActivityInTimeRange(start,
                                                                                                   end,
                                                                                                   record.getPackageName(),
                                                                                                   false);
        for(ApplicationActivityRecord activityRec : recordsBackgroundInInterval){
            int interval = whatSubInterval(activityRec.getRecordTime(), beginSubInterval, endSubInterval);
            ApplicationActivityRecord currentRecord = activityAggregatedBackground.get(interval);
            currentRecord.setDownloadedData(currentRecord.getDownloadedData() + activityRec.getDownloadedData());
            currentRecord.setUploadedData(currentRecord.getUploadedData() + activityRec.getUploadedData());
        }

        //END FILL BINS

        LogA.d("Appspy-Graph", "NB foreground aggr. record: " + activityAggregatedForeground.size());


        GraphView graphFGTime = (GraphView) findViewById(R.id.graph_fg_time);
        LineGraphSeries<DataPoint> foregroundSeries = new LineGraphSeries<>();

        //Create data for the line
        i = 0;
        //foregroundSeries.appendData(new DataPoint(-5, -5), false, nbPoints+1);

        long max = 0;
        for (ApplicationActivityRecord r : activityAggregatedForeground) {
            max = (r.getForegroundTime()) > max ? r.getForegroundTime() : max;
            DataPoint p = new DataPoint(i, r.getForegroundTime());
            foregroundSeries.appendData(p, false, nbPoints);
            //LogA.d("Appspy-Graph","i:"+ i +  " r.getForegroundTime()/1000: " + r.getForegroundTime());
            i++;
        }

        max = max > 0 ? goodRoundingForTime(max, numLabelVertical) : 10;


        //SETUP GRAPH for foreground time usage
        //************

        //Set the graph data
        foregroundSeries.setTitle("Foreground time");
        graphFGTime.removeAllSeries();
        graphFGTime.addSeries(foregroundSeries);

        //customize the graph
        graphFGTime.getViewport().setXAxisBoundsManual(true);
        graphFGTime.getViewport().setMaxX(48);
        graphFGTime.getViewport().setMinX(0);

        graphFGTime.getViewport().setYAxisBoundsManual(true);

        graphFGTime.getViewport().setMaxY(max);
        graphFGTime.getViewport().setMinY(0);

        graphFGTime.getViewport().setScalable(true);

        graphFGTime.setTitle("Foreground(usage) time");

        graphFGTime.getGridLabelRenderer().setVerticalAxisTitle("Time in app[mm:ss]");
        graphFGTime.getGridLabelRenderer().setHorizontalAxisTitle("Time since beginning of interval [h]");

        graphFGTime.getGridLabelRenderer().setLabelsSpace(3);

        graphFGTime.getGridLabelRenderer().setNumHorizontalLabels(numLabelHorizontal);
        graphFGTime.getGridLabelRenderer().setNumVerticalLabels(numLabelVertical);


        //setup formating for the axis labels
        graphFGTime.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values

                    int h = (int) (value * sbIntervalDurationHours);
                    double min = (double)(value * sbIntervalDurationHours) - h;

                    String s = min > 0 ? "" + h + ":" + "30" : "" + h;


                    return s;
                }
                else {
                    // show currency for y values
                    int minutes = (int) (value / 1000.0 / 60.0);
                    int seconds = (int) (value / 1000) - minutes * 60;
                    DecimalFormat df = new DecimalFormat("00");
                    //totFGTimeTV.setText("" + minutes + "m" + df.format(seconds) + "s");

                    return "" + minutes + ":" + df.format(seconds);
                }
            }
        });





        //graph.addSeries(series2);

        //SETUP GRAPH for downloaded_DATA
        //************
        GraphView graphDownData = (GraphView) findViewById(R.id.graph_down_data);
        graphDownData.removeAllSeries();
        LineGraphSeries<DataPoint> downloadBackgroundSeries = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> downloadForegroundSeries = new LineGraphSeries<>();

        long maxDown = 0;
        for(int i = 0; i < activityAggregatedForeground.size(); i++){
            ApplicationActivityRecord foreRec = activityAggregatedForeground.get(i);
            ApplicationActivityRecord backRec = activityAggregatedBackground.get(i);

            maxDown = (foreRec.getDownloadedData()) > maxDown ? foreRec.getDownloadedData() : maxDown;
            DataPoint pFore = new DataPoint(i, foreRec.getDownloadedData());
            downloadForegroundSeries.appendData(pFore, false, nbPoints);

            maxDown = (backRec.getDownloadedData()) > maxDown ? backRec.getDownloadedData() : maxDown;
            DataPoint pBack = new DataPoint(i, backRec.getDownloadedData());
            downloadBackgroundSeries.appendData(pBack, false, nbPoints);
        }
        maxDown = maxDown == 0 ? 10240 : goodRoundingForData(maxDown, numLabelVertical);


        //customize lines
        downloadForegroundSeries.setColor(Color.BLUE);
        downloadBackgroundSeries.setColor(Color.RED);

        //add lines to graph
        graphDownData.addSeries(downloadBackgroundSeries);
        graphDownData.addSeries(downloadForegroundSeries);

        //custom graph down data
        graphDownData.getViewport().setMinY(0);
        graphDownData.getViewport().setMaxY(maxDown);
        graphDownData.getViewport().setYAxisBoundsManual(true);

        graphDownData.getViewport().setMinX(0);
        graphDownData.getViewport().setMaxX(nbPoints);
        graphDownData.getViewport().setXAxisBoundsManual(true);

        graphDownData.setTitle("Downloaded data");
        graphDownData.getGridLabelRenderer().setVerticalAxisTitle("Data downloaded [kB]");
        graphDownData.getGridLabelRenderer().setHorizontalAxisTitle("Time [h]");

        graphDownData.getGridLabelRenderer().setNumVerticalLabels(numLabelVertical);
        graphDownData.getGridLabelRenderer().setNumHorizontalLabels(numLabelHorizontal);

        //setup formating for the axis labels
        graphDownData.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    int h = (int) (value * sbIntervalDurationHours);
                    double min = (double) (value * sbIntervalDurationHours) - h;

                    String s = min > 0 ? "" + h + ":" + "30" : "" + h;
                    return s;
                }
                else {
//                    DecimalFormat df = new DecimalFormat("#0.00");
//                    if(value < 100 * 1024) {
//                        return "" + df.format(value / 1024.0);
//                    }
                    return "" + (long)(value / 1024.0);
                }
            }
        });

        //SETUP GRAPH FOR UPLOADED DATA
        //************
        GraphView graphUpData = (GraphView) findViewById(R.id.graph_up_data);
        graphUpData.removeAllSeries();
        LineGraphSeries<DataPoint> UploadBackgroundSeries = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> UploadForegroundSeries = new LineGraphSeries<>();

        long maxUp = 0;
        for(int i = 0; i < activityAggregatedForeground.size(); i++){
            ApplicationActivityRecord foreRec = activityAggregatedForeground.get(i);
            ApplicationActivityRecord backRec = activityAggregatedBackground.get(i);

            maxUp = (foreRec.getUploadedData()) > maxUp ? foreRec.getUploadedData() : maxUp;
            DataPoint pFore = new DataPoint(i, foreRec.getUploadedData());
            UploadForegroundSeries.appendData(pFore, false, nbPoints);

            maxUp = (backRec.getUploadedData()) > maxUp ? backRec.getUploadedData() : maxUp;
            DataPoint pBack = new DataPoint(i, backRec.getUploadedData());
            //LogA.d("Appspy-Graph","DATA UP BACK " + backRec.getUploadedData());
            UploadBackgroundSeries.appendData(pBack, false, nbPoints);
        }

        //setup axis limit
        maxUp = maxUp == 0 ? 10240 : goodRoundingForData(maxUp, numLabelVertical);

        //customize lines
        UploadForegroundSeries.setColor(Color.BLUE);
        UploadBackgroundSeries.setColor(Color.RED);

        //add lines to graph
        graphUpData.addSeries(UploadBackgroundSeries);
        graphUpData.addSeries(UploadForegroundSeries);

        //custom graph Up data
        graphUpData.getViewport().setMinY(0);
        graphUpData.getViewport().setMaxY(maxUp);
        graphUpData.getViewport().setYAxisBoundsManual(true);

        graphUpData.getViewport().setMinX(0);
        graphUpData.getViewport().setMaxX(nbPoints);
        graphUpData.getViewport().setXAxisBoundsManual(true);

        graphUpData.setTitle("Uploaded data");
        graphUpData.getGridLabelRenderer().setVerticalAxisTitle("Data Uploaded [kB]");
        graphUpData.getGridLabelRenderer().setHorizontalAxisTitle("Time [h]");

        graphUpData.getGridLabelRenderer().setNumVerticalLabels(numLabelVertical);
        graphUpData.getGridLabelRenderer().setNumHorizontalLabels(numLabelHorizontal);

        //setup formating for the axis labels
        graphUpData.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    int h = (int) (value * sbIntervalDurationHours);
                    double min = (double) (value * sbIntervalDurationHours) - h;

                    String s = min > 0 ? "" + h + ":" + "30" : "" + h;
                    return s;
                }
                else {
                    DecimalFormat df = new DecimalFormat("#0.00");
//                    if(value < 100 * 1024) {
//                        return "" + df.format(value / 1024.0);
//                    }
                    return "" + (long)(value / 1024.0);
                }
            }
        });




        //SETUP TEXTVIEW VALUES (text data shown to user)
        //************

        long foregroundDataDownloaded = db.getDataDownloadedInTimeRange(record.getPackageName(), start,end, true);
        long foregroundDataUploaded = db.getDataUploadedInTimeRange(record.getPackageName(), start,end, true);

        long backDataDownloaded = db.getDataDownloadedInTimeRange(record.getPackageName(), start, end, false);
        long backDataUploaded = db.getDataUploadedInTimeRange(record.getPackageName(), start,end, false);


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

        if(totalFGTime < 60) {
            totFGTimeTV.setText("" + (totalFGTime / 1000) + "seconds");

        }
        else {
            int minutes = (int) (totalFGTime / 1000.0 / 60.0);
            int seconds = (int) (totalFGTime / 1000) - minutes * 60;
            DecimalFormat df = new DecimalFormat("00");
            totFGTimeTV.setText("" + minutes + "m" + df.format(seconds) + "s");
        }

    }


//    /**
//     * Always return. If index is out of bound, return null
//     * @param list
//     * @param index
//     * @param <K>
//     * @return
//     */
//@Nullable
//    private static <K> K getIndexNoException(List<K> list, int index){
//        if(index > 0 && index < list.size()){
//            return list.get(index);
//        }
//        else {
//            return null;
//        }
//    }


    /**
     * Return in what subinterval the current time is
     * @param current Time in which interval we want to know it is
     * @param begin the list of beginning of each subinterval
     * @param endthe list of ending of each subinterval
     * @return
     */
    private int whatSubInterval(long current, List<Long> begin, List<Long> end){
        for(int i = 0; i < begin.size(); i++){
            if(current > begin.get(i) && current <= end.get(i)){
                return i;
            }
        }
        return -1;
    }


    /**
     * Check if midnight is between the lastRecordTime and the currentRecordTime
     * @param lastRecordTime
     * @param currentRecordTime
     * @return
     */
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


    /**
     * Return a good value that is the smallest value greater or equal to "bytes", that is divisible by divisor.
     * Works for kiloBytes
     * @param bytes
     * @param divisor
     * @return
     */
    private long goodRoundingForData(long bytes, long divisor){
        double kB = bytes / 1024.0;
        long guess = (long) Math.ceil(kB);

        while(guess % (divisor-1) != 0){
            guess ++;
        }

        return guess * 1024;
    }


    /**
     * Return a good rounder value for the time, that is >= to timeMillis and divisible by divisor
     * @param timeMillis
     * @param divisor
     * @return
     */
    private long goodRoundingForTime(long timeMillis, long divisor){

        double secondesDouble = timeMillis / 1000.0;
        long seconds = (long) Math.ceil(secondesDouble);

        while(seconds % (divisor-1) != 0){
            seconds ++;
        }
        return seconds*1000;
    }

}
