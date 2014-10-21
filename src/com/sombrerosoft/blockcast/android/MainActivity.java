package com.sombrerosoft.blockcast.android;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

//import me.blockcast.web.pojo.Location;
import me.blockcast.web.pojo.Post;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sombrerosoft.blockcast.R;
import com.sombrerosoft.blockcast.android.util.Utils;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends Activity {

	private boolean isInForeground;

	private String timeformat = Utils.timeformat;
	private MapView mapView;
	//private ResourceProxyImpl resProxyImp = new ResourceProxyImpl(this);
	private IMapController mapController;
	private ItemizedIconOverlay<OverlayItem> myLocationOverlay;
	private ArrayList<OverlayItem> overlayItemArray;
	private MyLocationNewOverlay mMyLocationOverlay;
	//private GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat df = new SimpleDateFormat(timeformat);  

	private final String TAG = "MainActivity";

	private SharedPreferences prefs;
	private Location mLocation;
	private LocationManager mLocationManager;
	private String SYSTEM_OF_MEASUREMENT = "METRIC";
	private boolean network_enabled = false;
	
	private MyLocationListener locationListener;

	//In an Activity
	private String[] mFileList;
	private File mPath = new File(Environment.getExternalStorageDirectory() + "//yourdir//");
	private String mChosenFile;
	private static final String FTYPE = ".txt";    
	private static final int DIALOG_LOAD_FILE = 1000;
	private GeoPoint mapCenter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		//setContentView();
		df.setTimeZone(TimeZone.getTimeZone("GMT"));  
		locationListener = new MyLocationListener();

	}


	private class NetworkTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... params) {

			String target = params[0];
			String lat = params[1];
			String lon = params[2];
			String parentId = params[3];
			String content  = params[4];
			String distance = params[5];
			String time = params[6];
			String duration = params[7];

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();     
			//builder.setBoundary("+++BOUNDARY+++");
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addTextBody("content", content);
			builder.addTextBody("parentId", parentId);
			builder.addTextBody("time", time);
			builder.addTextBody("duration", duration);
			builder.addTextBody("distance", distance);
			builder.addTextBody("lat", lat);
			builder.addTextBody("lon", lon);

			HttpPost httpPost = new HttpPost(target);
			httpPost.setEntity(builder.build());

			Log.i(TAG, httpPost.getEntity().toString());
			Log.i(TAG, httpPost.getEntity().getContentType().getName() + "***" + httpPost.getEntity().getContentType().getValue());

			java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream((int)httpPost.getEntity().getContentLength());
			
			try {
				httpPost.getEntity().writeTo(out);
			} catch (IOException e1) {
				Log.e(TAG, "IOException:" + e1.toString());
			}
			byte[] entityContentAsBytes = out.toByteArray();
			// or convert to string
			String entityContentAsString = new String(out.toByteArray());

			Log.i(TAG, "entityContentAsString:" + entityContentAsString);

			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			HttpResponse response = null;

			try {
				response = client.execute(httpPost);
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} finally {
				client.close();
			}

			return response;
		}

		@Override
		protected void onPostExecute(HttpResponse result) {
			//Do something with result
			if (result != null){
				InputStream is = null;
				try {
					is = result.getEntity().getContent();
					String response_text = IOUtils.toString(is, "UTF-8");
					//if (isInForeground && Utils.isDebug){
						Toast toast = Toast.makeText(getApplicationContext(), response_text, Toast.LENGTH_SHORT);
						toast.show();
					//}
				} catch (IllegalStateException e) {
					Log.e(TAG, e.toString());
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}				
			}
		}
	}
	
	@Override
	protected void onPause() {
        isInForeground = false;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		isInForeground = true;
		
		setContentView(R.layout.activity_viewposts);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		try{
			network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			Log.d(TAG, "NETWORK_PROVIDER enabled");
		}catch(Exception ex){
			Log.e(TAG, "NETWORK_PROVIDER not enabled: " + ex.toString());
		}   

		if (network_enabled){
			mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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

		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setTileSource(TileSourceFactory.MAPNIK);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		mapController = mapView.getController();
		mapController.setZoom(18);

		// geolocate 
		/*
		 * https://code.google.com/p/osmdroid/source/browse/trunk/osmdroid-android/src/main/java/org/osmdroid/views/overlay/MyLocationOverlay.java?r=1123
		 */
		
		mapCenter = new GeoPoint((int)(mLocation.getLatitude() * 1e6), (int)( mLocation.getLongitude() * 1e6));
		Log.d(TAG, "" + mLocation.getLatitude() + " " +  mLocation.getLongitude() );
		Log.d(TAG, "" + (int)(mLocation.getLatitude() * 1e6) + " " +  (int)( mLocation.getLongitude() * 1e6) );
		//GeoPoint overlayPoint = new GeoPoint((mLocation.getLongitude() * 1e6) + 1000, (mLocation.getLatitude() * 1e6) + 1000);

		overlayItemArray = new ArrayList<OverlayItem>();

		overlayItemArray.add(new OverlayItem("New Overlay", "Overlay Description", mapCenter));

		DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(this);
		this.myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(overlayItemArray, null, resourceProxy);
		this.mapView.getOverlays().add(this.myLocationOverlay);

		//mapView.invalidate();
		
		mapView = (MapView) findViewById(R.id.mapview);

		//enable zoom controls
		mapView.setBuiltInZoomControls(true);

		//enable multitouch
		mapView.setMultiTouchControls(true);
		//GpsMyLocationProvider can be replaced by your own class. It provides the position information through GPS or Cell towers.
		GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
		//minimum distance for update
		imlp.setLocationUpdateMinDistance(1000);
		//minimum time for update
		imlp.setLocationUpdateMinTime(60000);       
		mMyLocationOverlay = new MyLocationNewOverlay(this.getBaseContext(),imlp , mapView);
		//mMyLocationOverlay.setUseSafeCanvas(false);
		mMyLocationOverlay.setDrawAccuracyEnabled(true);

		
		mapView.getOverlays().add(mMyLocationOverlay);
		
		if (mMyLocationOverlay.getMyLocation() != null){
			mapController.setCenter(mMyLocationOverlay.getMyLocation());
		}else{
			mapController.setCenter(mapCenter);
		}
		//mapController.setCenter(mMyLocationOverlay.getMyLocation());
		
		final Button post = (Button) findViewById(R.id.button_post);
		final Button upload = (Button) findViewById(R.id.button_upload);
		//final TextView text = (TextView) findViewById(R.id.textView1);
		final EditText text = (EditText) findViewById(R.id.editText1);
		post.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if ((text.getText().toString() != null) && (text.getText().toString().length() > 0)){
					
					long distance = Long.parseLong(prefs.getString("DISTANCE", "100"));
					long duration = Long.parseLong(prefs.getString("DURATION", "3600"));
					
					Post post = new Post();
					me.blockcast.web.pojo.Location l = new me.blockcast.web.pojo.Location();
					
					l.setLat(mLocation.getLatitude());
					l.setLon(mLocation.getLongitude());
					post.setLocation(l);
					post.setParentId(-1);
					post.setContent(text.getText().toString());
					post.setDistance(distance);
					post.setPostTimestamp(new Date());
					post.setDuration(duration);
	
					String reqstring = Utils.protocol + "://" + Utils.servername + Utils.api + "insertPost/" ;
	
					Log.i("MAINACTIVITY", "reqstring:" + reqstring);

				
					new NetworkTask().execute(reqstring, ""+post.getLocation().getLat(), ""+post.getLocation().getLon(),
							""+post.getParentId(), post.getContent(), ""+post.getDistance(), df.format(post.getPostTimestamp()), 
							""+post.getDuration()
							);
	
					text.setText("");
				}else{
					Toast.makeText(getApplicationContext(), "Can't post empty content! Please type some text.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
		
		upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// open modal dialog box
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				// Add the buttons
				builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               // User clicked OK button
				           }
				       });
				builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               // User cancelled the dialog
				           }
				       });
				// Set other dialog properties


				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
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
		// Handle item selection
		Log.i("MAINACTIVITY", "item.getItemId():" + item.getItemId());
		Log.i("MAINACTIVITY", "R.string.settings:" +R.string.settings);
		switch (item.getItemId()) {
		case R.id.action_settings:
		{
			Log.i("MAINACTIVITY", "action_settings!");
			Intent settingsActivity = new Intent(getBaseContext(),BlockcastPreferenceActivity.class);
			//Intent settingsActivity = new Intent(getBaseContext(),SampleLoader.class);
			startActivity(settingsActivity);
			break;
		}
		case R.string.view_posts:
		{
			Log.i("MAINACTIVITY", "view_posts selected");
			//Intent viewPostsActivity = new Intent(getBaseContext(),ViewPostsActivity.class);
			//startActivity(viewPostsActivity);
			break;
		}
		}
		return true;
	}

	/*// file upload 
	public class FireMissilesDialogFragment extends DialogFragment {
		protected Dialog onCreateDialog(int id) {
		    Dialog dialog = null;
		    AlertDialog.Builder builder = new Builder(getApplicationContext());

		    switch(id) {
		        case DIALOG_LOAD_FILE:
		            builder.setTitle("Choose your file");
		            if(mFileList == null) {
		                Log.e(TAG, "Showing file picker before loading the file list");
		                dialog = builder.create();
		                return dialog;
		            }
		            builder.setItems(mFileList, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int which) {
		                    mChosenFile = mFileList[which];
		                    //you can do stuff with the file here too
		                }
		            });
		            break;
		    }
		    dialog = builder.show();
		    return dialog;
		}
	}
	private void loadFileList() {
	    try {
	        mPath.mkdirs();
	    }
	    catch(SecurityException e) {
	        Log.e(TAG, "unable to write on the sd card " + e.toString());
	    }
	    if(mPath.exists()) {
	        FilenameFilter filter = new FilenameFilter() {
	            public boolean accept(File dir, String filename) {
	                File sel = new File(dir, filename);
	                return filename.contains(FTYPE) || sel.isDirectory();
	            }
	        };
	        mFileList = mPath.list(filter);
	    }
	    else {
	        mFileList= new String[0];
	    }
	}
*/
	
	
	public class MyLocationListener implements LocationListener {

	    public void onLocationChanged(android.location.Location location) {
	    	mLocation = location;
	    	//if (isInForeground && Utils.isDebug){
	    		Toast.makeText(getApplicationContext(), "acquired your new location @ (" + mLocation.getLatitude() + "lat, " +  mLocation.getLongitude() + "lon)", Toast.LENGTH_LONG).show();
	    	//}
	    	mapCenter = new GeoPoint((int)(mLocation.getLatitude() * 1e6), (int)( mLocation.getLongitude() * 1e6));
	    	mapView.invalidate();
	    	mapController.setCenter(mapCenter);
	    	//if (isInForeground && Utils.isDebug){
	    		Toast.makeText(getApplicationContext(), "revalidated mapview", Toast.LENGTH_LONG).show();
	    	//}
	    }

	    public void onProviderDisabled(String provider) {
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    }
	}

}


