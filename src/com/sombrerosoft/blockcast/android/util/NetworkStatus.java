package com.sombrerosoft.blockcast.android.util;

public class NetworkStatus {

	private boolean isOnWifi = false;
	private boolean isOnEthernet = false;
	private boolean is3G = false;
	private boolean isBt = false;
	private int connected = -1;
	
	public boolean isOnWifi() {
		return isOnWifi;
	}
	public void setOnWifi(boolean isOnWifi) {
		this.isOnWifi = isOnWifi;
	}
	public boolean isOnEthernet() {
		return isOnEthernet;
	}
	public void setOnEthernet(boolean isOnEthernet) {
		this.isOnEthernet = isOnEthernet;
	}
	public boolean isIs3G() {
		return is3G;
	}
	public void setIs3G(boolean is3g) {
		is3G = is3g;
	}
	public boolean isBt() {
		return isBt;
	}
	public void setBt(boolean isBt) {
		this.isBt = isBt;
	}
	public int getConnected() {
		return connected;
	}
	public void setConnected(int connected) {
		this.connected = connected;
	}
}
