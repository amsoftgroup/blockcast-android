package com.sombrerosoft.blockcast.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.sombrerosoft.blockcast.android.util.Utils;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import me.blockcast.web.pojo.Post;

public class ListViewLoader extends ListActivity{

	private String TAG = "ListViewLoader";

	@Override
	protected void onResume() {
		super.onResume();

		new BlockcastGet().execute();
	}

	private class BlockcastGet extends AsyncTask<String, Void, JSONArray> {
		@Override
		protected JSONArray doInBackground(String... params) {

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = null;
			JSONArray json = null;
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

			return json;

		}

		@Override
		protected void onPostExecute(JSONArray result) {

		}
	}

}