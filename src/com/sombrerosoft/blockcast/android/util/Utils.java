package com.sombrerosoft.blockcast.android.util;

import android.net.ConnectivityManager;

public class Utils {
	
	public static String timeformat = "yyyy/MM/dd HH:mm:ss";

	public static boolean isConnected(ConnectivityManager cm){

		//if ( cm.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||  cm.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED  );
		return  (cm.getActiveNetworkInfo() != null &&
					cm.getActiveNetworkInfo().isAvailable() &&
					cm.getActiveNetworkInfo().isConnected());

	}
}
