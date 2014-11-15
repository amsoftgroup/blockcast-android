package com.sombrerosoft.blockcast.android;

import java.io.File;

import com.ipaulpro.afilechooser.utils.FileUtils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class FilePickerActivity extends Activity {

	private static final int REQUEST_CHOOSER = 1234;
	private final String TAG = "FilePickerActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Create the ACTION_GET_CONTENT Intent
	    Intent getContentIntent = FileUtils.createGetContentIntent();

	    Intent intent = Intent.createChooser(getContentIntent, "Select a file");
	    startActivityForResult(intent, REQUEST_CHOOSER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
	        case REQUEST_CHOOSER:   
	            if (resultCode == RESULT_OK) {

	                final Uri uri = data.getData();

	                // Get the File path from the Uri
	                String path = FileUtils.getPath(this, uri);
	                Log.i(TAG, "path=" + path);
	                
	                // Alternatively, use FileUtils.getFile(Context, Uri)
	                if (path != null && FileUtils.isLocal(path)) {
	                    File file = new File(path);
	                    Log.i(TAG, "filesize=" + file.getTotalSpace());
	                }
	            }
	            
	            break;
	            
	    }
	}
}
