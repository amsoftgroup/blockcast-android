package com.sombrerosoft.blockcast.android;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import me.blockcast.web.pojo.Location;
import me.blockcast.web.pojo.Post;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends Activity {

	private String timeformat = Utils.timeformat;
	private MapView mapView;
	//private ResourceProxyImpl resProxyImp = new ResourceProxyImpl(this);
	private IMapController mapController;
    private ResourceProxyImpl resProxyImp;
    private ItemizedIconOverlay<OverlayItem> myLocationOverlay;
	private MyLocationNewOverlay mMyLocationOverlay;
	//private GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat df = new SimpleDateFormat(timeformat);  
	//private final static String servername = "http://192.168.1.3:8080/blockcastweb";
	private final static String servername = "http://www.blockcast.me";
	private final String TAG = "MainActivity";
	//private final static String servername = "localhost:8080/blockcastweb";
	//private final String api = "/restapi/insertPost/{lon}/{lat}/{parent_id}";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//setContentView();
		df.setTimeZone(TimeZone.getTimeZone("GMT"));  
	}


	private class NetworkTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... params) {

			String target = params[0];
			String lon = params[1];
			String lat = params[2];
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
			builder.addTextBody("lon", lon);
			builder.addTextBody("lat", lat);
		
			HttpPost httpPost = new HttpPost(target);
			httpPost.setEntity(builder.build());
			
			Log.i(TAG, httpPost.getEntity().toString());
			
			Log.i(TAG, httpPost.getEntity().getContentType().getName() + "***" + httpPost.getEntity().getContentType().getValue());
		
			/*
			String myString = null;
			
			try {
				myString = IOUtils.toString(httpPost.getEntity().getContent());
			} catch (IllegalStateException e1) {
				Log.e(TAG, "IllegalStateException:" + e1.toString());
			} catch (IOException e1) {
				Log.e(TAG, "IOException:" + e1.toString());
			}
			
			Log.e(TAG, "myString:" + myString);
			*/
			
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
			//if (result != null)
			//	result.getEntity().writeTo(new FileOutputStream(f));
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.activity_viewposts);

	     mapView = (MapView) findViewById(R.id.mapview);
         mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
         mapView.setBuiltInZoomControls(true);
         mapView.setMultiTouchControls(true);

         mapController = mapView.getController();
         mapController.setZoom(15);
        
         GeoPoint mapCenter = new GeoPoint(53554070, -2959520);
 GeoPoint overlayPoint = new GeoPoint(53554070 + 1000, -2959520 + 1000);
 mapController.setCenter(mapCenter);

 ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
 overlays.add(new OverlayItem("New Overlay", "Overlay Description", overlayPoint));

 DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(this);
 this.myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(overlays, null, resourceProxy);
 this.mapView.getOverlays().add(this.myLocationOverlay);

 mapView.invalidate();
		final Button button = (Button) findViewById(R.id.button1);
		//final TextView text = (TextView) findViewById(R.id.textView1);
		final EditText text = (EditText) findViewById(R.id.editText1);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Post post = new Post();
				Location l = new Location();
				l.setLat(38.8951);
				l.setLon(77.036);
				post.setLocation(l);
				post.setParentId(-1);
				post.setContent(text.getText().toString());
				post.setDistance(100);
				post.setPostTimestamp(new Date());
				post.setDuration(3600);
				
				String reqstring = servername + "/restapi/insertPost/" ;
				/*
				String reqstring = servername + "/restapi/insertPost/" + 
						post.getLocation().getLon() + "/" + 
						post.getLocation().getLat() + "/" + 
						post.getParentId();
				*/

				Log.i("MAINACTIVITY", "reqstring:" + reqstring);
				
				new NetworkTask().execute(reqstring, ""+post.getLocation().getLon(), ""+post.getLocation().getLat(),
						""+post.getParentId(), post.getContent(), ""+post.getDistance(), df.format(post.getPostTimestamp()), 
						""+post.getDuration()
						);

				
			}

		 });
		 

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    	// Handle item selection
			Log.i("MAINACTIVITY", "item.getItemId():" + item.getItemId());
			Log.i("MAINACTIVITY", "R.string.settings:" +R.string.settings);
	    	switch (item.getItemId()) {
	        case R.id.action_settings:
		        {
		        	Log.i("MAINACTIVITY", "R.string.settings:");
		        	Intent settingsActivity = new Intent(getBaseContext(),BlockcastPreferenceActivity.class);
		        	startActivity(settingsActivity);
		        }
	        case R.string.view_posts:
	        {
	        	Log.i("MAINACTIVITY", "R.string.settings:");
	        	Intent viewPostsActivity = new Intent(getBaseContext(),ViewPostsActivity.class);
	        	startActivity(viewPostsActivity);
	        }
	    	}
	    	return true;
	 }
	

}
