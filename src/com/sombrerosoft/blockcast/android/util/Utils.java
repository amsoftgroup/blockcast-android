package com.sombrerosoft.blockcast.android.util;

import android.net.ConnectivityManager;

public class Utils {
	
	public static String timeformat = "yyyy/MM/dd HH:mm:ss";
	
	public static boolean isDebug = true;

	public static boolean isConnected(ConnectivityManager cm){

		//if ( cm.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||  cm.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED  );
		return  (cm.getActiveNetworkInfo() != null &&
					cm.getActiveNetworkInfo().isAvailable() &&
					cm.getActiveNetworkInfo().isConnected());

	}
	
	public final static String protocol = "http";
	public final static String servername = "www.blockcast.me";
	public final static String api = "/restapiv1.0/";
	public final static int port = 80;
}
