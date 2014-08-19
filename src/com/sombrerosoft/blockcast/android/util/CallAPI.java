package com.sombrerosoft.blockcast.android.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import android.content.Intent;
import android.os.AsyncTask;

public class CallAPI extends AsyncTask<String, String, String> {
	
	
    @Override
    protected String doInBackground(String... params) {
      String urlString=params[0]; // URL to call
      String resultToDisplay = "";
      InputStream in = null;
      String result = null;
        
      // HTTP Get
      try {
        URL url = new URL(urlString);
    	HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    	in = new BufferedInputStream(urlConnection.getInputStream());
      } catch (Exception e ) {
        System.out.println(e.getMessage());
    	return e.getMessage();
      }
    	
/*
       // Parse XML
       XmlPullParserFactory pullParserFactory;
       try {
    	    pullParserFactory = XmlPullParserFactory.newInstance();
    	    XmlPullParser parser = pullParserFactory.newPullParser();
    	    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false); 
    	    parser.setInput(in, null);
    	    result = parseXML(parser);
       } catch (XmlPullParserException e) {
    	   e.printStackTrace();
       } catch (IOException e) {
    	   e.printStackTrace();
       }
*/
       DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
       HttpPost httppost = new HttpPost(Utils.servername + Utils.api);
       // Depends on your web service
       httppost.setHeader("Content-type", "application/json");

       InputStream inputStream = null;
      
       try {
           HttpResponse response = httpclient.execute(httppost);           
           HttpEntity entity = response.getEntity();

           inputStream = entity.getContent();
           // json is UTF-8 by default
           BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
           StringBuilder sb = new StringBuilder();

           String line = null;
           while ((line = reader.readLine()) != null)
           {
               sb.append(line + "\n");
           }
           result = sb.toString();
       } catch (Exception e) { 
           // Oops
       }
       finally {
           try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
       }
       
       /*
       // Simple logic to determine if the email is dangerous, invalid, or valid
       if (result != null ) {
         if( result.hygieneResult.equals("Spam Trap")) {
    	   resultToDisplay = "Dangerous email, please correct";
    	 }
    	 else if( Integer.parseInt(result.statusNbr) >= 300) {
    	   resultToDisplay = "Invalid email, please re-enter";
    	 }
    	 else {
    	   resultToDisplay = "Thank you for your submission";
    	 }
    		
       }
       else {
         resultToDisplay = "Exception Occured";
       }
     */
       return resultToDisplay;

    }

    /*
    protected void onPostExecute(String res) {
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class); 
   		intent.putExtra(EXTRA_MESSAGE, res);
    	startActivity(intent);
    }
    
    private emailVerificationResult parseXML( XmlPullParser parser ) throws XmlPullParserException, IOException {
      int eventType = parser.getEventType();
      emailVerificationResult result = new emailVerificationResult(); 
        
      while( eventType!= XmlPullParser.END_DOCUMENT) {
      String name = null;
      switch(eventType)
      {
        case XmlPullParser.START_TAG:
      	  name = parser.getName();
      	  if( name.equals("Error")) {
      	    System.out.println("Web API Error!");
      	  }
      	  else if ( name.equals("StatusNbr")) {
      	    result.statusNbr = parser.nextText();
      	  }
      	  else if (name.equals("HygieneResult")) {
      	    result.hygieneResult = parser.nextText();
      	  }
      	  break;
      	case XmlPullParser.END_TAG:
      	  break;
      	} // end switch
      	
      	eventType = parser.next();	
      } // end while
        return result;		
     }
*/

} // end CallAPI

