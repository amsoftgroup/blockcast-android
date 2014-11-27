package com.sombrerosoft.blockcast.android.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public class Utils {
	
	public static String timeformat = "yyyy/MM/dd HH:mm:ss";
	
	public static boolean isDebug = true;

	public static boolean isConnected(ConnectivityManager cm){

		//if ( cm.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||  cm.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED  );
		return  (cm.getActiveNetworkInfo() != null &&
					cm.getActiveNetworkInfo().isAvailable() &&
					cm.getActiveNetworkInfo().isConnected());

	}
	
	public String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    String ip = Formatter.formatIpAddress(inetAddress.hashCode());	                   
	                    return ip;
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        //Log.e(TAG, ex.toString());
	    }
	    return null;
	}
	/*
	public static String getIp(){
		WifiManager wifiManager = (WifiManager) getSystemService(WifiManager.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
	}
	*/
	public final static String protocol = "http";
	public final static String servername = "www.blockcast.me";
	public final static String api = "/restapiv1.0/";
	public final static String images = "/static/";
	public final static int port = 80;
}
