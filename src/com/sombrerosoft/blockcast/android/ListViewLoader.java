package com.sombrerosoft.blockcast.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.sombrerosoft.blockcast.R;
import com.sombrerosoft.blockcast.android.util.Utils;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import me.blockcast.common.Post;
import android.app.LoaderManager;

//public class ListViewLoader extends Activity implements AsyncDelegate{
public class ListViewLoader extends BlockcastBaseActivity {

	private String TAG = "ListViewLoader";
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Post> m_posts = null;
	private PostAdapter m_adapter;
	private Runnable viewOrders;
	private ListView lv;
	private Button send;
	ArrayList<String> listdata = new ArrayList<String>();     
	//private Bitmap mIcon11 = null;
	private CountDownTimer countDownTimer;
	private boolean timerHasStarted = false;
	private TextView tt;
	private TextView bt;
	private Chronometer c;
	private ImageView iv;
	private Button delButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();

		final BlockcastGet bcg = new BlockcastGet();
		bcg.execute();

		m_ProgressDialog = ProgressDialog.show(ListViewLoader.this, "Please wait...", "Retrieving data ...", true);
		m_ProgressDialog.setCancelable(true);
		m_ProgressDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				bcg.cancel(true);
			}

		});

		m_ProgressDialog.show();

		new BlockcastGet().execute();

		setContentView(R.layout.main);

		send = (Button)this.findViewById(R.id.button_send);  
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "send.onClick");
				Intent postIntent = new Intent(getBaseContext(),MainActivity.class);	
				startActivity(postIntent);

				finish();
			}
		});

		m_posts = new ArrayList<Post>();
		this.m_adapter = new PostAdapter(this, R.layout.row, m_posts);
		lv = (ListView)this.findViewById(android.R.id.list);  
		lv.setAdapter(this.m_adapter);

		viewOrders = new Runnable(){
			@Override
			public void run() {
				getPosts();
			}
		};

		Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
		thread.start();

	}

	private void getPosts(){
		Log.i(TAG + ".getPosts()", "calling runonUIThread");
		runOnUiThread(returnRes);
		Log.i(TAG + ".getPosts()", "finished runonUIThread");	
	}

	private class BlockcastGet extends AsyncTask<String, Void, ArrayList<Post>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected ArrayList<Post> doInBackground(String... params) {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = null;
			JSONArray json = null;
			ArrayList<Post> posts = new ArrayList<Post>();
			InputStream is = null;

			try {

				HttpHost target = new HttpHost(Utils.servername, Utils.port, Utils.protocol);
				//HttpGet getRequest = new HttpGet(Utils.api + "getPosts");

				//HttpGet getRequest = new HttpGet(Utils.api + "getPostsByDistanceAndDuration/" + distance + "/" + mLocation.getLatitude() + "/" + mLocation.getLongitude()); 
				HttpGet getRequest = new HttpGet(Utils.api + "getPostsByDistanceAndDurationWithGuid/" + distance + "/" + mLocation.getLatitude() + "/" + mLocation.getLongitude() + "/" + guid); 

				//Log.i(TAG, "Sending: " + Utils.api + "getPostsByDistanceAndDuration/" + distance + "/" + mLocation.getLatitude() + "/" + mLocation.getLongitude());


				Log.i(TAG, "executing http get to " + getRequest.getURI().toString());

				httpResponse = httpclient.execute(target, getRequest);

				if (httpResponse.getStatusLine().getStatusCode() != 200) {
					Log.e(TAG, "StatusCode() != 200");
					throw new RuntimeException("Failed : HTTP error code : "
							+ httpResponse.getStatusLine().getStatusCode());
				}else{
					Log.i(TAG, "Success: HTTP code " + httpResponse.getStatusLine().getStatusCode());
				}

				HttpEntity entity = httpResponse.getEntity();     

				Log.i(TAG, httpResponse.getStatusLine().toString());

				Header[] headers = httpResponse.getAllHeaders();
				for (int i = 0; i < headers.length; i++) {
					Log.i(TAG, "headers[" + i + "]" + headers[i]);
				}		      


				//Do something with result
				if (httpResponse != null){

					is = entity.getContent();

					BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 

					StringBuilder responseStrBuilder = new StringBuilder();
					String inputStr = "";
					int i = 0;
					while ((inputStr = streamReader.readLine()) != null){
						Log.i(TAG, "assembling json:" + i + " " + inputStr);
						responseStrBuilder.append(inputStr);
					}

					json = new JSONArray(responseStrBuilder.toString());

					if (json != null) { 
						//Log.i(TAG, "doInBackground: json.length()=" + json.length());
						for (int j=0;j<json.length();j++){ 
							Post p = new Post();
							try {
								//Log.i(TAG, "doInBackground adding posts:" + json.get(j).toString());
								p.setContent(((JSONObject)json.get(j)).getString("content"));
								p.setDistance(((JSONObject)json.get(j)).getLong("distance"));
								p.setDuration(((JSONObject)json.get(j)).getLong("duration"));								
								p.setId(((JSONObject)json.get(j)).getLong("id"));
								p.setLat(((JSONObject)json.get(j)).getLong("lat"));
								p.setLon(((JSONObject)json.get(j)).getLong("lon"));
								p.setEpoch(((JSONObject)json.get(j)).getLong("epoch"));
								p.setParentId(((JSONObject)json.get(j)).getLong("parentId"));
								//p.setSec_elapsed(((JSONObject)json.get(j)).getLong("sec_elapsed"));
								p.setMedia_preview(((JSONObject)json.get(j)).getString("media_preview"));

								if ((p.getMedia_preview() != null) && (!p.getMedia_preview().equalsIgnoreCase("null"))){
									InputStream in = new java.net.URL(Utils.protocol + "://" + Utils.servername + Utils.images + p.getMedia_preview()).openStream();
									p.setImage(BitmapFactory.decodeStream(in));
								} 
								p.setMine(((JSONObject)json.get(j)).getInt("mine"));
								//p.setMediaFile(((JSONObject)json.get(j)).<File>("mediafile"));
								posts.add(p);
							} catch (JSONException e) {
								Log.e(TAG, "Exiting. doInBackground: " + e.toString());
								System.exit(0);
							}
						} 
					}
				}

			} catch (IllegalStateException e) {
				Log.e(TAG, e.toString());
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			} finally {
				// When HttpClient instance is no longer needed,
				// shut down the connection manager to ensure
				// immediate deallocation of all system resources
				httpclient.getConnectionManager().shutdown();
			}			

			return posts;
		}

		@Override
		protected void onPostExecute(ArrayList<Post> result) {
			super.onPostExecute(result);

			Log.i(TAG, "onPostExecute entered result.size()="+result.size());

			m_posts = result;
			m_adapter.clear();

			for(int i=0;i<m_posts.size();i++){
				Log.i(TAG, "onPostExecute adding " + m_posts.get(i).getContent());
				m_adapter.add(m_posts.get(i));
			}

			m_adapter.notifyDataSetChanged();
			m_ProgressDialog.dismiss();	
		}

	}

	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			Log.i(TAG, "returnRes running");
			if(m_posts != null && m_posts.size() > 0){
				Log.i(TAG, "m_posts != null && m_posts.size() > 0");
				m_adapter.notifyDataSetChanged();
				for(int i=0;i<m_posts.size();i++){
					Log.i(TAG, "adding " + m_posts.get(i).getContent());
					m_adapter.add(m_posts.get(i));
				}
			}
			//m_ProgressDialog.dismiss();
			m_adapter.notifyDataSetChanged();
		}
	};

	private class PostAdapter extends ArrayAdapter<Post> {

		private ArrayList<Post> items;

		public PostAdapter(Context context, int textViewResourceId, ArrayList<Post> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			
			Post o = items.get(position);

			if (o != null) {

				long baseTime = SystemClock.elapsedRealtime();
				//Log.i(TAG, " baseTime = " + baseTime);
				long sec_elapsed = o.getSec_elapsed();
				//Log.i(TAG, " sec_elapsed = " + sec_elapsed);
				long sec_remaining = o.getDuration() - sec_elapsed;
				//Log.i(TAG, " sec_remaining = " + sec_remaining);	


				//long epoch = o.get
				Date now = new Date();
				long now_seconds = now.getTime();
				//long then = o.=-o];

				tt = (TextView) v.findViewById(R.id.toptext);
				//Log.e(TAG, "toptext: " + tt.getText());
				bt = (TextView) v.findViewById(R.id.bottomtext);
				//Log.e(TAG, "bottomtext: " + bt.getText());
				c = (Chronometer) v.findViewById(R.id.chronometer);
				iv = (ImageView) v.findViewById(R.id.icon);
				delButton = (Button) v.findViewById(R.id.delete);

				if (tt != null) {
					//Log.e(TAG, "tt != null");
					tt.setText(o.getContent());                            
				}
				if(bt != null){
					//Log.e(TAG, "bt != null");
					//Date expiry = new Date(Long.parseLong("" + o.getEpoch() * 1000l));
					bt.setText(o.getDistance() + "m " + ( o.getDuration()) + "s");		  
				}
				if (c !=null){
					c.setBase(0);
					c.start();
				}
				if ((iv !=null) && (o.getImage() !=null)){
					iv.setImageBitmap(o.getImage());
				}else{
					iv.setImageBitmap(null);
				}
				if (delButton !=null){
					delButton.setText("delete");
					MyOnClickListener ocl = new MyOnClickListener("" + o.getId());					
					delButton.setOnClickListener(ocl);
					
					if (o.getMine() == 1){
						delButton.setVisibility(android.view.View.VISIBLE);
					}else{
						delButton.setVisibility(android.view.View.GONE);
					}
					
		
				}

				/*
				Log.e(TAG, "o.getMine()=" + o.getMine());
				MyOnClickListener ocl = new MyOnClickListener("" + o.getId());
				
				delButton.setOnClickListener(ocl);
				if (o.getMine() == 1){
					delButton.setVisibility(0);
				}else{
					delButton.setVisibility(2);
				}
				*/
			}
			return v;
		}
	}

	private class DeletePostTask extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... params) {

			String target = params[0];
			String guid = params[1];
			String commentid = params[2];

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();     
			//builder.setBoundary("+++BOUNDARY+++");
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addTextBody("commentid", commentid);
			builder.addTextBody("guid", guid);

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
			
			//byte[] entityContentAsBytes = out.toByteArray();, or convert to string
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
	}

	private class MyOnClickListener implements OnClickListener{

		String postid = "";

		public MyOnClickListener(String postid) {
			this.postid = postid;
		}

		@Override
		public void onClick(View v)
		{
			Log.e(TAG, "CLICKED o.getMine(): postid="+postid + " guid=" + guid);
			if (v.getId() == R.id.delete){

				delButton = (Button) v.findViewById(R.id.delete);

				String reqstring = Utils.protocol + "://" + Utils.servername + Utils.api + "deletePost/" ;
				Log.i(TAG, "reqstring:" + reqstring);
		        
				new DeletePostTask().execute(reqstring, guid, postid);

				Intent refresh = new Intent(getBaseContext(),ListViewLoader.class);	
				startActivity(refresh);

				finish();
			}
		}
	}
}
