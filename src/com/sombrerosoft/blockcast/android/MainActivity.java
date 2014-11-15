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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
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

public class MainActivity extends BlockcastBaseActivity {

	private String timeformat = Utils.timeformat;
	private MapView mapView;
	//private ResourceProxyImpl resProxyImp = new ResourceProxyImpl(this);
	private IMapController mapController;
	private ItemizedIconOverlay<OverlayItem> myLocationOverlay;
	private ArrayList<OverlayItem> overlayItemArray;
	private MyLocationNewOverlay mMyLocationOverlay;
	//private GpsMyLocationProvider imlp = new GpsMyLocationProvider(this.getBaseContext());
	private static final int REQUEST_CHOOSER = 1234;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat df = new SimpleDateFormat(timeformat);  

	private final String TAG = "MainActivity";
	private SharedPreferences prefs;
	private String SYSTEM_OF_MEASUREMENT = "METRIC";
	private String filepath = null;
	private File file = null;


	private GeoPoint mapCenter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		//setContentView();
		/*
		df.setTimeZone(TimeZone.getTimeZone("GMT"));  
		locationListener = new MyLocationListener();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		distance = Long.parseLong(prefs.getString("DISTANCE", "100"));
	    duration = Long.parseLong(prefs.getString("DURATION", "3600"));
		 */
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
			filepath = params[8];
			
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
			if (filepath != null){
				builder.addBinaryBody("file", new File(filepath), ContentType.APPLICATION_OCTET_STREAM, "test.jpg");
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
	}

	@Override
	protected void onResume() {
		super.onResume();

		setContentView(R.layout.activity_viewposts);				
		mapView = (MapView) findViewById(R.id.mapview);

		//mapView.invalidate();
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
		Log.e(TAG, "mLocation.getLatitude() * 1e6 = " + mLocation.getLatitude());
		Log.e(TAG, "mLocation.getLongitude() * 1e6 = " + mLocation.getLongitude());

		mapController.setCenter(mapCenter);
		Polygon circle = new Polygon(this);
		circle.setPoints(Polygon.pointsAsCircle(mapCenter, distance));
		circle.setFillColor(0x12121212);
		circle.setStrokeColor(Color.RED);
		circle.setStrokeWidth(2);
		this.mapView.getOverlays().add(circle);
		//circle.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, map));
		//circle.setTitle("Centered on "+p.getLatitude()+","+p.getLongitude());
		overlayItemArray = new ArrayList<OverlayItem>();
		overlayItemArray.add(new OverlayItem("center", "MapCenter", mapCenter));	
		DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(this);
		this.myLocationOverlay = new ItemizedIconOverlay<OverlayItem>(overlayItemArray, null, resourceProxy);
		this.mapView.getOverlays().add(this.myLocationOverlay);

		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);	

		final Button post = (Button) findViewById(R.id.button_post);
		final Button upload = (Button) findViewById(R.id.button_upload);
		//final TextView text = (TextView) findViewById(R.id.textView1);
		final EditText text = (EditText) findViewById(R.id.editText1);
		post.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if ((text.getText().toString() != null) && (text.getText().toString().length() > 0)){

					Post post = new Post();
					me.blockcast.web.pojo.Location l = new me.blockcast.web.pojo.Location();

					l.setLat(mLocation.getLatitude());
					l.setLon(mLocation.getLongitude());
					post.setLocation(l);
					post.setParentId(-1);
					post.setContent(text.getText().toString());
					post.setDistance(distance);
					post.setEpoch((new Date().getTime())/1000l);
					post.setDuration(duration);
					if (filepath !=null){
						post.setFilePath(filepath);
					}
					String reqstring = Utils.protocol + "://" + Utils.servername + Utils.api + "insertPost/" ;
					Log.i(TAG, "reqstring:" + reqstring);

/*			
					 
					String target = params[0];
					String lat = params[1];
					String lon = params[2];
					String parentId = params[3];
					String content  = params[4];
					String distance = params[5];
					String time = params[6];
					String duration = params[7];
					filepath = params[8];
*/
					new NetworkTask().execute(reqstring, ""+post.getLocation().getLat(), ""+post.getLocation().getLon(),
							""+post.getParentId(), post.getContent(), ""+post.getDistance(), "" + post.getEpoch(), 
							""+post.getDuration(), post.getFilePath()
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
		Log.i(TAG, "item.getItemId():" + item.getItemId());
		Log.i(TAG, "R.string.settings:" +R.string.settings);
		Log.i(TAG, "R.id.action_settings:" +R.string.settings);
		Log.i(TAG, "R.id.post:" +R.string.settings);

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


}
