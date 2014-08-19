package com.sombrerosoft.blockcast.android;

import com.sombrerosoft.blockcast.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

 
public class BlockcastPreferenceActivity extends PreferenceActivity {

	//public static final String PREF_FILE = "WMATA_LOCATOR_PREF_FILE";
	
	boolean CheckboxPreference;
	private String refresh_delay_ms;
	private String system_of_measurement;
	private String distance;
	private String duration;
	

	private String TAG = "BlockcastPreferenceActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		addPreferencesFromResource(R.xml.preferences);

	}
	
	@Override
	protected void onStart() {	
		super.onStart();
		getPrefs();
	}
	
    private void getPrefs() {
        // Get the xml/preferences.xml preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        distance = prefs.getString("DISTANCE", "100");
        duration = prefs.getString("DURATION", "3600");
        refresh_delay_ms = prefs.getString("REFRESH_DELAY_MS", "nr1");
    	system_of_measurement = prefs.getString("MEASUREMENT_TYPE", "METRIC");
    	
    }
    
}