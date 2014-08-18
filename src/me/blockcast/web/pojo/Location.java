package me.blockcast.web.pojo;

public class Location implements java.io.Serializable {

	private double lat;
	private double lon;
	private double elevation;
	
	public Location(){
	}
	
	public Location(double lon, double lat){
		this.lon = lon;
		this.lat = lat;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public double getElevation() {
		return elevation;
	}
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	
}
