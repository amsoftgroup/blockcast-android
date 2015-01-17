package com.sombrerosoft.blockcast.android;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.osmdroid.util.GeoPoint;

import com.sombrerosoft.blockcast.R;
import com.sombrerosoft.blockcast.android.util.NetworkStatus;
import com.sombrerosoft.blockcast.android.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class BlockcastBaseActivity extends Activity implements ActivityLifecycleCallbacks {
	
	//private ActivityTracker activityTracker;
	
	private String timeformat = Utils.timeformat;
	private SimpleDateFormat df = new SimpleDateFormat(timeformat);  
	private String TAG = "BlockcastBaseActivity";
	private SharedPreferences prefs;
	protected static Location mLocation;

	private static LocationManager mLocationManager;
	private String SYSTEM_OF_MEASUREMENT = "METRIC";
	private boolean network_enabled = false;
	public long distance = -1;
	public long duration = -1;
	protected String debug = "0";
	protected static MyLocationListener locationListener;
	private GeoPoint mapCenter = null;
	private NetworkStatus network_status = null;
	protected boolean is_connected = false;
	protected String guid = null;
	
	@Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
		
        guid = Installation.id(getBaseContext());
        df.setTimeZone(TimeZone.getTimeZone("GMT"));  
 	

    }
	
	@Override
    protected void onStart() {
        super.onStart();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();   
    }

	@Override
    protected void onResume() {
        super.onResume();
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        debug = prefs.getString("DEBUG", "0");
        distance = Long.parseLong(prefs.getString("DISTANCE", "100"));
	    duration = Long.parseLong(prefs.getString("DURATION", "3600"));
	    
        is_connected = Utils.isNetworkAvailable(this);
        
        if (!is_connected && debug.equals("1")){
			Toast toast = Toast.makeText(getApplicationContext(), "Not Connected to the Internet!", Toast.LENGTH_SHORT);
			toast.show();
        }else if (debug.equals("1")){
			Toast toast = Toast.makeText(getApplicationContext(), "Connected to the Internet!", Toast.LENGTH_SHORT);
			toast.show();
        }
	    
		try{
			network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			Log.d(TAG, "NETWORK_PROVIDER enabled");
		}catch(Exception ex){
			Log.e(TAG, "NETWORK_PROVIDER not enabled: " + ex.toString());
		}   

		if (network_enabled){
			mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			Log.d(TAG, "Setting mLocation: " + "lat " + mLocation.getLatitude());
			Log.d(TAG, "Setting mLocation: " + "lon " + mLocation.getLongitude());
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, (float) 5, locationListener); 	
		}else{
			
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setMessage("Cannot determine location: NETWORK_PROVIDER.");
			alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					//Toast.makeText(getApplicationContext(), "OK button clicked", Toast.LENGTH_LONG).show();
				}
			});
			alertbox.show();
			return;
		}
    }

	@Override
    protected void onPause() {
        super.onPause();
       
    }

	@Override
    protected void onStop() {
        super.onStop();  
        mLocationManager.removeUpdates(locationListener);
    }
    
    protected class MyLocationListener implements LocationListener {

	    public void onLocationChanged(android.location.Location location) {
	    	Log.i(TAG, "onLocationChanged lat:" + location.getLatitude() + "lon:" + location.getLongitude());
	    	mLocation = location;
	    	
	    }

	    public void onProviderDisabled(String provider) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }
	}

	@Override
	public void onActivityCreated(Activity arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityDestroyed(Activity arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityPaused(Activity arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityResumed(Activity arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivitySaveInstanceState(Activity arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityStarted(Activity arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityStopped(Activity arg0) {
		
		//mLocationManager.removeUpdates(locationListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/* we need an options menu, not nav bar */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		// Handle item selection
		Log.i(TAG, "item.getItemId():" + item.getItemId());
		Log.i(TAG, "R.string.settings:" +R.string.settings);
		Log.i(TAG, "R.id.action_settings:" +R.string.settings);
		Log.i(TAG, "R.id.post:" +R.string.settings);
		
		if (item.getItemId() == R.id.action_settings)  {

			Log.i(TAG, "action_settings!");
			Intent settingsActivity = new Intent(getBaseContext(),BlockcastPreferenceActivity.class);	
			startActivity(settingsActivity);			
			//finish();
	
		}else if (item.getItemId() == R.id.view_post){
	
			Log.i(TAG, "view_posts selected");
			Intent viewPostsActivity = new Intent(getBaseContext(),ListViewLoader.class);
			startActivity(viewPostsActivity);
			
			finish();
			
		}else if (item.getItemId() ==R.id.post){
	
			Log.i(TAG, "post selected");
			Intent mainActivity = new Intent(getBaseContext(),MainActivity.class);
			startActivity(mainActivity);
			
			//finish();
			
		}
		
		return true;
	}
	
}