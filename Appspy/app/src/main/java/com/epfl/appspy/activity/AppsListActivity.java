package com.epfl.appspy.activity;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.epfl.appspy.R;
import com.epfl.appspy.database.Database;
import com.epfl.appspy.database.DatabaseNames;


/**
 * Show the list of application that are currently installed on the device, or that were installed on the device while
 * Appspy was monitoring
 */
public class AppsListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private SimpleCursorAdapter adapter;

    static final String PROJECTION[] = new String[] { DatabaseNames.COL_APP_NAME};
    static final String SELECTION = "*";
    private Cursor c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_list);

        String[] fromColumns = {DatabaseNames.COL_APP_NAME};
        int[] toView = {android.R.id.text1};
        adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,null,fromColumns, toView,0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0,null,this);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {

                // get all data from DataBase
                c = Database.getDatabaseInstance(getApplicationContext()).getAllTimeApplicationInstalledCursor();

                return c;
            }
        };
    }


    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        adapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        adapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
        Intent nextIntent = new Intent(this, GraphActivity.class);
        nextIntent.putExtra("AppId", id);
        startActivity(nextIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_apps_list, menu);
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
}
