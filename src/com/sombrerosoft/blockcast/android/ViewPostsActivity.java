package com.sombrerosoft.blockcast.android;

import java.util.List;
import java.util.Locale;

import com.sombrerosoft.blockcast.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

public class ViewPostsActivity  extends Activity {
	private Location mLocation;
	private LocationManager mLocationManager;
	private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	private String TAG = "ViewPostsActivity";
	private String SYSTEM_OF_MEASUREMENT = "METRIC";
	private boolean network_enabled = false;
    private String REFRESH_DELAY_MS = "30000";
    private int ms = 5000;
    private ProgressDialog m_ProgressDialog = null;
    private TextView lastupdated;
    
    @Override    
	public void onResume() {	
		super.onResume();
		
		setContentView(R.layout.location_layout);
		//lv = (ListView)findViewById(R.id.gridview);
		lastupdated = (TextView)findViewById(R.id.lastupdated);
		
		m_ProgressDialog = new ProgressDialog(this);
		m_ProgressDialog.setTitle("Please wait...");
		m_ProgressDialog.setMessage("Retrieving data ...");
		m_ProgressDialog.setCancelable(true);
		m_ProgressDialog.show();
		
		 mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			try{
				network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				//Log.d(TAG, "NETWORK_PROVIDER enabled");
			}catch(Exception ex){
				Log.e(TAG, "NETWORK_PROVIDER not enabled: " + ex.toString());
			}   

			if (network_enabled){
				mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ms, (float) 5, myLocL); 	
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
    
    private final LocationListener myLocL = new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {	
			mLocation = location;
			
	        Thread thread = new Thread(mUpdateTimeTask);
	        thread.start();
	        
		}
		@Override
		public void onProviderDisabled(String provider) {

		}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}	
	};
	
	private Runnable mUpdateTimeTask = new Runnable() {
		
		@Override
		public void run() {

			//getOrders();
			
			//mHandler.post(mUpdateResults);
			/*
			// this differs from station activity:
			// we want updates on position changed, not time!
			
			//mHandler.handleMessage(null);
			//mHandler.postDelayed(mUpdateTimeTask, ms);
			 * 
			 */
		}
	};
	
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateResultsInUi();
        }
    };
    
 private void updateResultsInUi() {
    	
    	if (m_ProgressDialog != null){
    		m_ProgressDialog.dismiss();
    	}
    	
    	/*
    	String firstRunS = "";
		if (firstRun){
			firstRunS += "Using last known location (update pending):\n";
			firstRun = false;
		}else{
			firstRunS += "Detected location:\n";
		}
		*/
        // Back in the UI thread -- update our UI elements based on the data in mResults
	
		String location = "";
		if (mLocation != null){
				
			List<Address> addresses;
			
			try{	
				Geocoder gc = new Geocoder(this, Locale.getDefault());
				addresses = gc.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
				
				if (addresses != null){
					Address currentAddress = addresses.get(0);
					StringBuilder sb = new StringBuilder("");
					for (int i=0; i <currentAddress.getMaxAddressLineIndex(); i++){
						sb.append(currentAddress.getAddressLine(i)).append("\n");
					}
					location+=sb.toString();
				}
				
			}catch(Exception e){
				Log.e(TAG, e.toString());
				// no need to let the user know! 
				//location+="[error locating your street address]\n";
			}
			

			location += "[" + mLocation.getLatitude() + " lat, " + mLocation.getLongitude() + " lon]";
		}

		lastupdated.setText(location + "\nData refreshed as your position changes.");
		
		/*
		if(m_orders != null && m_orders.size() > 0){
			m_adapter.notifyDataSetChanged();
			m_adapter.clear();
			for(int i=0;i<m_orders.size();i++){
				m_adapter.add(m_orders.get(i));
			}               
		}

		m_adapter.notifyDataSetChanged();
		*/
    }

}
