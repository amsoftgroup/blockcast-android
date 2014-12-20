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
import me.blockcast.common.Post;

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
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.sombrerosoft.blockcast.R;
import com.sombrerosoft.blockcast.android.util.Utils;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.BasicInfoWindow;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends BlockcastBaseActivity {

	private String timeformat = Utils.timeformat;
	private MapView mapView;
	//private ResourceProxyImpl resProxyImp = new ResourceProxyImpl(this);
	private IMapController mapController;
	private ItemizedIconOverlay<OverlayItem> myLocationOverlay;
	private ArrayList<OverlayItem> overlayItemArray;
	private MyLocationNewOverlay mMyLocationOverlay;
	private BoundingBoxE6 bbox;
	//private GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
	private static final int REQUEST_CHOOSER = 1234;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat df = new SimpleDateFormat(timeformat);  

	private final String TAG = "MainActivity";
	private SharedPreferences prefs;
	private String SYSTEM_OF_MEASUREMENT = "METRIC";
	private String filepath = null;
	private String postcontent = null;
	private File file = null;


	private GeoPoint mapCenter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	private class NetworkTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... params) {

			String target = params[0];
			String lat = params[1];
			String lon = params[2];
			String parentId = params[3];
			postcontent  = params[4];
			String distance = params[5];
			String time = params[6];
			String duration = params[7];
			filepath = params[8];
			String guid = params[9];
			
			Log.i(TAG, "GUID:"+guid);
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();     
			//builder.setBoundary("+++BOUNDARY+++");
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addTextBody("content", postcontent);
			builder.addTextBody("parentId", parentId);
			builder.addTextBody("time", time);
			builder.addTextBody("duration", duration);
			builder.addTextBody("distance", distance);
			builder.addTextBody("lat", lat);
			builder.addTextBody("lon", lon);
			builder.addTextBody("guid", guid);
			if (filepath != null){
				File f = new File(filepath);
				builder.addBinaryBody("file", f, ContentType.APPLICATION_OCTET_STREAM, f.getName());
			}else{
				//builder.addBinaryBody("file", null);
			}
			
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
//			byte[] entityContentAsBytes = out.toByteArray();
			// or convert to string
			String entityContentAsString = new String(out.toByteArray());

			Log.i(TAG, "entityContentAsString:" + entityContentAsString);

			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			HttpResponse response = null;

			try {
				response = client.execute(httpPost);
				//filepath = null;
				//postcontent = null;				
			} catch (IOException e) {
				Log.e(TAG, e.toString());

			} finally {
				client.close();
			}
			filepath = null;
			postcontent = null;
			
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
					Log.e(TAG, "debug = " + debug);
					if ((debug != null) && (debug.equals("1"))){
						Toast toast = Toast.makeText(getApplicationContext(), response_text, Toast.LENGTH_SHORT);
						toast.show();
					}
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
		super.onPause();
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.invalidate();
	}

	@Override
	protected void onResume() {
		super.onResume();

		setContentView(R.layout.activity_viewposts);
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.invalidate();
		
		final Projection pj = mapView.getProjection();
		
		Log.i(TAG, "Projection = " + pj.getNorthEast().toString() + " " + pj.getSouthWest().toString());
		mapController = mapView.getController();
		mapController.setZoom(17);

		mapView.setTileSource(TileSourceFactory.MAPNIK);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		
		bbox = mapView.getBoundingBox();
		Log.i(TAG, bbox.toString());
		// geolocate 
		// https://code.google.com/p/osmdroid/source/browse/trunk/osmdroid-android/src/main/java/org/osmdroid/views/overlay/MyLocationOverlay.java?r=1123

		mapCenter = new GeoPoint((int)(mLocation.getLatitude() * 1e6), (int)( mLocation.getLongitude() * 1e6));
		Log.i(TAG, "mLocation.getLatitude() * 1e6 = " + mLocation.getLatitude());
		Log.i(TAG, "mLocation.getLongitude() * 1e6 = " + mLocation.getLongitude());
		mapController.setCenter(mapCenter);
		//mapController.animateTo(mapCenter);

		//NE corner is post lat lon! we need to recenter.
		bbox = mapView.getBoundingBox();
		Log.i(TAG, bbox.toString());
		
		Polygon circle = new Polygon(this);
		circle.setPoints(Polygon.pointsAsCircle(mapCenter, distance));
		circle.setFillColor(0x12121212);
		circle.setStrokeColor(Color.RED);
		circle.setStrokeWidth(2);
		this.mapView.getOverlays().add(circle);
		
		overlayItemArray = new ArrayList<OverlayItem>();
		OverlayItem item = new OverlayItem("center", "MapCenter", mapCenter);
		item.setMarkerHotspot(HotspotPlace.BOTTOM_CENTER);
		overlayItemArray.add(item);	       

		DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(this);
		this.myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(overlayItemArray, null, resourceProxy);
		this.mapView.getOverlays().add(this.myLocationOverlay);

		//mapView.invalidate();
		//mapView.setBuiltInZoomControls(true);
		//mapView.setMultiTouchControls(true);	

		final Button post = (Button) findViewById(R.id.button_post);
		final Button upload = (Button) findViewById(R.id.button_upload);
		//final TextView text = (TextView) findViewById(R.id.textView1);
		final EditText text = (EditText) findViewById(R.id.editText1);
		
		mapController.setCenter(mapCenter);
		
		mapView.invalidate();
		
		if (postcontent != null){
			text.setText(postcontent);
		}
		
/* 
 * 		TODO: see https://groups.google.com/forum/#!topic/osmdroid/PCeSzz-Tg_E 
 * 		this may change in future releases
 */
		
		ViewTreeObserver vto = mapView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
		    @Override
		    public void onGlobalLayout() {
		    	mapView.getController().setCenter(mapCenter);
		    }
		});
		
		post.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if ((text.getText().toString() != null) && (text.getText().toString().length() > 0)){
					
					postcontent = text.getText().toString();
					Post post = new Post();
					me.blockcast.common.Location l = new me.blockcast.common.Location();

					l.setLat(mLocation.getLatitude());
					l.setLon(mLocation.getLongitude());
					post.setLocation(l);
					post.setParentId(-1);
					post.setContent(text.getText().toString());
					post.setDistance(distance);
					post.setEpoch((new Date().getTime())/1000l);
					post.setDuration(duration);
					if (filepath !=null){
						post.setMedia_file(filepath);
					}
					String reqstring = Utils.protocol + "://" + Utils.servername + Utils.api + "insertPost/" ;
					Log.i(TAG, "reqstring:" + reqstring);
					Log.i(TAG, "GUID:" + Installation.id(getBaseContext()));
					
					new NetworkTask().execute(reqstring, ""+post.getLocation().getLat(), ""+post.getLocation().getLon(),
							""+post.getParentId(), post.getContent(), ""+post.getDistance(), "" + post.getEpoch(), 
							""+post.getDuration(), post.getMedia_file(), Installation.id(getBaseContext()));

					text.setText("");
				}else{
					Toast.makeText(getApplicationContext(), "Can't post empty content! Please type some text.", Toast.LENGTH_SHORT).show();
				}
				
				// send to view posts?
				Log.i(TAG, "viewing posts");
				Intent viewPostsActivity = new Intent(getBaseContext(),ListViewLoader.class);
				startActivity(viewPostsActivity);
			}
		});	

		upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if ((text.getText().toString() != null) && (text.getText().toString().length() > 0)){
					postcontent = text.getText().toString();
				}
				// Create the ACTION_GET_CONTENT Intent
				Intent getContentIntent = FileUtils.createGetContentIntent();

				Intent intent = Intent.createChooser(getContentIntent, "Select a file");
				startActivityForResult(intent, REQUEST_CHOOSER);

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CHOOSER:   
			if (resultCode == RESULT_OK) {

				final Uri uri = data.getData();

				// Get the File path from the Uri
				filepath = FileUtils.getPath(this, uri);
				Log.i(TAG, "path=" + filepath);

				// Alternatively, use FileUtils.getFile(Context, Uri)
				if (filepath != null && FileUtils.isLocal(filepath)) {
					file = new File(filepath);
					Log.i(TAG, "filesize=" + file.getTotalSpace());
				}
			}
			break; 
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/* we need an options menu, not nav bar */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// Handle item selection

		if (item.getItemId() == R.id.action_settings) {
			Log.i(TAG, "action_settings!");
			Intent settingsActivity = new Intent(getBaseContext(),BlockcastPreferenceActivity.class);
			startActivity(settingsActivity);
		}else if (item.getItemId() == R.id.view_post){
			Log.i(TAG, "view_posts selected");
			Intent viewPostsActivity = new Intent(getBaseContext(),ListViewLoader.class);
			startActivity(viewPostsActivity);
		}
		else if (item.getItemId() == R.id.post){
			Log.i(TAG, "post selected");
			Intent mainActivity = new Intent(getBaseContext(),MainActivity.class);
			startActivity(mainActivity);
		}

		return true;
	}


}
