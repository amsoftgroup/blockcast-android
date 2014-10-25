package com.sombrerosoft.blockcast.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sombrerosoft.blockcast.R;
import com.sombrerosoft.blockcast.android.util.Utils;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import me.blockcast.web.pojo.Post;
import android.app.LoaderManager;

//public class ListViewLoader extends Activity implements AsyncDelegate{
public class ListViewLoader extends Activity {

	private String TAG = "ListViewLoader";
	private ProgressDialog m_ProgressDialog = null;
	private ArrayList<Post> m_posts = null;
	private PostAdapter m_adapter;
	private Runnable viewOrders;
	private ListView lv;
	ArrayList<String> listdata = new ArrayList<String>();     

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_ProgressDialog = ProgressDialog.show(ListViewLoader.this,    
				"Please wait...", "Retrieving data ...", true);

		//new BlockcastGet(this).execute();
		new BlockcastGet().execute();

		setContentView(R.layout.main);
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

	@Override
	protected void onResume() {
		super.onResume();


	}

	private void getPosts(){
		Log.i(TAG + ".getPosts()", "calling runonUIThread");
		runOnUiThread(returnRes);
		Log.i(TAG + ".getPosts()", "finished runonUIThread");
	}

	/*
	 private void updateResultsInUi() {

	       // Back in the UI thread -- update our UI elements based on the data in mResults

	    	if (m_ProgressDialog != null){
	    		m_ProgressDialog.dismiss();
	    	}

			GregorianCalendar now = new GregorianCalendar();
			Date d =  now.getTime();
			DateFormat df1 = DateFormat.getDateTimeInstance();
			String s1 = df1.format(d);

			lastupdated.setText("Data refreshed every " + REFRESH_DELAY_MS_INT/1000 + " seconds.\nCurrent as of " + s1);

			if(m_orders != null && m_orders.size() > 0){
				m_adapter.notifyDataSetChanged();
				m_adapter.clear();
				for(int i=0;i<m_orders.size();i++){
					m_adapter.add(m_orders.get(i));
				}               
			}else{	
				lastupdated.setText("No results returned from WMATA.com.");
			}
			m_adapter.notifyDataSetChanged();
	    }
	 */	

	/*
	 private class getItemLists extends
     AsyncTask<Void, String, ArrayList<Post>> {
 @Override
 protected void onPreExecute() {
     super.onPreExecute();
 }

 @Override
 protected void onProgressUpdate(String... values) {
     super.onProgressUpdate(values);
 }

 @Override
 protected ArrayList<Item> doInBackground(Void... params) {
     DAO dao = new DAO();
     ArrayList<Item> fal = dao.ItemsGetList(getActivity());
     return fal;
 }

 @Override
 protected void onPostExecute(ArrayList<Item> result) {
     super.onPostExecute(result);

     falAdp = new ItemsListAdapter(getActivity(),
             R.layout.fragment_list_item_text_view, result);
     setListAdapter(falAdp);
 }
}
	 */
	private class BlockcastGet extends AsyncTask<String, Void, ArrayList<Post>> {

		private AsyncDelegate delegate;


		//public BlockcastGet(AsyncDelegate delegate){
		//    this.delegate = delegate;
		//}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		//@Override
		//protected void onProgressUpdate(String... values) {
		//    super.onProgressUpdate(values);
		//}	

		@Override
		protected ArrayList<Post> doInBackground(String... params) {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = null;
			JSONArray json = null;
			ArrayList<Post> posts = new ArrayList<Post>();
			InputStream is = null;

			try {

				HttpHost target = new HttpHost(Utils.servername, Utils.port, Utils.protocol);
				HttpGet getRequest = new HttpGet(Utils.api + "getPosts");

				Log.i(TAG, "executing http get to " + target.getHostName());

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
								p.setPostTimeString(((JSONObject)json.get(j)).getString("postTimeString"));
								//p.setLocation(((JSONObject)json.get(j)).getLong("location"));
								p.setParentId(((JSONObject)json.get(j)).getLong("parentId"));
								p.setSec_elapsed(((JSONObject)json.get(j)).getLong("sec_elapsed"));
								p.setSec_remaining(((JSONObject)json.get(j)).getLong("sec_remaining"));
								//p.setPostTimestamp(((JSONObject)json.get(j)).getString("postTimeStamp"));
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
			m_adapter.notifyDataSetChanged();
			for(int i=0;i<m_posts.size();i++){
				//Log.i(TAG, "onPostExecute adding " + m_posts.get(i).getContent());
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
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				//Log.e(TAG, "toptext: " + tt.getText());
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				//Log.e(TAG, "bottomtext: " + bt.getText());
				if (tt != null) {
					tt.setText(o.getContent());                            
				}
				if(bt != null){
					bt.setText(o.getDistance() + "m " + o.getSec_remaining() + "/" + o.getDuration() + "s");
				}
			}
			return v;
		}
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
		switch (item.getItemId()) {
		case R.id.action_settings:
		{
			Log.i(TAG, "action_settings!");
			Intent settingsActivity = new Intent(getBaseContext(),BlockcastPreferenceActivity.class);
			//Intent settingsActivity = new Intent(getBaseContext(),SampleLoader.class);
			startActivity(settingsActivity);
			break;
		}
		case R.id.view_post:
		{
			Log.i(TAG, "view_posts selected");
			Intent viewPostsActivity = new Intent(getBaseContext(),ListViewLoader.class);
			startActivity(viewPostsActivity);
			break;
		}
		case R.id.post:
		{
			Log.i(TAG, "post selected");
			Intent mainActivity = new Intent(getBaseContext(),MainActivity.class);
			startActivity(mainActivity);
			break;
		}
		}
		return true;
	}
	//@Override
	//public void asyncComplete(boolean success) {
	//	m_adapter.notifyDataSetChanged();
	//}
}