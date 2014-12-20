package com.sombrerosoft.blockcast.android.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public class Utils {
	
	public static String timeformat = "yyyy/MM/dd HH:mm:ss";
	
	public static boolean isDebug = true;

	public static NetworkStatus isConnected(Context c){
		
		NetworkStatus status = new NetworkStatus();

		ConnectivityManager connectivityManager = (ConnectivityManager)
                c.getSystemService(Context.CONNECTIVITY_SERVICE);

		ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mEthernet = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		NetworkInfo m3G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo bt = connManager.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
		
		if (mWifi!=null) status.setOnWifi(mWifi.isConnected());
		if (mEthernet!=null) status.setOnEthernet(mEthernet.isConnected());
		if (m3G!=null) status.setIs3G(m3G.isConnected());
		if (bt!=null) status.setIs3G(bt.isConnected());
		
		if  (connectivityManager.getActiveNetworkInfo() != null &&
				connectivityManager.getActiveNetworkInfo().isAvailable() &&
				connectivityManager.getActiveNetworkInfo().isConnected()){
				status.setConnected(connectivityManager.getActiveNetworkInfo().getType());
			
		};
		
		return status;
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
