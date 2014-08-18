/*

package com.sombrerosoft.blockcast.android;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.location.Geocoder;
import android.location.Location;


public class LocationActivity extends Activity {

	private ListView lv;
	private TextView lastupdated;
	private ProgressDialog m_ProgressDialog = null;
	//private ArrayList<Station> m_orders = null;
	private OrderAdapter m_adapter;
	private String lineCode = null;
	private Location mLocation;
	private LocationManager mLocationManager;
	private String SYSTEM_OF_MEASUREMENT = "METRIC";
	private boolean network_enabled = false;
    private String REFRESH_DELAY_MS = "30000";
    private int ms = 30000;
    private boolean firstRun = true;
	boolean isReady = true;

	private Handler mHandler = new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	    	super.handleMessage(msg);
	    }
	};

	private String FORMAT;
	private String TAG = "LocationActivityHandler";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);   
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		
		@Override
		public void run() {

			getOrders();
			
			mHandler.post(mUpdateResults);
			// this differs from station activity:
			// we want updates on position changed, not time!
			
			//mHandler.handleMessage(null);
			//mHandler.postDelayed(mUpdateTimeTask, ms);
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
    	
    	String firstRunS = "";
		if (firstRun){
			firstRunS += "Using last known location (update pending):\n";
			firstRun = false;
		}else{
			firstRunS += "Detected location:\n";
		}
		
        // Back in the UI thread -- update our UI elements based on the data in mResults

		String location = "";
		if (mLocation != null){
				
			List<Address> addresses;
			
			try{	
				Geocoder gc = new Geocoder(this, Locale.getDefault());
				addresses = gc.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
				
				if (addresses != null){
					Address currentAddress = addresses.get(0);
					StringBuilder sb = new StringBuilder(firstRunS);
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
		
		if(m_orders != null && m_orders.size() > 0){
			m_adapter.notifyDataSetChanged();
			m_adapter.clear();
			for(int i=0;i<m_orders.size();i++){
				m_adapter.add(m_orders.get(i));
			}               
		}

		m_adapter.notifyDataSetChanged();
    }

	@Override    
	public void onResume() {	
		super.onResume();

		setContentView(R.layout.my_location);
		lv = (ListView)findViewById(R.id.gridview);
		lastupdated = (TextView)findViewById(R.id.lastupdated);
		
		m_ProgressDialog = new ProgressDialog(this);
		m_ProgressDialog.setTitle("Please wait...");
		m_ProgressDialog.setMessage("Retrieving data ...");
		m_ProgressDialog.setCancelable(true);
		m_ProgressDialog.show();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		FORMAT = prefs.getString("RESULT_FORMAT", "TABLE"); 
    	REFRESH_DELAY_MS = prefs.getString("REFRESH_DELAY_MS", "30000");
		SYSTEM_OF_MEASUREMENT = prefs.getString("MEASUREMENT_TYPE", "METRIC");
		
		try{
			ms = Integer.parseInt(REFRESH_DELAY_MS);
		}catch(Exception ex){
			Log.e(TAG, "cannot parse REFRESH_DELAY_MS" + ex.toString());
		}
			
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

		//this.lv = (ListView)this.findViewById();  
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent newIntent;

				if (FORMAT.equalsIgnoreCase("TEXT")){
					//Log.d(TAG, " FORMAT " + FORMAT );
					newIntent = new Intent(LocationActivity.this, TimeOfArrivalActivityText.class);
				}else{
					//Log.d(TAG, " FORMAT " + FORMAT );
					newIntent = new Intent(LocationActivity.this, TimeOfArrivalActivity.class);
				}

				Station s = (Station) lv.getItemAtPosition(arg2);

				newIntent.putExtra("lineCode", lineCode);
				newIntent.putExtra("stationCode", s.getCode());
				newIntent.putExtra("stationName", s.getName());

				m_orders = null;

				startActivity(newIntent);

			}
		});

		m_orders = new ArrayList<Station>();
		this.m_adapter = new OrderAdapter(this, R.layout.closest_row, m_orders);
		lv.setAdapter(this.m_adapter);  
		
        Thread thread = new Thread(mUpdateTimeTask);
        thread.start();
       
	}

	@SuppressWarnings("unchecked")
	private void getOrders(){

		try{        	  

			// 1) parse all lines, get line codes
			// 2) parse all line codes, get each closest station
			// 3) add to arraylist and return

			XmlPullFeedParser xmlpull = XmlPullFeedParser.getXmlPullFeedParser(this);
			ArrayList<Line> lines = (ArrayList<Line>)  xmlpull.parseHardCodedLines(this);
			ArrayList<Station> s =  new ArrayList<Station>();


			float shortest_distance = Float.MAX_VALUE;
			Station closest_station = null;

			for (int i = 0; i < lines.size();i++){

				float[] results = new float[3];

				ArrayList<Station> stations = (ArrayList<Station>) xmlpull.parseHardCodedStationsByLine(this, lines.get(i).getLineCode());

				for (int j = 0; j < stations.size(); j++){

					Location.distanceBetween(mLocation.getLatitude(), mLocation.getLongitude(), stations.get(j).getLat(), stations.get(j).getLon(), results);
					stations.get(j).setDistanceFromMe(results[0]);


					Location endPoint = new Location("");
					endPoint.setLatitude(stations.get(j).getLat());
					endPoint.setLongitude(stations.get(j).getLon());

					float bearing = mLocation.bearingTo(endPoint); 

					if (bearing<0) {
						bearing = 360+bearing; 
					}

					stations.get(j).setBearingTo(bearing);

					if ((float)results[0] < shortest_distance){
						shortest_distance = (float)results[0];
						closest_station = stations.get(j);
					}
				}
				shortest_distance = Float.MAX_VALUE;

				boolean f = false;
				for (int k = 0; k < s.size(); k++){
					if (s.get(k).getName().equalsIgnoreCase(closest_station.getName())){
						f = true;
						break;
					}
				}
				if (!f){
					s.add(closest_station);
				}

				//Log.d(TAG ,"add: " + closest_station);
			}

			Collections.sort(s, new Comparator(){		 
				public int compare(Object o1, Object o2) {
					Station p1 = (Station) o1;
					Station p2 = (Station) o2;
					return (Float.compare(p1.getDistanceFromMe(), p2.getDistanceFromMe()));
				}
			}
			);

			m_orders = s;
			
		} catch (Exception e) {
			Log.e(TAG, "BACKGROUND_PROC " + e.getLocalizedMessage());
		}
	}

	private class OrderAdapter extends ArrayAdapter<Station> {

		private ArrayList<Station> items;

		public OrderAdapter(Context context, int textViewResourceId, ArrayList<Station> mOrders) {
			super(context, textViewResourceId, mOrders);
			this.items = mOrders;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.closest_row, null);
			}
			
			Station o = items.get(position);

			if (o != null) {

				TextView tt = (TextView) v.findViewById(R.id.toptext);
				String text = "";

				float[] results = new float[3];            	
				Location.distanceBetween(mLocation.getLatitude(), mLocation.getLongitude(), o.getLat(), o.getLon(), results);

				Location endPoint = new Location("");
				endPoint.setLatitude(o.getLat());
				endPoint.setLongitude(o.getLon());

				float bearing = mLocation.bearingTo(endPoint);

				o.setBearingTo(bearing);
				o.setDistanceFromMe(results[0]);

				if (tt != null) {

					if (SYSTEM_OF_MEASUREMENT.equalsIgnoreCase("METRIC")){

						double meters = o.getDistanceFromMe();
						DecimalFormat twoDForm = new DecimalFormat("#.##");

						if (meters > 1000){ 			
							double kilometers = meters/1000;    		
							kilometers = Double.valueOf(twoDForm.format(kilometers));
							text += "&nbsp;" + o.getName() + "<br>&nbsp;" + kilometers + " km"; 
						}else{
							text += "  " + o.getName() + "<br>&nbsp;" + (int) meters + " m"; 
						}

					}else{
						double feet = (float) (o.getDistanceFromMe() * 3.2808399);

						DecimalFormat twoDForm = new DecimalFormat("#.##");
						double miles = (feet/5280);

						text += "&nbsp;" + o.getName() + "<br>&nbsp;";

						if (miles > 1){

							miles = Double.valueOf(twoDForm.format(miles));
							text += miles + " mi.";
						}else{
							feet = Double.valueOf(twoDForm.format(feet));
							text += (int) feet + " ft.";
						}

					}

					text += "   ";

					if (o.getLineCode1() != null){
						if (o.getLineCode1().equalsIgnoreCase("GR")){
							text+="<img src='icon_marble_green'>";
						}else if (o.getLineCode1().equalsIgnoreCase("YL")){
							text+="<img src='icon_marble_yellow'>";
						}else if (o.getLineCode1().equalsIgnoreCase("RD")){
							text+="<img src='icon_marble_red'>";
						}else if (o.getLineCode1().equalsIgnoreCase("BL")){
							text+="<img src='icon_marble_blue'>";
						}else if (o.getLineCode1().equalsIgnoreCase("OR")){
							text+="<img src='icon_marble_orange'>";
						}
					}

					if (o.getLineCode2() != null){
						if (o.getLineCode2().equalsIgnoreCase("GR")){
							text+="<img src='icon_marble_green'>";
						}else if (o.getLineCode2().equalsIgnoreCase("YL")){
							text+="<img src='icon_marble_yellow'>";
						}else if (o.getLineCode2().equalsIgnoreCase("RD")){
							text+="<img src='icon_marble_red'>";
						}else if (o.getLineCode2().equalsIgnoreCase("BL")){
							text+="<img src='icon_marble_blue'>";
						}else if (o.getLineCode2().equalsIgnoreCase("OR")){
							text+="<img src='icon_marble_orange'>";
						}            	
					}

					if (o.getLineCode3()!=null){
						if (o.getLineCode3().equalsIgnoreCase("GR")){
							text+="<img src='icon_marble_green'>";
						}else if (o.getLineCode3().equalsIgnoreCase("YL")){
							text+="<img src='icon_marble_yellow'>";
						}else if (o.getLineCode3().equalsIgnoreCase("RD")){
							text+="<img src='icon_marble_red'>";
						}else if (o.getLineCode3().equalsIgnoreCase("BL")){
							text+="<img src='icon_marble_blue'>";
						}else if (o.getLineCode3().equalsIgnoreCase("OR")){
							text+="<img src='icon_marble_orange'>";
						}      		
					}

					if (o.getLineCode4()!=null){
						if (o.getLineCode4().equalsIgnoreCase("GR")){
							text+="<img src='icon_marble_green'>";
						}else if (o.getLineCode4().equalsIgnoreCase("YL")){
							text+="<img src='icon_marble_yellow'>";
						}else if (o.getLineCode4().equalsIgnoreCase("RD")){
							text+="<img src='icon_marble_red'>";
						}else if (o.getLineCode4().equalsIgnoreCase("BL")){
							text+="<img src='icon_marble_blue'>";
						}else if (o.getLineCode4().equalsIgnoreCase("OR")){
							text+="<img src='icon_marble_orange'>";
						}		
					}

					tt.setText(Html.fromHtml(text, new ImageGetter() {                 
						@Override
						public Drawable getDrawable(String source) {
							Drawable drawFromPath;
							int path = LocationActivity.this.getResources().getIdentifier(source, "drawable", "com.sombrerosoft.android.wmata"); 
							drawFromPath = (Drawable) LocationActivity.this.getResources().getDrawable(path);
							drawFromPath.setBounds(0, 0, drawFromPath.getIntrinsicWidth(), drawFromPath.getIntrinsicHeight());
							return drawFromPath;
						}
					}, null));	
				}
			}
			return v;
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

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mLocationManager.removeUpdates(myLocL);

	}	
	
	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mLocationManager.removeUpdates(myLocL);
	}	
	
    @Override    
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.app_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.string.GPS_closest:
        {
        	Intent settingsActivity = new Intent(getBaseContext(), LocationActivity.class);
        	startActivity(settingsActivity);
        }
        return true;
        case R.string.about:
        {
        	String app_ver = "";
        	
        	try{
        		app_ver = " v" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        	}
        	catch (NameNotFoundException e){
        	    Log.v(TAG, e.getMessage());
        	}
        	
        	String about = "wdc metro locator app" + app_ver + "\nsupport@amsoftgroup.com";
        	
            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            alertbox.setMessage(about);
            alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                //    Toast.makeText(getApplicationContext(), "OK button clicked", Toast.LENGTH_LONG).show();
                }
            });
            alertbox.show();
        }
        return true;
        case R.string.status:
        {
        	Intent settingsActivity = new Intent(getBaseContext(), WebStatusActivity.class);
        	startActivity(settingsActivity);
        }
        return true;
        case R.string.settings:
        {
        	Intent settingsActivity = new Intent(getBaseContext(), WMATAPreferenceActivity.class);
        	startActivity(settingsActivity);
        }
        return true;
        case R.string.voice:
        {
        	Intent settingsActivity = new Intent(getBaseContext(), VoiceStartActivity.class);
        	startActivity(settingsActivity);
        }
        return true;
        default:
            return super.onOptionsItemSelected(item);
        }


    } 
}
*/