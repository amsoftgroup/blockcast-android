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
	private String display_format;
	private String system_of_measurement;
	private String system_wide_delay;

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
    
        refresh_delay_ms = prefs.getString("REFRESH_DELAY_MS", "nr1");
    	display_format = prefs.getString("RESULT_FORMAT", "TABLE");
    	system_of_measurement = prefs.getString("MEASUREMENT_TYPE", "METRIC");
    	system_wide_delay = prefs.getString("SYSTEM_WIDE_DELAY", "0");
    }
    
}