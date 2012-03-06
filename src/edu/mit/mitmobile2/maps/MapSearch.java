package edu.mit.mitmobile2.maps;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

// MapSearch Class - provides an interface for performing searches against the ArcGIS server and displaying the results 

public class MapSearch implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String image; // (optional) - the image to use for annotations
	public String color; // (optional) - the color to use for annotations
	public String type; // String contants from the MapSearchType object - keyword, canned building, canned restroom, etc
	public String building[]; // String array of building numbers
	public String floor[]; // array of strings (defaults to all floors)
	public Map<String, Object> criteria; //class: hash map of criteria such as key words, handicapped accessible, 		id required, near me, etc
	
	public static class CriteriaType {
		public static String KEY_WORDS = "KEY_WORDS";
		public static String HANDICAPPED_ACCESSIBLE = "HANDICAPPED_ACCESSIBLE";
		public static String ID_REQUIRED = "ID_REQUIRED";
		public static String NEAR_ME = "NEAR_ME";		
	}
	
	public MapSearch() {
		super();
		// TODO Auto-generated constructor stub
		criteria = new HashMap<String,Object>();
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getBuilding() {
		return building;
	}

	public void setBuilding(String[] building) {
		this.building = building;
	}

	public String[] getFloor() {
		return floor;
	}

	public void setFloor(String[] floor) {
		this.floor = floor;
	}

	public Map<String,Object> getCriteria() {
		return criteria;
	}

	public void setCriteria(Map<String,Object> criteria) {
		this.criteria = criteria;
	}

}
