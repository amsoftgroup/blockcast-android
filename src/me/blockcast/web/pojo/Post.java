package me.blockcast.web.pojo;

//Generated Feb 22, 2014 9:46:04 AM by Hibernate Tools 3.4.0.CR1

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
* Op generated by hbm2java
*/
public class Post implements java.io.Serializable {

	/*
	 * "content":"test content 2",
	 * "distance":537,
	 * "duration":83250,
	 * "id":799359,
	 * "lat":19.3022700106744,
	 * "location":null,
	 * "lon":-98.5396733640062,
	 * "parentId":-1,
	 * "postTimestamp":"Oct 21, 2014",
	 * "postTimeString":"2014/10/21 20:45:29",
	 * "sec_elapsed":6662,
	 * "sec_remaining":76588}
	 * **/
	 
	private long id;
	private Location location;
	private long lat;
	private long lon;
	private String content;
	private long parentId;
	//private Date postTimestamp;
	private long duration;
	private long distance;
	private long epoch;
	//private String postTimeString;
	private long sec_elapsed;
	private long sec_remaining;
	private File file;
	private String filePath = null;
	
	public Post() {
	}

	public Post(long id, Location location, String content, long parentId) {
		this.id = id;
		this.location = location;
		this.content = content;
		this.parentId = parentId;
	}

	public Post(long id, Location location, String content, long parentId,
			long epoch) {
		this.id = id;
		this.location = location;
		this.content = content;
		this.parentId = parentId;
		this.epoch = epoch;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getParentId() {
		return this.parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

/*
	public Date getPostTimestamp() {
		return this.postTimestamp;
	}

	public void setPostTimestamp(Date postTimestamp) {
		this.postTimestamp = postTimestamp;
	}
*/
	public long getDistance() {
		return distance;
	}

	public void setDistance(long distance) {
		this.distance = distance;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
	public long getLat() {
		return lat;
	}

	public void setLat(long lat) {
		this.lat = lat;
	}

	public long getLon() {
		return lon;
	}

	public void setLon(long lon) {
		this.lon = lon;
	}
/*
	public String getPostTimeString() {
		return postTimeString;
	}

	public void setPostTimeString(String postTimeString) {
		this.postTimeString = postTimeString;
	}
*/
	public long getSec_elapsed() {
		return sec_elapsed;
	}

	public void setSec_elapsed(long sec_elapsed) {
		this.sec_elapsed = sec_elapsed;
	}

	public long getSec_remaining() {
		return sec_remaining;
	}
	public void setSec_remaining(long sec_remaining) {
		this.sec_remaining = sec_remaining;
	}

	public long getEpoch() {
		return epoch;
	}

	public void setEpoch(long epoch) {
		this.epoch = epoch;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
